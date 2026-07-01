package com.app.garapan.presentation.screen.order_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Pesanan
import com.app.garapan.domain.model.PesananStatus
import com.app.garapan.domain.model.ProjectProposal
import com.app.garapan.domain.model.ProposalStatus
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.usecase.GetMyPesananUseCase
import com.app.garapan.domain.usecase.GetMyProposalsUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import com.app.garapan.presentation.util.CurrencyFormatter
import com.app.garapan.presentation.util.PesananDisplayMapper
import com.app.garapan.presentation.util.UserMessageLocalizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

private const val HISTORY_PAGE_LIMIT = 100

data class OrderHistoryItem(
    val id: String,
    val title: String,
    val amount: String,
    val counterpartyName: String,
    val time: String,
    val status: String,
    val statusRaw: PesananStatus,
    val createdAtRaw: String,
    val isIncome: Boolean
)

enum class OrderHistoryTab {
    PESANAN,
    PROPOSAL
}

enum class OrderHistoryPeriod(val label: String) {
    HARI_INI("Hari Ini"),
    MINGGU_INI("Minggu Ini"),
    BULAN_INI("Bulan Ini"),
    SEMUA("Semua")
}

data class OrderHistoryFilterState(
    val period: OrderHistoryPeriod = OrderHistoryPeriod.BULAN_INI,
    val status: PesananStatus? = null
)

data class ProposalHistoryItem(
    val id: String,
    val projectId: String,
    val projectTitle: String,
    val proposedPrice: String,
    val status: ProposalStatus,
    val statusLabel: String
)

