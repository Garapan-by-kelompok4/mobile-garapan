package com.app.garapan.presentation.screen.review

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.presentation.components.AppPrimaryButton
import com.app.garapan.presentation.components.AppTopBar
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.ErrorRed
import com.app.garapan.ui.theme.LightGray
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.StarYellow
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White

@Composable
fun ReviewScreen(
    navController: NavController,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ReviewEvent.Submitted -> {
                    val message = if (event.isEditMode) {
                        "Ulasan berhasil diperbarui."
                    } else {
                        "Ulasan berhasil dikirim."
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    navController.navigateUp()
                }
                is ReviewEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            AppTopBar(
                title = if (uiState.isEditMode) "Edit Ulasan" else "Beri Ulasan",
                onBack = { navController.navigateUp() }
            )
        },
        bottomBar = {
            if (!uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    AppPrimaryButton(
                        text = if (uiState.isEditMode) "Simpan Perubahan" else "Kirim Ulasan",
                        onClick = viewModel::submit,
                        enabled = uiState.isSubmitEnabled && !uiState.isSubmitting,
                        isLoading = uiState.isSubmitting
                    )
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandNavy)
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ReviewPanel(uiState = uiState, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun ReviewPanel(
    uiState: ReviewUiState,
    viewModel: ReviewViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = uiState.jasaTitle,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        )
        Text(
            text = listOf(uiState.workerName, uiState.orderDate)
                .filter { it.isNotBlank() }
                .joinToString(" - "),
            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
        )
        RatingPicker(
            rating = uiState.rating,
            onRatingChanged = viewModel::onRatingChanged
        )
        OutlinedTextField(
            value = uiState.comment,
            onValueChange = viewModel::onCommentChanged,
            enabled = uiState.isSubmitEnabled && !uiState.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            label = { Text("Ulasan") },
            placeholder = { Text("Ceritakan pengalamanmu bekerja dengan freelancer ini.") }
        )
        Text(
            text = "${uiState.comment.length}/500",
            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText),
            modifier = Modifier.align(Alignment.End)
        )
        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = ErrorRed,
                    fontWeight = FontWeight.SemiBold
                )
            )
            if (!uiState.isSubmitEnabled) {
                TextButton(onClick = viewModel::retry) {
                    Text("Muat Ulang")
                }
            }
        }
    }
}

@Composable
private fun RatingPicker(
    rating: Int,
    onRatingChanged: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..5).forEach { value ->
            Icon(
                imageVector = Lucide.Star,
                contentDescription = "$value bintang",
                tint = if (value <= rating) StarYellow else LightGray,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onRatingChanged(value) }
                    .padding(4.dp)
            )
        }
    }
}
