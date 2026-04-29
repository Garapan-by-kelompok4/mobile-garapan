package com.app.garapan.presentation.screen.checkout

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class PaymentMethod { GOPAY, OVO, QRIS }

data class CheckoutUiState(
    val serviceName: String = "Pembuatan Website Company Profile Modern",
    val workerName: String = "Ahmad Sumbul",
    val servicePrice: String = "Rp 2.500.000",
    val platformFee: String = "Rp 125.000",
    val total: String = "Rp 2.625.000",
    val selectedMethod: PaymentMethod? = null
)

@HiltViewModel
class CheckoutViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun onPaymentMethodSelected(method: PaymentMethod) =
        _uiState.update { it.copy(selectedMethod = method) }
}
