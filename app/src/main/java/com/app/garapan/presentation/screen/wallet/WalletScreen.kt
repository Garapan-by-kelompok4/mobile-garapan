package com.app.garapan.presentation.screen.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.app.garapan.domain.model.WalletSummary
import com.app.garapan.domain.model.WalletTransaction
import com.app.garapan.domain.model.WalletTransactionDirection
import com.app.garapan.domain.model.WalletTransactionStatus
import com.app.garapan.domain.model.WalletTransactionType
import com.app.garapan.domain.model.Withdrawal
import com.app.garapan.presentation.components.AppCard
import com.app.garapan.presentation.components.AppPrimaryButton
import com.app.garapan.presentation.components.AppTopBar
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.ErrorRed
import com.app.garapan.ui.theme.LightGray
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.StarYellow
import com.app.garapan.ui.theme.SuccessGreen
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White
import com.composables.icons.lucide.ArrowDownLeft
import com.composables.icons.lucide.ArrowUpRight
import com.composables.icons.lucide.CircleDollarSign
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ReceiptText
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    navController: NavController,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is WalletEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (uiState.showWithdrawalSheet) {
        WithdrawalBottomSheet(
            uiState = uiState,
            onDismiss = viewModel::onDismissWithdrawalSheet,
            onAmountChanged = viewModel::onWithdrawalAmountChanged,
            onBankNameChanged = viewModel::onBankNameChanged,
            onAccountNumberChanged = viewModel::onAccountNumberChanged,
            onAccountHolderNameChanged = viewModel::onAccountHolderNameChanged,
            onNoteChanged = viewModel::onNoteChanged,
            onSubmit = viewModel::submitWithdrawal,
            modifier = Modifier,
            sheetState = sheetState
        )
    }

    Scaffold(
        containerColor = Surface,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            AppTopBar(
                title = if (uiState.isMahasiswa) "Dompet Saya" else "Saldo Refund",
                onBack = { navController.navigateUp() }
            )
            when {
                uiState.isLoading -> LoadingContent()
                uiState.errorMessage != null -> WalletErrorContent(
                    message = uiState.errorMessage.orEmpty(),
                    onRetry = viewModel::retry
                )
                else -> WalletContent(
                    uiState = uiState,
                    onWithdrawClick = viewModel::onOpenWithdrawalSheet,
                    onFilterSelected = viewModel::onFilterSelected,
                    onTransactionClick = { transaction ->
                        transaction.pesananId?.takeIf { it.isNotBlank() }?.let {
                            navController.navigate(Routes.orderDetailRoute(it))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = BrandNavy)
    }
}

@Composable
private fun WalletErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
        Spacer(modifier = Modifier.height(16.dp))
        AppPrimaryButton(text = "Coba Lagi", onClick = onRetry)
    }
}

@Composable
private fun WalletContent(
    uiState: WalletUiState,
    onWithdrawClick: () -> Unit,
    onFilterSelected: (WalletFilter) -> Unit,
    onTransactionClick: (WalletTransaction) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            BalanceCard(
                summary = uiState.summary,
                isMahasiswa = uiState.isMahasiswa,
                isRefreshing = uiState.isRefreshing,
                onWithdrawClick = onWithdrawClick
            )
        }
        item {
            SummaryGrid(summary = uiState.summary, isMahasiswa = uiState.isMahasiswa)
        }
        item {
            FilterRow(
                selected = uiState.selectedFilter,
                isMahasiswa = uiState.isMahasiswa,
                onSelected = onFilterSelected
            )
        }
        if (uiState.filteredTransactions.isEmpty()) {
            item { EmptyState() }
        } else {
            items(uiState.filteredTransactions, key = { it.id }) { transaction ->
                TransactionCard(transaction = transaction, onClick = { onTransactionClick(transaction) })
            }
        }
        if (uiState.isMahasiswa && uiState.withdrawals.isNotEmpty()) {
            item { SectionTitle("Permintaan Penarikan") }
            items(uiState.withdrawals, key = { "withdrawal-${it.id}" }) { withdrawal ->
                WithdrawalCard(withdrawal)
            }
        }
    }
}

