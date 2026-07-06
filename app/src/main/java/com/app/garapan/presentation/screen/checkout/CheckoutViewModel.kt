package com.app.garapan.presentation.screen.checkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreatePaymentTokenParams
import com.app.garapan.domain.model.CreatePesananParams
import com.app.garapan.domain.model.Pesanan
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
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

private const val IDEMPOTENCY_KEY = "idempotencyKey"
private const val RESUME_ORDER_AGE_SECONDS = 30L

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
    private val idempotencyKey: String = savedStateHandle.get<String>(IDEMPOTENCY_KEY)
        ?: UUID.randomUUID().toString().also { savedStateHandle[IDEMPOTENCY_KEY] = it }

    private val _uiState = MutableStateFlow(CheckoutUiState(jasaId = jasaId, isLoading = true))
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CheckoutEvent>()
    val events: SharedFlow<CheckoutEvent> = _events.asSharedFlow()

    private var pendingPesananId: String? = null
    private var awaitingPaymentReturn: Boolean = false
    private var paymentInFlight: Boolean = false

    init {
        loadJasa()
    }

    fun onPayNowClicked() {
        if (jasaId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Jasa tidak ditemukan.") }
            return
        }
        if (paymentInFlight || _uiState.value.isProcessingPayment) return

        paymentInFlight = true
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessingPayment = true,
                    errorMessage = null,
                    statusMessage = "Membuat pesanan..."
                )
            }

            val pesanan = when (
                val createResult = createPesananUseCase(
                    CreatePesananParams(jasaId = jasaId, idempotencyKey = idempotencyKey)
                )
            ) {
                is Resource.Success -> createResult.data
                is Resource.Error -> {
                    clearPaymentInFlight()
                    _uiState.update {
                        it.copy(
                            errorMessage = UserMessageLocalizer.localize(createResult.message)
                        )
                    }
                    return@launch
                }
                Resource.Loading -> return@launch
            }

            pendingPesananId = pesanan.id
            val statusMessage = if (isResumingExistingOrder(pesanan)) {
                "Melanjutkan pembayaran pesanan yang ada..."
            } else {
                "Menyiapkan pembayaran..."
            }
            _uiState.update { it.copy(statusMessage = statusMessage) }

            when (val tokenResult = createPaymentTokenUseCase(CreatePaymentTokenParams(pesananId = pesanan.id))) {
                is Resource.Success -> {
                    awaitingPaymentReturn = true
                    _uiState.update {
                        it.copy(statusMessage = "Menyelesaikan pembayaran di halaman Midtrans...")
                    }
                    _events.emit(
                        CheckoutEvent.LaunchSnapPayment(
                            snapToken = tokenResult.data.midtransToken,
                            pesananId = pesanan.id
                        )
                    )
                }
                is Resource.Error -> {
                    clearPaymentInFlight()
                    _uiState.update {
                        it.copy(
                            statusMessage = null,
                            errorMessage = UserMessageLocalizer.localize(tokenResult.message)
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onPaymentLaunchFailed() {
        if (!awaitingPaymentReturn) return
        awaitingPaymentReturn = false
        clearPaymentInFlight()
        _uiState.update {
            it.copy(
                statusMessage = null,
                errorMessage = "Tidak dapat membuka pembayaran."
            )
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
                    clearPaymentInFlight()
                    _uiState.update { it.copy(statusMessage = null) }
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
                    clearPaymentInFlight()
                    _uiState.update { it.copy(statusMessage = null) }
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

    private fun clearPaymentInFlight() {
        paymentInFlight = false
        _uiState.update { it.copy(isProcessingPayment = false) }
    }

    private fun isResumingExistingOrder(pesanan: Pesanan): Boolean {
        if (pesanan.status != PesananStatus.PENDING) return false
        val createdAt = runCatching { Instant.parse(pesanan.createdAt) }.getOrNull() ?: return false
        return Duration.between(createdAt, Instant.now()).seconds > RESUME_ORDER_AGE_SECONDS
    }

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
