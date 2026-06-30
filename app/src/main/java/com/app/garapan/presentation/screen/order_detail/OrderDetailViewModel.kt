package com.app.garapan.presentation.screen.order_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.ActiveOrder
import com.app.garapan.domain.model.Pesanan
import com.app.garapan.domain.model.PesananStatus
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.CreatePaymentTokenParams
import com.app.garapan.domain.usecase.CompletePesananUseCase
import com.app.garapan.domain.usecase.CreatePaymentTokenUseCase
import com.app.garapan.domain.usecase.DeliverPesananUseCase
import com.app.garapan.domain.usecase.GetPesananDetailUseCase
import com.app.garapan.domain.usecase.GetReviewsUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import com.app.garapan.domain.usecase.WaitForPesananPaymentUseCase
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.presentation.util.CurrencyFormatter
import com.app.garapan.presentation.util.PesananDisplayMapper
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

data class OrderDetailUiState(
    val id: String = "",
    val jasaId: String? = null,
    val title: String = "",
    val counterpartyName: String = "",
    val counterpartyLabel: String = "",
    val amount: String = "",
    val status: String = "",
    val statusRaw: PesananStatus = PesananStatus.PENDING,
    val createdAt: String = "",
    val canDeliver: Boolean = false,
    val canComplete: Boolean = false,
    val canReview: Boolean = false,
    val existingReviewId: String? = null,
    val reviewButtonLabel: String = "Beri Ulasan",
    val canPay: Boolean = false,
    val canDispute: Boolean = false,
    val canChat: Boolean = false,
    val conversationId: String? = null,
    val showDisputedInfoBanner: Boolean = false,
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val errorMessage: String? = null,
    val actionMessage: String? = null
)

