package com.app.garapan.presentation.screen.dispute

import android.widget.Toast
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.presentation.components.AppCard
import com.app.garapan.presentation.components.AppPrimaryButton
import com.app.garapan.presentation.components.AppTopBar
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.ErrorRed
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface

@Composable
fun DisputeScreen(
    navController: NavController,
    viewModel: DisputeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DisputeEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                DisputeEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Ajukan Dispute?") },
            text = {
                Text(
                    "Pesanan akan dikunci dalam status sengketa dan dana escrow ditahan " +
                        "sampai admin menyelesaikan. Tindakan ini tidak dapat dibatalkan."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.onSubmitDispute()
                    }
                ) {
                    Text("Kirim", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            AppTopBar(title = "Ajukan Dispute", onBack = { navController.navigateUp() })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Laporkan Masalah Pesanan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Silakan jelaskan masalah secara rinci. Alasan ini akan ditinjau oleh Admin untuk penyelesaian escrow sengketa.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Alasan Dispute",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.reason,
                        onValueChange = viewModel::onReasonChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = {
                            Text(
                                text = "Tuliskan alasan detail mengapa Anda mengajukan dispute untuk pesanan ini...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SecondaryText.copy(alpha = 0.6f)
                            )
                        },
                        isError = uiState.errorMessage != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = SecondaryText.copy(alpha = 0.3f),
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            focusedTextColor = PrimaryText,
                            unfocusedTextColor = PrimaryText
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (uiState.errorMessage != null) {
                            Text(
                                text = uiState.errorMessage.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Text(
                            text = "${uiState.reasonLength}/${DisputeValidation.MAX_REASON_LENGTH}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (uiState.reasonLength >= DisputeValidation.MIN_REASON_LENGTH) {
                                    AccentBlue
                                } else {
                                    SecondaryText
                                }
                            )
                        )
                    }
                    if (uiState.reasonLength < DisputeValidation.MIN_REASON_LENGTH) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Minimal ${DisputeValidation.MIN_REASON_LENGTH} karakter.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppPrimaryButton(
                text = "Kirim Dispute",
                onClick = { showConfirmDialog = true },
                enabled = uiState.canSubmit,
                isLoading = uiState.isLoading
            )
        }
    }
}
