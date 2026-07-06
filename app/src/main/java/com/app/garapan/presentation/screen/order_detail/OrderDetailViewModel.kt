package com.app.garapan.presentation.screen.order_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.ActiveOrder
import com.app.garapan.domain.model.DisputeOutcome
import com.app.garapan.domain.model.DisputeOutcomeResolver
import com.app.garapan.domain.model.Laporan
import com.app.garapan.domain.model.LaporanStatus
import com.app.garapan.domain.model.Pesanan
import com.app.garapan.domain.model.PesananStatus
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.CreatePaymentTokenParams
import com.app.garapan.domain.usecase.CancelPesananUseCase
import com.app.garapan.domain.usecase.CompletePesananUseCase
import com.app.garapan.domain.usecase.CreatePaymentTokenUseCase
import com.app.garapan.domain.usecase.DeliverPesananUseCase
import com.app.garapan.domain.usecase.GetPesananDetailUseCase
import com.app.garapan.domain.usecase.GetReviewByPesananUseCase
import com.app.garapan.domain.usecase.MarkMatchingNotificationsReadUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import com.app.garapan.domain.usecase.WaitForPesananPaymentUseCase
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.presentation.notification.NotificationRefreshNotifier
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
    val canCancel: Boolean = false,
    val canDispute: Boolean = false,
    val canChat: Boolean = false,
    val conversationId: String? = null,
    val showDisputedInfoBanner: Boolean = false,
    val disputedBannerTitle: String = "",
    val disputedBannerMessage: String = "",
    val showDisputeStatusCard: Boolean = false,
    val disputeCardTitle: String = "",
    val disputeCardReason: String = "",
    val disputeCardStatusLabel: String = "",
    val disputeCardResolutionNote: String? = null,
    val disputeCardOutcomeLabel: String? = null,
    val disputeCardResolvedAt: String? = null,
    val showWalletLink: Boolean = false,
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
    private val cancelPesananUseCase: CancelPesananUseCase,
    private val createPaymentTokenUseCase: CreatePaymentTokenUseCase,
    private val waitForPesananPaymentUseCase: WaitForPesananPaymentUseCase,
    private val getReviewByPesananUseCase: GetReviewByPesananUseCase,
    private val markMatchingNotificationsReadUseCase: MarkMatchingNotificationsReadUseCase,
    private val notificationRefreshNotifier: NotificationRefreshNotifier,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private val pesananId: String = savedStateHandle["pesananId"] ?: ""
    private var currentRole: Role? = null
    private var currentUserId: String? = null
    private var currentPesanan: Pesanan? = null
    private var hasDismissedRelatedNotifications = false
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
                currentPesanan?.let { pesanan ->
                    applyPesanan(pesanan)
                    if (canReview(pesanan.status, pesanan)) {
                        loadExistingReview(pesanan.id, pesanan.status)
                    }
                }
            }
        }
        loadDetail()
    }

    fun retry() = loadDetail()

    fun refresh() = loadDetail(silent = true)

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
                                "Pembayaran belum dikonfirmasi. Coba Lanjutkan Pembayaran jika belum selesai."
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
                            "Gagal memuat status pembayaran. Buka kembali halaman ini untuk mencoba lagi."
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
                    _events.emit(OrderDetailEvent.NavigateToReview(result.data.id))
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

    fun onCancelClicked() {
        if (pesananId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, actionMessage = null, errorMessage = null) }
            when (val result = cancelPesananUseCase(pesananId)) {
                is Resource.Success -> {
                    applyPesanan(result.data)
                    _events.emit(OrderDetailEvent.ShowMessage("Pesanan dibatalkan."))
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

    private fun loadDetail(silent: Boolean = false) {
        if (pesananId.isBlank()) {
            _uiState.value = OrderDetailUiState(
                isLoading = false,
                errorMessage = "Pesanan tidak ditemukan."
            )
            return
        }

        viewModelScope.launch {
            val keepStale = silent && currentPesanan != null
            _uiState.update { it.copy(isLoading = !keepStale, errorMessage = null) }
            when (val result = getPesananDetailUseCase(pesananId)) {
                is Resource.Success -> {
                    applyPesanan(result.data)
                    loadExistingReview(result.data.id, result.data.status)
                    dismissRelatedOrderNotificationsOnce()
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

    private fun dismissRelatedOrderNotificationsOnce() {
        if (hasDismissedRelatedNotifications || pesananId.isBlank()) return
        hasDismissedRelatedNotifications = true
        viewModelScope.launch {
            markMatchingNotificationsReadUseCase { notification ->
                notification.meta?.pesananId == pesananId
            }
            notificationRefreshNotifier.requestRefresh()
        }
    }

    private suspend fun loadExistingReview(pesananId: String, status: PesananStatus) {
        if (!canReview(status, currentPesanan)) return
        when (val result = getReviewByPesananUseCase(pesananId)) {
            is Resource.Success -> {
                _uiState.update {
                    it.copy(
                        existingReviewId = result.data.id,
                        reviewButtonLabel = "Edit Ulasan"
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
                canReview = canReview(pesanan.status, pesanan),
                existingReviewId = null,
                reviewButtonLabel = "Beri Ulasan",
                canPay = canPay(pesanan.status, isBuyer),
                canCancel = canCancel(pesanan.status, isBuyer),
                canDispute = OrderDisputeEligibility.canDispute(
                    status = pesanan.status,
                    isParticipant = isBuyer || isProvider
                ),
                canChat = !pesanan.conversationId.isNullOrBlank(),
                conversationId = pesanan.conversationId,
                showDisputedInfoBanner = OrderDisputeEligibility.showDisputedInfoBanner(pesanan.status),
                disputedBannerTitle = DisputeDisplayMapper.bannerTitle(pesanan.status),
                disputedBannerMessage = DisputeDisplayMapper.bannerMessage(
                    laporan = pesanan.laporan,
                    currentUserId = currentUserId
                ),
                showDisputeStatusCard = DisputeDisplayMapper.showStatusCard(pesanan.status, pesanan.laporan),
                disputeCardTitle = DisputeDisplayMapper.cardTitle(pesanan.laporan, currentUserId),
                disputeCardReason = pesanan.laporan?.reason.orEmpty(),
                disputeCardStatusLabel = DisputeDisplayMapper.statusLabel(pesanan.laporan?.status),
                disputeCardResolutionNote = pesanan.laporan?.resolutionNote,
                disputeCardOutcomeLabel = DisputeDisplayMapper.outcomeLabel(
                    laporan = pesanan.laporan,
                    orderStatus = pesanan.status,
                    isBuyer = isBuyer
                ),
                disputeCardResolvedAt = pesanan.laporan?.resolvedAt?.let(PesananDisplayMapper::formatOrderDate),
                showWalletLink = DisputeDisplayMapper.showWalletLink(pesanan.laporan, pesanan.status),
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

    private fun canReview(status: PesananStatus, pesanan: Pesanan?): Boolean =
        OrderReviewEligibility.canReview(
            status = status,
            isBuyer = pesanan?.isBuyerForCurrentUser() == true
        )

    private fun canPay(status: PesananStatus, isBuyer: Boolean): Boolean =
        isBuyer && status == PesananStatus.PENDING

    private fun canCancel(status: PesananStatus, isBuyer: Boolean): Boolean =
        isBuyer && status == PesananStatus.PENDING

    private fun Pesanan.isBuyerForCurrentUser(): Boolean {
        return OrderParticipantResolver.isBuyer(
            currentUserId = currentUserId,
            currentRole = currentRole,
            clientUserId = clientUserId,
            workerUserId = workerUserId
        )
    }

    private fun Pesanan.isProviderForCurrentUser(): Boolean {
        return OrderParticipantResolver.isProvider(
            currentUserId = currentUserId,
            currentRole = currentRole,
            clientUserId = clientUserId,
            workerUserId = workerUserId
        )
    }
}

object OrderParticipantResolver {
    fun isBuyer(
        currentUserId: String?,
        currentRole: Role?,
        clientUserId: String?,
        workerUserId: String?
    ): Boolean {
        if (currentUserId.isNullOrBlank()) return currentRole == Role.KLIEN
        if (clientUserId == currentUserId) return true
        if (workerUserId == currentUserId) return false
        return currentRole == Role.KLIEN && clientUserId.isNullOrBlank()
    }

    fun isProvider(
        currentUserId: String?,
        currentRole: Role?,
        clientUserId: String?,
        workerUserId: String?
    ): Boolean {
        if (currentUserId.isNullOrBlank()) return currentRole == Role.MAHASISWA
        if (workerUserId == currentUserId) return true
        if (clientUserId == currentUserId) return false
        return currentRole == Role.MAHASISWA && workerUserId.isNullOrBlank()
    }
}

object OrderReviewEligibility {
    fun canReview(status: PesananStatus, isBuyer: Boolean): Boolean =
        isBuyer && status == PesananStatus.COMPLETED
}

object OrderDisputeEligibility {
    fun canDispute(status: PesananStatus, isParticipant: Boolean): Boolean =
        isParticipant && (status == PesananStatus.IN_PROGRESS || status == PesananStatus.DELIVERED)

    fun showDisputedInfoBanner(status: PesananStatus): Boolean =
        status == PesananStatus.DISPUTED
}

object DisputeDisplayMapper {
    fun bannerTitle(status: PesananStatus): String =
        if (status == PesananStatus.DISPUTED) "Pesanan dalam sengketa" else ""

    fun bannerMessage(laporan: Laporan?, currentUserId: String?): String {
        if (laporan == null) {
            return "Menunggu keputusan admin."
        }
        return when {
            laporan.reporterId == currentUserId ->
                "Sengketa Anda sedang ditinjau admin. Dana escrow ditahan sementara."
            else ->
                "Pihak lain mengajukan sengketa. Menunggu keputusan admin."
        }
    }

    fun showStatusCard(status: PesananStatus, laporan: Laporan?): Boolean =
        laporan != null || status == PesananStatus.DISPUTED

    fun cardTitle(laporan: Laporan?, currentUserId: String?): String {
        if (laporan == null) return "Status Sengketa"
        return when {
            laporan.reporterId == currentUserId -> "Anda mengajukan sengketa"
            else -> "Pihak lain mengajukan sengketa"
        }
    }

    fun statusLabel(status: LaporanStatus?): String = when (status) {
        LaporanStatus.PENDING -> "Menunggu tinjauan admin"
        LaporanStatus.RESOLVED -> "Selesai"
        LaporanStatus.REJECTED -> "Ditolak admin"
        null -> "Menunggu tinjauan admin"
    }

    fun outcomeLabel(laporan: Laporan?, orderStatus: PesananStatus, isBuyer: Boolean): String? {
        val outcome = DisputeOutcomeResolver.resolveOutcome(
            laporanStatus = laporan?.status,
            orderStatus = orderStatus,
            refundAmount = laporan?.refundAmount
        ) ?: return null
        return when (outcome) {
            DisputeOutcome.RELEASE ->
                if (isBuyer) "Pesanan selesai — dana dicairkan ke freelancer"
                else "Dana masuk dompet Anda"
            DisputeOutcome.REFUND ->
                if (isBuyer) "Refund penuh ke dompet Anda"
                else "Pesanan dibatalkan — dana dikembalikan ke klien"
            DisputeOutcome.PARTIAL_REFUND -> {
                val refund = laporan?.refundAmount?.let { CurrencyFormatter.formatRupiah(it) }.orEmpty()
                if (isBuyer) "Refund sebagian $refund ke dompet Anda"
                else "Sisa dana dicairkan ke dompet Anda"
            }
            DisputeOutcome.REJECT -> "Sengketa ditolak — lanjutkan pesanan"
        }
    }

    fun showWalletLink(laporan: Laporan?, orderStatus: PesananStatus): Boolean {
        if (laporan?.status != LaporanStatus.RESOLVED) return false
        return orderStatus == PesananStatus.COMPLETED || orderStatus == PesananStatus.CANCELLED
    }
}