sealed interface OrderDetailEvent {
    data class LaunchSnapPayment(val snapToken: String) : OrderDetailEvent
    data class NavigateToReview(val pesananId: String) : OrderDetailEvent
    data class NavigateToChat(val route: String) : OrderDetailEvent
    data class ShowMessage(val message: String) : OrderDetailEvent
}

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPesananDetailUseCase: GetPesananDetailUseCase,
    private val deliverPesananUseCase: DeliverPesananUseCase,
    private val completePesananUseCase: CompletePesananUseCase,
    private val createPaymentTokenUseCase: CreatePaymentTokenUseCase,
    private val waitForPesananPaymentUseCase: WaitForPesananPaymentUseCase,
    private val getReviewsUseCase: GetReviewsUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private val pesananId: String = savedStateHandle["pesananId"] ?: ""
    private var currentRole: Role? = null
    private var currentUserId: String? = null
    private var currentUserEmail: String? = null
    private var currentClientName: String? = null
    private var currentPesanan: Pesanan? = null
    private var awaitingPaymentReturn: Boolean = false

    private val _uiState = MutableStateFlow(OrderDetailUiState(isLoading = true))
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<OrderDetailEvent>()
    val events: SharedFlow<OrderDetailEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                currentRole = user?.role
                currentUserId = user?.id
                currentUserEmail = user?.email
                currentClientName = user?.klien?.companyName
                currentPesanan?.let { pesanan ->
                    applyPesanan(pesanan)
                    if (canReview(pesanan.status, pesanan.jasaId, pesanan)) {
                        loadExistingReview(pesanan.jasaId, pesanan.id, pesanan.status)
                    }
                }
            }
        }
        loadDetail()
    }

    fun retry() = loadDetail()

    fun refresh() = loadDetail()

    fun onRefreshStatusClicked() {
        if (pesananId.isBlank()) return
        if (_uiState.value.statusRaw != PesananStatus.PENDING) {
            loadDetail()
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(isActionLoading = true, actionMessage = "Memeriksa status pembayaran...", errorMessage = null)
            }
            when (val result = waitForPesananPaymentUseCase(pesananId, maxAttempts = 8, delayMs = 1_500L)) {
                is Resource.Success -> {
                    applyPesanan(result.data)
                    if (result.data.status == PesananStatus.PENDING) {
                        _events.emit(
                            OrderDetailEvent.ShowMessage(
                                "Pembayaran belum dikonfirmasi. Coba Lanjutkan Pembayaran jika belum selesai."
                            )
                        )
                    } else {
                        _events.emit(OrderDetailEvent.ShowMessage("Pembayaran berhasil dikonfirmasi."))
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            actionMessage = null,
                            errorMessage = UserMessageLocalizer.localize(result.message)
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onPayClicked() {
        if (pesananId.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(isActionLoading = true, actionMessage = "Menyiapkan pembayaran...", errorMessage = null)
            }
            when (val result = createPaymentTokenUseCase(CreatePaymentTokenParams(pesananId = pesananId))) {
                is Resource.Success -> {
                    awaitingPaymentReturn = true
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            actionMessage = "Menyelesaikan pembayaran di halaman Midtrans..."
                        )
                    }
                    _events.emit(OrderDetailEvent.LaunchSnapPayment(result.data.midtransToken))
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            actionMessage = null,
                            errorMessage = UserMessageLocalizer.localize(result.message)
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onReturnedFromPayment() {
        if (!awaitingPaymentReturn) return
        awaitingPaymentReturn = false
        viewModelScope.launch {
            _uiState.update {
                it.copy(isActionLoading = true, actionMessage = "Menunggu konfirmasi pembayaran...")
            }
            when (val refresh = waitForPesananPaymentUseCase(pesananId)) {
                is Resource.Success -> {
                    applyPesanan(refresh.data)
                    if (refresh.data.status == PesananStatus.PENDING) {
                        _events.emit(
                            OrderDetailEvent.ShowMessage(
                                "Pembayaran belum dikonfirmasi. Ketuk Perbarui Status atau Lanjutkan Pembayaran."
                            )
                        )
                    } else {
                        _events.emit(OrderDetailEvent.ShowMessage("Pembayaran berhasil dikonfirmasi."))
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isActionLoading = false, actionMessage = null) }
                    _events.emit(
                        OrderDetailEvent.ShowMessage(
                            "Gagal memuat status. Ketuk Perbarui Status untuk mencoba lagi."
                        )
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onDeliverClicked() {
        if (pesananId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, actionMessage = null, errorMessage = null) }
            when (val result = deliverPesananUseCase(pesananId)) {
                is Resource.Success -> {
                    applyPesanan(result.data)
                    _events.emit(OrderDetailEvent.ShowMessage("Pesanan ditandai sudah dikirim."))
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            errorMessage = UserMessageLocalizer.localize(result.message)
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onCompleteClicked() {
        if (pesananId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, actionMessage = null, errorMessage = null) }
            when (val result = completePesananUseCase(pesananId)) {
                is Resource.Success -> {
                    applyPesanan(result.data)
                    _events.emit(OrderDetailEvent.ShowMessage("Pesanan selesai. Dana telah dilepas."))
                    if (!result.data.jasaId.isNullOrBlank()) {
                        _events.emit(OrderDetailEvent.NavigateToReview(result.data.id))
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            errorMessage = UserMessageLocalizer.localize(result.message)
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onReviewClicked() {
        if (pesananId.isBlank()) return
        viewModelScope.launch {
            _events.emit(OrderDetailEvent.NavigateToReview(pesananId))
        }
    }

    private fun loadDetail() {
        if (pesananId.isBlank()) {
            _uiState.value = OrderDetailUiState(
                isLoading = false,
                errorMessage = "Pesanan tidak ditemukan."
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getPesananDetailUseCase(pesananId)) {
                is Resource.Success -> {
                    applyPesanan(result.data)
                    loadExistingReview(result.data.jasaId, result.data.id, result.data.status)
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

    private suspend fun loadExistingReview(jasaId: String?, pesananId: String, status: PesananStatus) {
        if (!canReview(status, jasaId, currentPesanan)) return
        when (val result = getReviewsUseCase(jasaId.orEmpty())) {
            is Resource.Success -> {
                val userId = currentUserId
                val reviewerLabels = listOfNotNull(currentUserEmail, currentClientName)
                    .filter { it.isNotBlank() }
                val existingReview = result.data.firstOrNull { review ->
                    review.pesananId == pesananId ||
                        (!userId.isNullOrBlank() && review.reviewerId == userId) ||
                        reviewerLabels.any { it == review.reviewerName }
                }
                _uiState.update {
                    it.copy(
                        existingReviewId = existingReview?.id,
                        reviewButtonLabel = if (existingReview != null) "Edit Ulasan" else "Beri Ulasan"
                    )
                }
            }
            is Resource.Error -> Unit
            Resource.Loading -> Unit
        }
    }

    fun onChatClicked() {
        val conversationId = _uiState.value.conversationId ?: return
        val counterpartyName = _uiState.value.counterpartyName
        val activeOrder = currentPesanan?.let { pesanan ->
            ActiveOrder(
                pesananId = pesanan.id,
                status = pesanan.status,
                title = PesananDisplayMapper.orderTitle(pesanan.jasaTitle, pesanan.projectId)
            )
        }
        viewModelScope.launch {
            _events.emit(
                OrderDetailEvent.NavigateToChat(
                    Routes.chatRoute(
                        conversationId = conversationId,
                        peerName = counterpartyName,
                        activeOrder = activeOrder
                    )
                )
            )
        }
    }

    private fun applyPesanan(pesanan: Pesanan) {
        currentPesanan = pesanan
        val isBuyer = pesanan.isBuyerForCurrentUser()
        val isProvider = pesanan.isProviderForCurrentUser()
        _uiState.update {
            it.copy(
                id = pesanan.id,
                jasaId = pesanan.jasaId,
                title = PesananDisplayMapper.orderTitle(pesanan.jasaTitle, pesanan.projectId),
                counterpartyName = if (isBuyer) pesanan.workerName else pesanan.clientLabel,
                counterpartyLabel = if (isBuyer) "Freelancer" else "Klien",
                amount = CurrencyFormatter.formatRupiah(pesanan.totalPrice),
                status = PesananDisplayMapper.statusLabel(pesanan.status),
                statusRaw = pesanan.status,
                createdAt = PesananDisplayMapper.formatOrderDate(pesanan.createdAt),
                canDeliver = canDeliver(pesanan.status, isProvider),
                canComplete = canComplete(pesanan.status, isBuyer),
                canReview = canReview(pesanan.status, pesanan.jasaId, pesanan),
                existingReviewId = null,
                reviewButtonLabel = "Beri Ulasan",
                canPay = canPay(pesanan.status, isBuyer),
                canDispute = canDispute(pesanan.status, isBuyer),
                canChat = !pesanan.conversationId.isNullOrBlank(),
                conversationId = pesanan.conversationId,
                showDisputedInfoBanner = showDisputedInfoBanner(pesanan.status, isProvider),
                isLoading = false,
                isActionLoading = false,
                errorMessage = null
            )
        }
    }

    private fun canDeliver(status: PesananStatus, isProvider: Boolean): Boolean =
        isProvider && status == PesananStatus.IN_PROGRESS

    private fun canComplete(status: PesananStatus, isBuyer: Boolean): Boolean =
        isBuyer && status == PesananStatus.DELIVERED

    private fun canReview(status: PesananStatus, jasaId: String?, pesanan: Pesanan?): Boolean =
        pesanan?.isBuyerForCurrentUser() == true && status == PesananStatus.COMPLETED && !jasaId.isNullOrBlank()

    private fun canPay(status: PesananStatus, isBuyer: Boolean): Boolean =
        isBuyer && status == PesananStatus.PENDING

    private fun canDispute(status: PesananStatus, isBuyer: Boolean): Boolean =
        isBuyer && (status == PesananStatus.IN_PROGRESS || status == PesananStatus.DELIVERED)

    private fun showDisputedInfoBanner(status: PesananStatus, isProvider: Boolean): Boolean =
        isProvider && status == PesananStatus.DISPUTED

    private fun Pesanan.isBuyerForCurrentUser(): Boolean {
        val userId = currentUserId
        return !userId.isNullOrBlank() && clientUserId == userId ||
            (userId.isNullOrBlank() && currentRole == Role.KLIEN)
    }

    private fun Pesanan.isProviderForCurrentUser(): Boolean {
        val userId = currentUserId
        return !userId.isNullOrBlank() && workerUserId == userId ||
            (userId.isNullOrBlank() && currentRole == Role.MAHASISWA)
    }
}
