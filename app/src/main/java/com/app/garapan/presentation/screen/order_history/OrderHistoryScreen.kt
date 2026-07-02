package com.app.garapan.presentation.screen.order_history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ArrowUpRight
import com.composables.icons.lucide.ArrowDownLeft
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.X
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.app.garapan.domain.model.PesananStatus
import com.app.garapan.domain.model.ProposalStatus
import com.app.garapan.presentation.components.AppCard
import com.app.garapan.presentation.components.AppLogoTopBar
import com.app.garapan.presentation.components.AppPrimaryButton
import com.app.garapan.presentation.components.AppTopBar
import com.app.garapan.presentation.components.PesananStatusBadge
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.ErrorRed
import com.app.garapan.ui.theme.LightGray
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    navController: NavController,
    showBackButton: Boolean = true,
    viewModel: OrderHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val lifecycleOwner = LocalLifecycleOwner.current

    if (uiState.showFilterSheet) {
        OrderHistoryFilterBottomSheet(
            filter = uiState.filter,
            sheetState = sheetState,
            onDismiss = viewModel::onDismissFilter,
            onPeriodSelected = viewModel::onPeriodSelected,
            onStatusSelected = viewModel::onStatusSelected,
            onApply = viewModel::onApplyFilter
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(containerColor = Surface) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            if (showBackButton) {
                AppTopBar(
                    title = "Riwayat Pesanan",
                    onBack = { navController.navigateUp() }
                )
            } else {
                AppLogoTopBar(title = "Riwayat Pesanan")
            }
            if (uiState.isMahasiswa) {
                OrderHistoryTabRow(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = viewModel::onTabSelected
                )
            }
            if (uiState.isMahasiswa && uiState.selectedTab == OrderHistoryTab.PROPOSAL) {
                ProposalHistoryContent(
                    uiState = uiState,
                    onRetry = { viewModel.onTabSelected(OrderHistoryTab.PROPOSAL) },
                    onProposalClick = { projectId ->
                        navController.navigate(Routes.projectDetailRoute(projectId))
                    }
                )
                return@Column
            }
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandNavy)
                    }
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
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
                    OrderHistoryFilterChip(
                        filter = uiState.filter,
                        onClick = viewModel::onFilterChipClicked
                    )
                    if (uiState.items.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada pesanan.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 14.dp,
                                end = 14.dp,
                                top = 14.dp,
                                bottom = 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(uiState.items, key = { it.id }) { item ->
                                OrderHistoryCard(
                                    item = item,
                                    onClick = {
                                        navController.navigate(Routes.orderDetailRoute(item.id))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryTabRow(
    selectedTab: OrderHistoryTab,
    onTabSelected: (OrderHistoryTab) -> Unit
) {
    val tabs = listOf(
        OrderHistoryTab.PESANAN to "Pesanan",
        OrderHistoryTab.PROPOSAL to "Proposal"
    )
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        tabs.forEachIndexed { index, (tab, label) ->
            SegmentedButton(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = BrandNavy,
                    activeContentColor = White,
                    activeBorderColor = BrandNavy,
                    inactiveContainerColor = LightGray,
                    inactiveContentColor = SecondaryText,
                    inactiveBorderColor = LightGray
                ),
                icon = {},
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
        }
    }
}

@Composable
private fun ProposalHistoryContent(
    uiState: OrderHistoryUiState,
    onRetry: () -> Unit,
    onProposalClick: (String) -> Unit
) {
    when {
        uiState.isLoadingProposals -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandNavy)
            }
        }
        uiState.proposalsErrorMessage != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.proposalsErrorMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Text("Coba Lagi")
                }
            }
        }
        uiState.proposals.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Belum ada proposal yang diajukan.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                )
            }
        }
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(uiState.proposals, key = { it.id }) { proposal ->
                    ProposalHistoryCard(
                        item = proposal,
                        onClick = { onProposalClick(proposal.projectId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProposalHistoryCard(
    item: ProposalHistoryItem,
    onClick: () -> Unit
) {
    val statusColor = when (item.status) {
        ProposalStatus.ACCEPTED -> AccentBlue
        ProposalStatus.REJECTED -> ErrorRed
        ProposalStatus.WITHDRAWN -> SecondaryText
        ProposalStatus.PENDING -> BrandNavy
    }
    AppCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = item.projectTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = item.proposedPrice,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.statusLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(statusColor)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun OrderHistoryFilterChip(
    filter: OrderHistoryFilterState,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, top = 16.dp)
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(50.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(50.dp))
            .background(LightGray)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = Lucide.Calendar,
            contentDescription = null,
            tint = SecondaryText,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = filter.period.label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SecondaryText
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Lucide.ChevronDown,
            contentDescription = "Buka filter periode",
            tint = SecondaryText,
            modifier = Modifier.size(14.dp)
        )
        if (filter.status != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(AccentBlue)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun OrderHistoryFilterBottomSheet(
    filter: OrderHistoryFilterState,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onPeriodSelected: (OrderHistoryPeriod) -> Unit,
    onStatusSelected: (PesananStatus?) -> Unit,
    onApply: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Pesanan",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    ),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LightGray)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Lucide.X,
                        contentDescription = "Tutup",
                        tint = PrimaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FilterSectionLabel(text = "PERIODE")
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OrderHistoryPeriod.entries.forEach { period ->
                    FilterChipItem(
                        label = period.label,
                        selected = filter.period == period,
                        onClick = { onPeriodSelected(period) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FilterSectionLabel(text = "STATUS")
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipItem(
                    label = "Semua",
                    selected = filter.status == null,
                    onClick = { onStatusSelected(null) }
                )
                PesananStatus.entries.forEach { status ->
                    FilterChipItem(
                        label = status.filterLabel(),
                        selected = filter.status == status,
                        onClick = { onStatusSelected(status) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppPrimaryButton(
                text = "Terapkan",
                onClick = onApply
            )
        }
    }
}

@Composable
private fun FilterSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            color = SecondaryText,
            fontWeight = FontWeight.SemiBold
        )
    )
}

@Composable
private fun FilterChipItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) BrandNavy else White)
            .border(
                width = 1.dp,
                color = if (selected) BrandNavy else BorderColor,
                shape = RoundedCornerShape(50.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (selected) White else PrimaryText,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}

private fun PesananStatus.filterLabel(): String = when (this) {
    PesananStatus.PENDING -> "Menunggu Bayar"
    PesananStatus.PAID -> "Dibayar"
    PesananStatus.IN_PROGRESS -> "Diproses"
    PesananStatus.DELIVERED -> "Dikirim"
    PesananStatus.COMPLETED -> "Selesai"
    PesananStatus.DISPUTED -> "Dispute"
    PesananStatus.CANCELLED -> "Dibatalkan"
}

@Composable
private fun OrderHistoryCard(
    item: OrderHistoryItem,
    onClick: () -> Unit
) {
    val amountColor = if (item.isIncome) BrandNavy else ErrorRed
    val iconColor = if (item.isIncome) AccentBlue else ErrorRed
    val iconBg = if (item.isIncome) AccentBlue.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (item.isIncome) Lucide.ArrowDownLeft else Lucide.ArrowUpRight,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = item.amount,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = amountColor
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${item.counterpartyName}  •  ${item.time}",
                style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
            )
            Spacer(modifier = Modifier.height(8.dp))
            PesananStatusBadge(status = item.status)
        }
    }
}
