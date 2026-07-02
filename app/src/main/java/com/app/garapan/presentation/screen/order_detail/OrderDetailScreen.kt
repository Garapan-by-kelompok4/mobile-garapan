package com.app.garapan.presentation.screen.order_detail

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageSquare
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.app.garapan.presentation.components.AppCard
import com.app.garapan.presentation.components.AppPrimaryButton
import com.app.garapan.presentation.components.AppTopBar
import com.app.garapan.presentation.components.PesananStatusBadge
import com.app.garapan.presentation.payment.SnapPaymentLauncher
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.ErrorRed
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
    var showCancelDialog by remember { mutableStateOf(false) }

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
                is OrderDetailEvent.NavigateToChat -> {
                    navController.navigate(event.route) { launchSingleTop = true }
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Batalkan Pesanan") },
            text = { Text("Batalkan pesanan ini? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    viewModel.onCancelClicked()
                }) {
                    Text("Batalkan", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            AppTopBar(title = "Detail Pesanan", onBack = { navController.navigateUp() })
        },
        bottomBar = {
            if (uiState.canPay || uiState.canReview || uiState.canDeliver || uiState.canComplete || uiState.canDispute) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (uiState.canPay) {
                            AppPrimaryButton(
                                text = "Lanjutkan Pembayaran",
                                onClick = viewModel::onPayClicked,
                                enabled = !uiState.isActionLoading,
                                isLoading = uiState.isActionLoading
                            )
                        } else if (uiState.canReview) {
                            AppPrimaryButton(
                                text = uiState.reviewButtonLabel,
                                onClick = viewModel::onReviewClicked,
                                enabled = !uiState.isActionLoading
                            )
                        } else {
                            if (uiState.canDeliver || uiState.canComplete) {
                                AppPrimaryButton(
                                    text = if (uiState.canDeliver) {
                                        "Tandai Sudah Dikirim"
                                    } else {
                                        "Terima Pekerjaan"
                                    },
                                    onClick = {
                                        if (uiState.canDeliver) viewModel.onDeliverClicked()
                                        else viewModel.onCompleteClicked()
                                    },
                                    enabled = !uiState.isActionLoading,
                                    isLoading = uiState.isActionLoading
                                )
                            }
                            if (uiState.canDispute) {
                                OutlinedButton(
                                    onClick = { navController.navigate(Routes.disputeRoute(uiState.id)) },
                                    enabled = !uiState.isActionLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    shape = RoundedCornerShape(50.dp),
                                    border = BorderStroke(1.dp, ErrorRed),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = ErrorRed
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
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
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
                        .consumeWindowInsets(innerPadding)
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
                        .consumeWindowInsets(innerPadding)
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
                        if (uiState.canCancel) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(
                                    onClick = { showCancelDialog = true },
                                    enabled = !uiState.isActionLoading
                                ) {
                                    Text(
                                        text = "Batalkan Pesanan",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = ErrorRed
                                        )
                                    )
                                }
                            }
                        }
                    }
                    DetailCard {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            DetailRow(
                                label = uiState.counterpartyLabel,
                                value = uiState.counterpartyName,
                                modifier = Modifier.weight(1f)
                            )
                            if (uiState.canChat) {
                                Spacer(modifier = Modifier.width(8.dp))
                                ChatIconButton(onClick = viewModel::onChatClicked)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow(label = "Total", value = uiState.amount, emphasize = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatIconButton(onClick: () -> Unit) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = Surface,
            contentColor = BrandNavy
        )
    ) {
        Icon(
            imageVector = Lucide.MessageSquare,
            contentDescription = "Buka Chat",
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun DisputedInfoBanner() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ErrorRed.copy(alpha = 0.1f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Pesanan dalam sengketa",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = ErrorRed
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
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, emphasize: Boolean = false, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText),
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (emphasize) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (emphasize) AccentBlue else PrimaryText
            ),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End
        )
    }
}
