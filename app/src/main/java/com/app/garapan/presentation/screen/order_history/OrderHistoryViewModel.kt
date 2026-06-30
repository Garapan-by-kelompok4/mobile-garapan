package com.app.garapan.presentation.screen.order_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Pesanan
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.usecase.GetMyPesananUseCase
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
import javax.inject.Inject

data class OrderHistoryItem(
    val id: String,
    val title: String,
    val amount: String,
    val counterpartyName: String,
    val time: String,
    val status: String,
    val isIncome: Boolean
)

data class OrderHistoryUiState(
    val items: List<OrderHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val getMyPesananUseCase: GetMyPesananUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private var currentRole: Role? = null
    private var currentUserId: String? = null

    private val _uiState = MutableStateFlow(OrderHistoryUiState(isLoading = true))
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                currentRole = user?.role
                currentUserId = user?.id
                if (user != null) {
                    loadOrders()
                }
            }
        }
    }

    fun retry() = loadOrders()

    fun refresh() = loadOrders()

    private fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getMyPesananUseCase()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            items = result.data.map { pesanan -> pesanan.toHistoryItem(currentRole, currentUserId) },
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UserMessageLocalizer.localize(result.message)
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
            isIncome = isProvider
        )
    }

    private fun Pesanan.isBuyerFor(userId: String?, role: Role?): Boolean =
        !userId.isNullOrBlank() && clientUserId == userId ||
            (userId.isNullOrBlank() && role == Role.KLIEN)

    private fun Pesanan.isProviderFor(userId: String?, role: Role?): Boolean =
        !userId.isNullOrBlank() && workerUserId == userId ||
            (userId.isNullOrBlank() && role == Role.MAHASISWA)
}