data class OrderHistoryUiState(
    val items: List<OrderHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isMahasiswa: Boolean = false,
    val selectedTab: OrderHistoryTab = OrderHistoryTab.PESANAN,
    val showFilterSheet: Boolean = false,
    val filter: OrderHistoryFilterState = OrderHistoryFilterState(),
    val proposals: List<ProposalHistoryItem> = emptyList(),
    val isLoadingProposals: Boolean = false,
    val proposalsErrorMessage: String? = null
)

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val getMyPesananUseCase: GetMyPesananUseCase,
    private val getMyProposalsUseCase: GetMyProposalsUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private var currentRole: Role? = null
    private var currentUserId: String? = null
    private var allItems: List<OrderHistoryItem> = emptyList()

    private val _uiState = MutableStateFlow(OrderHistoryUiState(isLoading = true))
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                currentRole = user?.role
                currentUserId = user?.id
                _uiState.update { it.copy(isMahasiswa = user?.role == Role.MAHASISWA) }
                if (user != null) {
                    loadOrders()
                }
            }
        }
    }

    fun retry() = loadOrders()

    fun refresh() {
        loadOrders(silent = true)
        if (_uiState.value.selectedTab == OrderHistoryTab.PROPOSAL) loadProposals(silent = true)
    }

    fun onTabSelected(tab: OrderHistoryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == OrderHistoryTab.PROPOSAL && _uiState.value.proposals.isEmpty()) loadProposals()
    }

    fun onFilterChipClicked() = _uiState.update { it.copy(showFilterSheet = true) }

    fun onDismissFilter() = _uiState.update { it.copy(showFilterSheet = false) }

    fun onPeriodSelected(period: OrderHistoryPeriod) =
        _uiState.update { it.copy(filter = it.filter.copy(period = period)) }

    fun onStatusSelected(status: PesananStatus?) =
        _uiState.update { it.copy(filter = it.filter.copy(status = status)) }

    fun onApplyFilter() {
        _uiState.update {
            it.copy(showFilterSheet = false, items = filterItems(allItems, it.filter))
        }
    }

    private fun loadOrders(silent: Boolean = false) {
        viewModelScope.launch {
            val keepStale = silent && _uiState.value.items.isNotEmpty()
            _uiState.update { it.copy(isLoading = !keepStale, errorMessage = null) }
            when (val result = getMyPesananUseCase(limit = HISTORY_PAGE_LIMIT)) {
                is Resource.Success -> {
                    allItems = result.data.map { pesanan -> pesanan.toHistoryItem(currentRole, currentUserId) }
                    _uiState.update {
                        it.copy(
                            items = filterItems(allItems, it.filter),
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = if (keepStale) null else UserMessageLocalizer.localize(result.message)
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun filterItems(
        source: List<OrderHistoryItem>,
        filter: OrderHistoryFilterState
    ): List<OrderHistoryItem> = source.filter { item ->
        matchesPeriod(item.createdAtRaw, filter.period) &&
            (filter.status == null || item.statusRaw == filter.status)
    }

    private fun matchesPeriod(isoDate: String, period: OrderHistoryPeriod): Boolean {
        if (period == OrderHistoryPeriod.SEMUA) return true
        val date = runCatching {
            Instant.parse(isoDate).atZone(ZoneId.systemDefault()).toLocalDate()
        }.getOrNull() ?: return false
        val today = LocalDate.now()
        return when (period) {
            OrderHistoryPeriod.HARI_INI -> date == today
            OrderHistoryPeriod.MINGGU_INI -> {
                val startOfWeek = today.with(DayOfWeek.MONDAY)
                !date.isBefore(startOfWeek) && !date.isAfter(today)
            }
            OrderHistoryPeriod.BULAN_INI -> date.year == today.year && date.month == today.month
            OrderHistoryPeriod.SEMUA -> true
        }
    }

    private fun loadProposals(silent: Boolean = false) {
        viewModelScope.launch {
            val keepStale = silent && _uiState.value.proposals.isNotEmpty()
            _uiState.update { it.copy(isLoadingProposals = !keepStale, proposalsErrorMessage = null) }
            when (val result = getMyProposalsUseCase(page = 1, limit = 50)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            proposals = result.data.map { proposal -> proposal.toHistoryItem() },
                            isLoadingProposals = false
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingProposals = false,
                            proposalsErrorMessage = if (keepStale) null else UserMessageLocalizer.localize(result.message)
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun Pesanan.toHistoryItem(role: Role?, userId: String?): OrderHistoryItem {
        val isProvider = isProviderFor(userId, role)
        val isBuyer = isBuyerFor(userId, role)
        val amountPrefix = if (isProvider) "+" else "-"
        return OrderHistoryItem(
            id = id,
            title = PesananDisplayMapper.orderTitle(jasaTitle, projectId),
            amount = "$amountPrefix${CurrencyFormatter.formatRupiah(totalPrice)}",
            counterpartyName = if (isBuyer) workerName else clientLabel,
            time = PesananDisplayMapper.formatOrderDate(createdAt),
            status = PesananDisplayMapper.statusLabel(status),
            statusRaw = status,
            createdAtRaw = createdAt,
            isIncome = isProvider
        )
    }

    private fun Pesanan.isBuyerFor(userId: String?, role: Role?): Boolean =
        !userId.isNullOrBlank() && clientUserId == userId ||
            (userId.isNullOrBlank() && role == Role.KLIEN)

    private fun Pesanan.isProviderFor(userId: String?, role: Role?): Boolean =
        !userId.isNullOrBlank() && workerUserId == userId ||
            (userId.isNullOrBlank() && role == Role.MAHASISWA)

    private fun ProjectProposal.toHistoryItem(): ProposalHistoryItem = ProposalHistoryItem(
        id = id,
        projectId = projectId,
        projectTitle = projectTitle.ifBlank { "Proyek" },
        proposedPrice = CurrencyFormatter.formatRupiah(proposedPrice),
        status = status,
        statusLabel = status.toLabel()
    )

    private fun ProposalStatus.toLabel(): String = when (this) {
        ProposalStatus.PENDING -> "Menunggu"
        ProposalStatus.ACCEPTED -> "Diterima"
        ProposalStatus.REJECTED -> "Ditolak"
        ProposalStatus.WITHDRAWN -> "Ditarik"
    }
}
