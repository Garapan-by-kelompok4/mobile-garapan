package com.app.garapan.presentation.screen.order_detail

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.app.garapan.presentation.navigation.Routes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.app.garapan.presentation.components.PesananStatusBadge
import com.app.garapan.presentation.payment.SnapPaymentLauncher
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White

@Composable
fun OrderDetailScreen(
    navController: NavController,
    viewModel: OrderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    var paymentLaunched by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (paymentLaunched) {
                        paymentLaunched = false
                        viewModel.onReturnedFromPayment()
                    } else {
                        viewModel.refresh()
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OrderDetailEvent.LaunchSnapPayment -> {
                    if (activity == null) {
                        Toast.makeText(context, "Tidak dapat membuka pembayaran.", Toast.LENGTH_SHORT).show()
                        return@collect
                    }
                    paymentLaunched = true
                    SnapPaymentLauncher.open(activity, event.snapToken)
                }
                is OrderDetailEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is OrderDetailEvent.NavigateToReview -> {
                    navController.navigate(com.app.garapan.presentation.navigation.Routes.reviewRoute(event.pesananId))
                }
            }
        }
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AccentBlue
                    )
                }
                Text(
                    text = "Detail Pesanan",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue
                    )
                )
            }
        },
        bottomBar = {
            if (uiState.canPay || uiState.canDeliver || uiState.canComplete || uiState.canReview || uiState.canDispute) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (uiState.canPay) {
                            Button(
                                onClick = viewModel::onPayClicked,
                                enabled = !uiState.isActionLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandNavy,
                                    contentColor = White
                                )
                            ) {
                                if (uiState.isActionLoading) {
                                    CircularProgressIndicator(
                                        color = White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.height(22.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Lanjutkan Pembayaran",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                }
                            }
                            TextButton(
                                onClick = viewModel::onRefreshStatusClicked,
                                enabled = !uiState.isActionLoading,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Perbarui Status")
                            }
                        } else if (uiState.canReview) {
                            Button(
                                onClick = viewModel::onReviewClicked,
                                enabled = !uiState.isActionLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandNavy,
                                    contentColor = White
                                )
                            ) {
                                Text(
                                    text = uiState.reviewButtonLabel,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                            }
                        } else {
                            if (uiState.canDeliver || uiState.canComplete) {
                                Button(
                                    onClick = {
                                        if (uiState.canDeliver) viewModel.onDeliverClicked()
                                        else viewModel.onCompleteClicked()
                                    },
                                    enabled = !uiState.isActionLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    shape = RoundedCornerShape(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandNavy,
                                        contentColor = White
                                    )
                                ) {
                                    if (uiState.isActionLoading) {
                                        CircularProgressIndicator(
                                            color = White,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.height(22.dp)
                                        )
                                    } else {
                                        Text(
                                            text = if (uiState.canDeliver) {
                                                "Tandai Sudah Dikirim"
                                            } else {
                                                "Terima Pekerjaan"
                                            },
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        )
                                    }
                                }
                            }
                            if (uiState.canDispute) {
                                OutlinedButton(
                                    onClick = { navController.navigate(Routes.disputeRoute(uiState.id)) },
                                    enabled = !uiState.isActionLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    shape = RoundedCornerShape(50.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE31B23)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFE31B23)
                                    )
                                ) {
                                    Text(
                                        text = "Ajukan Dispute / Komplain",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandNavy)
                }
            }
            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = viewModel::retry) {
                        Text("Coba Lagi")
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    uiState.actionMessage?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall.copy(color = AccentBlue)
                        )
                    }
                    if (uiState.showDisputedInfoBanner) {
                        DisputedInfoBanner()
                    }
                    DetailCard {
                        PesananStatusBadge(status = uiState.status)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = uiState.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.createdAt,
                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                        )
                    }
                    DetailCard {
                        DetailRow(label = uiState.counterpartyLabel, value = uiState.counterpartyName)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow(label = "Total", value = uiState.amount, emphasize = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun DisputedInfoBanner() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFE1E1))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Pesanan dalam sengketa",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE31B23)
            )
        )
        Text(
            text = "Klien mengajukan komplain. Menunggu keputusan admin.",
            style = MaterialTheme.typography.bodySmall.copy(color = PrimaryText)
        )
    }
}

@Composable
private fun DetailCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .padding(20.dp)
    ) {
        content()
    }
}

@Composable
private fun DetailRow(label: String, value: String, emphasize: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (emphasize) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (emphasize) AccentBlue else PrimaryText
            )
        )
    }
}
