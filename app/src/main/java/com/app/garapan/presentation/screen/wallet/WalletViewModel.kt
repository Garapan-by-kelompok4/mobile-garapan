package com.app.garapan.presentation.screen.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.WalletSummary
import com.app.garapan.domain.model.WalletTransaction
import com.app.garapan.domain.model.WalletTransactionType
import com.app.garapan.domain.model.Withdrawal
import com.app.garapan.domain.model.WithdrawalRequest
import com.app.garapan.domain.usecase.GetWalletSummaryUseCase
import com.app.garapan.domain.usecase.GetWalletTransactionsUseCase
import com.app.garapan.domain.usecase.GetWithdrawalsUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import com.app.garapan.domain.usecase.RequestWithdrawalUseCase
import com.app.garapan.presentation.util.CurrencyFormatter
import com.app.garapan.presentation.util.UserMessageLocalizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

private const val WALLET_PAGE_LIMIT = 50

enum class WalletFilter(val label: String, val type: WalletTransactionType?) {
    ALL("Semua", null),
    INCOME("Pemasukan", WalletTransactionType.CREDIT),
    ESCROW("Escrow", WalletTransactionType.ESCROW),
    WITHDRAWALS("Penarikan", WalletTransactionType.WITHDRAWAL)
}

data class WithdrawalFormState(
    val amount: String = "",
    val bankName: String = "",
    val accountNumber: String = "",
    val accountHolderName: String = "",
    val note: String = "",
    val errorMessage: String? = null
)

data class WalletUiState(
    val role: Role? = null,
    val summary: WalletSummary? = null,
    val transactions: List<WalletTransaction> = emptyList(),
    val withdrawals: List<Withdrawal> = emptyList(),
    val selectedFilter: WalletFilter = WalletFilter.ALL,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSubmittingWithdrawal: Boolean = false,
    val errorMessage: String? = null,
    val showWithdrawalSheet: Boolean = false,
    val withdrawalForm: WithdrawalFormState = WithdrawalFormState()
) {
    val isMahasiswa: Boolean = role == Role.MAHASISWA
    val filteredTransactions: List<WalletTransaction>
        get() = selectedFilter.type?.let { type -> transactions.filter { it.type == type } } ?: transactions
}

sealed interface WalletEvent {
    data class ShowMessage(val message: String) : WalletEvent
}