@Composable
private fun BalanceCard(
    summary: WalletSummary?,
    isMahasiswa: Boolean,
    isRefreshing: Boolean,
    onWithdrawClick: () -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(BrandNavy.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Lucide.CircleDollarSign, contentDescription = null, tint = BrandNavy)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isMahasiswa) "Saldo Dompet" else "Saldo Refund",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                    Text(
                        text = formatMoney(summary?.balance),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryText
                    )
                }
                if (isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = BrandNavy)
                }
            }
            if (isMahasiswa) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bisa ditarik", style = MaterialTheme.typography.labelMedium, color = SecondaryText)
                        Text(
                            text = formatMoney(summary?.availableForWithdraw),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = BrandNavy
                        )
                    }
                    TextButton(onClick = onWithdrawClick) {
                        Text("Tarik Dana", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryGrid(summary: WalletSummary?, isMahasiswa: Boolean) {
    val items = buildList {
        add((if (isMahasiswa) "Bulan Ini" else "Refund Bulan Ini") to formatMoney(summary?.incomeThisMonth))
        add("Di Escrow" to formatMoney(summary?.heldInEscrow))
        if (isMahasiswa) add("Sudah Ditarik" to formatMoney(summary?.totalWithdrawn))
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { (label, value) ->
                    MiniSummaryCard(label, value, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MiniSummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier, shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = SecondaryText)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterRow(
    selected: WalletFilter,
    isMahasiswa: Boolean,
    onSelected: (WalletFilter) -> Unit
) {
    val filters = if (isMahasiswa) {
        WalletFilter.entries
    } else {
        WalletFilter.entries.filterNot { it == WalletFilter.WITHDRAWALS }
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        filters.forEach { filter ->
            val active = selected == filter
            Text(
                text = filter.label,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (active) BrandNavy else White)
                    .clickable { onSelected(filter) }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                color = if (active) White else PrimaryText,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun TransactionCard(transaction: WalletTransaction, onClick: () -> Unit) {
    val accent = transactionColor(transaction)
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !transaction.pesananId.isNullOrBlank()) { onClick() },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            WalletIcon(icon = transactionIcon(transaction), color = accent)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.title.ifBlank { transactionLabel(transaction.type) },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    transaction.counterpartyName.ifBlank { formatDate(transaction.createdAt) },
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                StatusChip(transaction.status)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                val sign = if (transaction.direction == WalletTransactionDirection.OUT) "-" else "+"
                Text(
                    "$sign${formatMoney(transaction.amount)}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = accent
                )
                Text(formatDate(transaction.createdAt), style = MaterialTheme.typography.labelSmall, color = SecondaryText)
            }
        }
    }
}

@Composable
private fun WithdrawalCard(withdrawal: Withdrawal) {
    AppCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            WalletIcon(icon = Lucide.ArrowUpRight, color = BrandNavy)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(withdrawal.bankName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Text(withdrawal.accountNumber, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                Spacer(modifier = Modifier.height(8.dp))
                StatusChip(withdrawal.status)
            }
            Text(formatMoney(withdrawal.amount), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun WalletIcon(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun StatusChip(status: WalletTransactionStatus) {
    val (label, color) = when (status) {
        WalletTransactionStatus.SUCCESS -> "Sukses" to SuccessGreen
        WalletTransactionStatus.PENDING -> "Menunggu" to StarYellow
        WalletTransactionStatus.APPROVED -> "Disetujui" to AccentBlue
        WalletTransactionStatus.PAID -> "Dibayar" to SuccessGreen
        WalletTransactionStatus.REJECTED -> "Ditolak" to ErrorRed
        WalletTransactionStatus.CANCELLED -> "Dibatalkan" to SecondaryText
        WalletTransactionStatus.UNKNOWN -> "Status tidak dikenal" to SecondaryText
    }
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        color = color,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryText)
}

@Composable
private fun EmptyState() {
    AppCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
        Text(
            "Belum ada transaksi refund.",
            modifier = Modifier.padding(22.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WithdrawalBottomSheet(
    uiState: WalletUiState,
    onDismiss: () -> Unit,
    onAmountChanged: (String) -> Unit,
    onBankNameChanged: (String) -> Unit,
    onAccountNumberChanged: (String) -> Unit,
    onAccountHolderNameChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier,
    sheetState: androidx.compose.material3.SheetState
) {
    val form = uiState.withdrawalForm
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = White,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Tarik Dana", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text(
                "Maksimal ${formatMoney(uiState.summary?.availableForWithdraw)}",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText
            )
            WalletTextField(
                value = form.amount,
                onValueChange = onAmountChanged,
                label = "Jumlah",
                keyboardType = KeyboardType.Decimal
            )
            WalletTextField(value = form.bankName, onValueChange = onBankNameChanged, label = "Nama Bank")
            WalletTextField(
                value = form.accountNumber,
                onValueChange = onAccountNumberChanged,
                label = "Nomor Rekening",
                keyboardType = KeyboardType.Number
            )
            WalletTextField(value = form.accountHolderName, onValueChange = onAccountHolderNameChanged, label = "Nama Pemilik Rekening")
            WalletTextField(value = form.note, onValueChange = onNoteChanged, label = "Catatan (opsional)")
            form.errorMessage?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = ErrorRed)
            }
            AppPrimaryButton(
                text = "Kirim Permintaan",
                onClick = onSubmit,
                isLoading = uiState.isSubmittingWithdrawal
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WalletTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(8.dp)
    )
}

private fun transactionIcon(transaction: WalletTransaction): ImageVector =
    when (transaction.type) {
        WalletTransactionType.WITHDRAWAL -> Lucide.ArrowUpRight
        WalletTransactionType.ESCROW -> Lucide.Clock
        WalletTransactionType.REFUND -> Lucide.ArrowDownLeft
        WalletTransactionType.CREDIT -> Lucide.CircleDollarSign
        WalletTransactionType.UNKNOWN -> Lucide.ReceiptText
    }

private fun transactionColor(transaction: WalletTransaction): Color =
    when {
        transaction.direction == WalletTransactionDirection.OUT -> ErrorRed
        transaction.type == WalletTransactionType.ESCROW -> StarYellow
        transaction.type == WalletTransactionType.REFUND -> AccentBlue
        else -> SuccessGreen
    }

private fun transactionLabel(type: WalletTransactionType): String =
    when (type) {
        WalletTransactionType.CREDIT -> "Pemasukan"
        WalletTransactionType.REFUND -> "Refund"
        WalletTransactionType.ESCROW -> "Dana Escrow"
        WalletTransactionType.WITHDRAWAL -> "Penarikan"
        WalletTransactionType.UNKNOWN -> "Transaksi"
    }

private fun formatMoney(value: String?): String {
    val amount = value?.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }
    return "Rp ${formatter.format(amount)}"
}

private fun formatDate(value: String): String =
    runCatching {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
        Instant.parse(value).atZone(ZoneId.systemDefault()).format(formatter)
    }.getOrDefault(value.take(10))
