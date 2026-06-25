package com.app.garapan.presentation.screen.checkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreatePaymentTokenParams
import com.app.garapan.domain.model.CreatePesananParams
import com.app.garapan.domain.model.PesananStatus
import com.app.garapan.domain.usecase.CreatePaymentTokenUseCase
import com.app.garapan.domain.usecase.CreatePesananUseCase
import com.app.garapan.domain.usecase.GetJasaDetailUseCase
import com.app.garapan.domain.usecase.WaitForPesananPaymentUseCase
import com.app.garapan.presentation.util.CurrencyFormatter
import com.app.garapan.presentation.util.UserMessageLocalizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val jasaId: String = "",
    val serviceName: String = "",
    val workerName: String = "",
    val servicePrice: String = "",
    val total: String = "",
    val imageUrl: String = "",
    val isLoading: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

sealed interface CheckoutEvent {
    data class LaunchSnapPayment(val snapToken: String, val pesananId: String) : CheckoutEvent
    data class NavigateToOrderDetail(val pesananId: String) : CheckoutEvent
    data class ShowMessage(val message: String) : CheckoutEvent
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getJasaDetailUseCase: GetJasaDetailUseCase,
    private val createPesananUseCase: CreatePesananUseCase,
    private val createPaymentTokenUseCase: CreatePaymentTokenUseCase,
    private val waitForPesananPaymentUseCase: WaitForPesananPaymentUseCase
) : ViewModel() {

    private val jasaId: String = savedStateHandle["jasaId"] ?: ""

    private val _uiState = MutableStateFlow(CheckoutUiState(jasaId = jasaId, isLoading = true))
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CheckoutEvent>()
    val events: SharedFlow<CheckoutEvent> = _events.asSharedFlow()

    private var pendingPesananId: String? = null
    private var awaitingPaymentReturn: Boolean = false

    init {
        loadJasa()
    }

    fun onPayNowClicked() {
        if (jasaId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Jasa tidak ditemukan.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessingPayment = true,
                    errorMessage = null,
                    statusMessage = "Membuat pesanan..."
                )
            }

            val pesanan = when (val createResult = createPesananUseCase(CreatePesananParams(jasaId))) {
                is Resource.Success -> createResult.data
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isProcessingPayment = false,
                            statusMessage = null,
                            errorMessage = UserMessageLocalizer.localize(createResult.message)
                        )
                    }
                    return@launch
                }
                Resource.Loading -> return@launch
            }

            pendingPesananId = pesanan.id
            _uiState.update { it.copy(statusMessage = "Menyiapkan pembayaran...") }

            when (val tokenResult = createPaymentTokenUseCase(CreatePaymentTokenParams(pesananId = pesanan.id))) {
                is Resource.Success -> {
                    awaitingPaymentReturn = true
                    _uiState.update {
                        it.copy(
                            isProcessingPayment = false,
                            statusMessage = "Menyelesaikan pembayaran di halaman Midtrans..."
                        )
                    }
                    _events.emit(
                        CheckoutEvent.LaunchSnapPayment(
                            snapToken = tokenResult.data.midtransToken,
                            pesananId = pesanan.id
                        )
                    )
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isProcessingPayment = false,
                            statusMessage = null,
                            errorMessage = UserMessageLocalizer.localize(tokenResult.message)
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onReturnedFromPayment() {
        if (!awaitingPaymentReturn) return
        val pesananId = pendingPesananId ?: return
        awaitingPaymentReturn = false

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessingPayment = true,
                    statusMessage = "Menunggu konfirmasi pembayaran..."
                )
            }
            when (val refresh = waitForPesananPaymentUseCase(pesananId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isProcessingPayment = false, statusMessage = null)
                    }
                    if (refresh.data.status == PesananStatus.PENDING) {
                        _events.emit(
                            CheckoutEvent.ShowMessage(
                                "Pembayaran belum dikonfirmasi. Buka detail pesanan dan ketuk Perbarui Status."
                            )
                        )
                    } else {
                        _events.emit(CheckoutEvent.ShowMessage("Pembayaran berhasil! Pesanan sedang diproses."))
                    }
                    _events.emit(CheckoutEvent.NavigateToOrderDetail(pesananId))
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isProcessingPayment = false, statusMessage = null)
                    }
                    _events.emit(
                        CheckoutEvent.ShowMessage(
                            "Gagal memuat status. Buka detail pesanan dan ketuk Perbarui Status."
                        )
                    )
                    _events.emit(CheckoutEvent.NavigateToOrderDetail(pesananId))
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun retry() = loadJasa()

    private fun loadJasa() {
        if (jasaId.isBlank()) {
            _uiState.update {
                it.copy(isLoading = false, errorMessage = "Jasa tidak ditemukan.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getJasaDetailUseCase(jasaId)) {
                is Resource.Success -> {
                    val jasa = result.data
                    val priceLabel = CurrencyFormatter.formatRupiah(jasa.price)
                    _uiState.update {
                        it.copy(
                            jasaId = jasa.id,
                            serviceName = jasa.title,
                            workerName = jasa.workerName.ifBlank { "Freelancer" },
                            servicePrice = priceLabel,
                            total = priceLabel,
                            imageUrl = jasa.imageUrl,
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
}