@HiltViewModel
class WalletViewModel @Inject constructor(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getWalletSummaryUseCase: GetWalletSummaryUseCase,
    private val getWalletTransactionsUseCase: GetWalletTransactionsUseCase,
    private val getWithdrawalsUseCase: GetWithdrawalsUseCase,
    private val requestWithdrawalUseCase: RequestWithdrawalUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<WalletEvent>()
    val events: SharedFlow<WalletEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                _uiState.update { it.copy(role = user?.role) }
                if (user != null) loadWallet()
            }
        }
    }

    fun retry() = loadWallet()

    fun refresh() = loadWallet(silent = true)

    fun onFilterSelected(filter: WalletFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun onOpenWithdrawalSheet() {
        if (!_uiState.value.isMahasiswa) return
        _uiState.update {
            it.copy(
                showWithdrawalSheet = true,
                withdrawalForm = WithdrawalFormState()
            )
        }
    }

    fun onDismissWithdrawalSheet() {
        if (_uiState.value.isSubmittingWithdrawal) return
        _uiState.update { it.copy(showWithdrawalSheet = false) }
    }

    fun onWithdrawalAmountChanged(value: String) =
        updateForm { it.copy(amount = CurrencyFormatter.formatRupiahInput(value), errorMessage = null) }
    fun onBankNameChanged(value: String) = updateForm { it.copy(bankName = value, errorMessage = null) }
    fun onAccountNumberChanged(value: String) = updateForm { it.copy(accountNumber = value, errorMessage = null) }
    fun onAccountHolderNameChanged(value: String) = updateForm { it.copy(accountHolderName = value, errorMessage = null) }
    fun onNoteChanged(value: String) = updateForm { it.copy(note = value, errorMessage = null) }

    fun submitWithdrawal() {
        val state = _uiState.value
        if (!state.isMahasiswa || state.isSubmittingWithdrawal) return
        val validation = validateWithdrawal(state.withdrawalForm, state.summary?.availableForWithdraw)
        if (validation != null) {
            updateForm { it.copy(errorMessage = validation) }
            return
        }

        viewModelScope.launch {
            val form = _uiState.value.withdrawalForm
            _uiState.update { it.copy(isSubmittingWithdrawal = true) }
            val request = WithdrawalRequest(
                amount = normalizeAmount(form.amount),
                bankName = form.bankName.trim(),
                accountNumber = form.accountNumber.trim(),
                accountHolderName = form.accountHolderName.trim(),
                note = form.note.trim().takeIf { it.isNotBlank() }
            )
            when (val result = requestWithdrawalUseCase(request)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingWithdrawal = false,
                            showWithdrawalSheet = false,
                            withdrawalForm = WithdrawalFormState()
                        )
                    }
                    _events.emit(WalletEvent.ShowMessage("Permintaan penarikan berhasil dikirim."))
                    loadWallet(silent = true)
                }
                is Resource.Error -> {
                    updateForm { it.copy(errorMessage = localizeWithdrawalError(result.message)) }
                    _uiState.update { it.copy(isSubmittingWithdrawal = false) }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadWallet(silent: Boolean = false) {
        viewModelScope.launch {
            val keepStale = silent && _uiState.value.summary != null
            _uiState.update {
                it.copy(
                    isLoading = !keepStale,
                    isRefreshing = keepStale,
                    errorMessage = null
                )
            }

            val summaryDeferred = async { getWalletSummaryUseCase() }
            val transactionsDeferred = async { getWalletTransactionsUseCase(limit = WALLET_PAGE_LIMIT) }
            val withdrawalsDeferred = if (_uiState.value.isMahasiswa) {
                async { getWithdrawalsUseCase(limit = WALLET_PAGE_LIMIT) }
            } else {
                null
            }

            val summaryResult = summaryDeferred.await()
            val transactionsResult = transactionsDeferred.await()
            val withdrawalsResult = withdrawalsDeferred?.await()

            val error = listOf(summaryResult, transactionsResult, withdrawalsResult)
                .filterIsInstance<Resource.Error>()
                .firstOrNull()

            if (error != null && !keepStale) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = UserMessageLocalizer.localize(error.message)
                    )
                }
                return@launch
            }

            _uiState.update { current ->
                current.copy(
                    summary = (summaryResult as? Resource.Success)?.data ?: current.summary,
                    transactions = (transactionsResult as? Resource.Success)?.data?.data ?: current.transactions,
                    withdrawals = (withdrawalsResult as? Resource.Success)?.data?.data ?: current.withdrawals,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null
                )
            }
        }
    }

    private fun updateForm(transform: (WithdrawalFormState) -> WithdrawalFormState) {
        _uiState.update { it.copy(withdrawalForm = transform(it.withdrawalForm)) }
    }

    private fun validateWithdrawal(form: WithdrawalFormState, available: String?): String? {
        val amount = CurrencyFormatter.parseRupiahAmount(form.amount)
            ?: return "Masukkan jumlah penarikan yang valid."
        if (amount <= BigDecimal.ZERO || amount.scale() > 2) return "Masukkan jumlah penarikan yang valid."
        val availableAmount = available?.toBigDecimalOrNull()
        if (availableAmount != null && amount > availableAmount) {
            return "Jumlah penarikan melebihi saldo yang tersedia."
        }
        if (form.bankName.isBlank() || form.accountNumber.isBlank() || form.accountHolderName.isBlank()) {
            return "Lengkapi informasi rekening."
        }
        return null
    }

    private fun normalizeAmount(value: String): String =
        CurrencyFormatter.parseRupiahAmount(value)?.setScale(2)?.toPlainString().orEmpty()

    private fun localizeWithdrawalError(message: String): String {
        val lower = message.lowercase()
        return when {
            "available" in lower || "saldo" in lower || "balance" in lower -> "Jumlah penarikan melebihi saldo yang tersedia."
            "amount" in lower -> "Masukkan jumlah penarikan yang valid."
            "bank" in lower || "account" in lower || "rekening" in lower -> "Lengkapi informasi rekening."
            else -> UserMessageLocalizer.localize(message).ifBlank { "Gagal memproses permintaan. Coba lagi." }
        }
    }
}
