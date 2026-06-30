package com.app.garapan.presentation.screen.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.ActiveOrder
import com.app.garapan.domain.model.OrderChatMessage
import com.app.garapan.domain.model.OrderChatPage
import com.app.garapan.domain.model.PesananStatus
import com.app.garapan.domain.model.SupportMessage
import com.app.garapan.domain.model.User
import com.app.garapan.domain.repository.SessionRepository
import com.app.garapan.domain.usecase.GetOrderMessagesUseCase
import com.app.garapan.domain.usecase.GetSupportThreadUseCase
import com.app.garapan.domain.usecase.MarkOrderChatReadUseCase
import com.app.garapan.domain.usecase.MarkSupportThreadReadUseCase
import com.app.garapan.domain.usecase.SendOrderMessageUseCase
import com.app.garapan.domain.usecase.SendSupportMessageUseCase
import com.app.garapan.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

sealed class ChatMessage {
    data class JasaCard(
        val title: String,
        val price: String,
        val time: String
    ) : ChatMessage()

    data class Sent(
        val text: String,
        val time: String
    ) : ChatMessage()

    data class Received(
        val text: String,
        val time: String,
        val senderInitials: String
    ) : ChatMessage()

    data class FileAndOrderConfirmation(
        val fileName: String,
        val fileSize: String,
        val serviceName: String,
        val servicePrice: String,
        val extras: String,
        val total: String,
        val time: String,
        val senderInitials: String
    ) : ChatMessage()
}

data class ChatUiState(
    val workerName: String = "",
    val workerInitials: String = "",
    val currentUserProfile: ChatCurrentUserProfile = ChatCurrentUserProfile(),
    val isOnline: Boolean = true,
    val isAdminSupport: Boolean = false,
    val supportLabel: String? = null,
    val showStatus: Boolean = true,
    val dateSeparator: String = "Hari ini",
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isLoadingOlder: Boolean = false,
    val hasMore: Boolean = false,
    val errorMessage: String? = null,
    val activeOrder: ActiveOrder? = null
)

data class ChatCurrentUserProfile(
    val initials: String = "?",
    val avatarUrl: String? = null
)

object ChatCurrentUserPresenter {
    fun from(user: User?): ChatCurrentUserProfile {
        val displayName = user.resolveDisplayName()
        return ChatCurrentUserProfile(
            initials = initialsOf(displayName),
            avatarUrl = user?.avatarUrl?.takeIf { it.isNotBlank() }
        )
    }

    private fun User?.resolveDisplayName(): String {
        if (this == null) return ""
        return displayName?.takeIf { it.isNotBlank() }
            ?: mahasiswa?.fullName?.takeIf { it.isNotBlank() }
            ?: klien?.companyName?.takeIf { it.isNotBlank() }
            ?: mahasiswa?.university?.takeIf { it.isNotBlank() }
            ?: email.substringBefore("@")
    }

    private fun initialsOf(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "?"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSupportThreadUseCase: GetSupportThreadUseCase,
    private val sendSupportMessageUseCase: SendSupportMessageUseCase,
    private val markSupportThreadReadUseCase: MarkSupportThreadReadUseCase,
    private val getOrderMessagesUseCase: GetOrderMessagesUseCase,
    private val sendOrderMessageUseCase: SendOrderMessageUseCase,
    private val markOrderChatReadUseCase: MarkOrderChatReadUseCase,
    sessionRepository: SessionRepository
) : ViewModel() {

    private val conversationId: String = savedStateHandle["conversationId"] ?: ""
    private val peerName: String = savedStateHandle["peerName"] ?: ""
    private val navActiveOrder: ActiveOrder? = parseNavActiveOrder(savedStateHandle)
    private val isSupportThread = conversationId == Routes.SUPPORT_WORKER_ID
    private val isPeerChat = !isSupportThread && conversationId.isNotBlank()
    private val currentUser: User? = sessionRepository.peekCurrentUser()
    private val currentUserId: String? = currentUser?.id
    private val currentUserProfile: ChatCurrentUserProfile = ChatCurrentUserPresenter.from(currentUser)

    private val initialState: ChatUiState = when {
        isSupportThread -> ChatUiState(
            workerName = "Bantuan Admin",
            workerInitials = "LS",
            isOnline = true,
            isAdminSupport = true,
            supportLabel = "Live Support",
            dateSeparator = "Hari ini",
            isLoading = true
        )
        isPeerChat -> ChatUiState(
            workerName = peerName.ifBlank { "Percakapan" },
            workerInitials = initialsOf(peerName),
            isOnline = true,
            showStatus = false,
            isLoading = true,
            activeOrder = navActiveOrder
        )
        else -> ChatUiState(
            workerName = peerName.ifBlank { "Percakapan" },
            workerInitials = initialsOf(peerName),
            isOnline = false,
            showStatus = false,
            errorMessage = "Percakapan tidak ditemukan."
        )
    }

    private val _uiState = MutableStateFlow(initialState.copy(currentUserProfile = currentUserProfile))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Number of items prepended to the top of the thread on the last load-older.
    // The screen collects this to keep the user's scroll anchored after older
    // history is inserted above the current viewport.
    private val _prependEvents = MutableSharedFlow<Int>(extraBufferCapacity = 8)
    val prependEvents: SharedFlow<Int> = _prependEvents.asSharedFlow()

    // `liveMessages` is the newest page kept fresh by polling; `olderMessages`
    // accumulates earlier pages fetched on scroll-up. They are merged, de-duped
    // by id and ordered oldest -> newest for display.
    private var liveMessages: List<SupportMessage> = emptyList()
    private var olderMessages: List<SupportMessage> = emptyList()
    private var oldestPageLoaded: Int = 1
    private var totalMessages: Int = 0

    // Newest page of an order thread, kept fresh by polling.
    private var orderMessages: List<OrderChatMessage> = emptyList()
    // Last message id we've reconciled, so we only send a read receipt when a
    // genuinely new incoming message arrives (not on every 4s poll).
    private var lastSeenOrderMessageId: String? = null

    private var pollJob: Job? = null

    init {
        when {
            isSupportThread -> loadSupportThread()
            isPeerChat -> loadPeerThread()
        }
    }

    fun onInputChanged(text: String) = _uiState.update { it.copy(inputText = text) }

    fun onSend() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return
        if (isSupportThread) {
            sendSupportMessage(text)
            return
        }
        if (isPeerChat) {
            sendPeerMessage(text)
            return
        }
        _uiState.update {
            it.copy(errorMessage = "Percakapan tidak ditemukan.")
        }
    }

    fun retry() {
        when {
            isSupportThread -> loadSupportThread()
            isPeerChat -> loadPeerThread()
        }
    }

    /** Starts silently polling the thread so new replies appear without leaving the screen. */
    fun startPolling() {
        if (!isSupportThread && !isPeerChat) return
        if (pollJob?.isActive == true) return
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(POLL_INTERVAL_MS)
                if (!_uiState.value.isSending) {
                    if (isSupportThread) {
                        fetchThread(showLoading = false, surfaceError = false)
                    } else {
                        fetchPeerThread(showLoading = false, surfaceError = false)
                    }
                }
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    private fun loadSupportThread() {
        viewModelScope.launch { fetchThread(showLoading = true, surfaceError = true) }
    }

    /** Loads the next older page and prepends it for reverse infinite scroll. */
    fun loadOlderMessages() {
        if (!isSupportThread) return
        val state = _uiState.value
        if (state.isLoadingOlder || !state.hasMore || state.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingOlder = true) }
            when (val result = getSupportThreadUseCase(page = oldestPageLoaded + 1, limit = PAGE_SIZE)) {
                is Resource.Success -> {
                    val page = result.data
                    olderMessages = dedupeById(olderMessages + page.messages)
                    oldestPageLoaded += 1
                    totalMessages = maxOf(page.total, olderMessages.size + liveMessages.size)
                    _uiState.update { it.copy(isLoadingOlder = false) }
                    rebuildThread(emitPrependCount = true)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoadingOlder = false, errorMessage = result.message) }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private suspend fun fetchThread(showLoading: Boolean, surfaceError: Boolean) {
        val hasRenderableMessages = _uiState.value.messages.isNotEmpty()
        if (showLoading && !hasRenderableMessages) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        } else if (surfaceError) {
            _uiState.update { it.copy(errorMessage = null) }
        }
        when (val result = getSupportThreadUseCase(page = 1, limit = PAGE_SIZE)) {
            is Resource.Success -> {
                val page = result.data
                liveMessages = page.messages
                totalMessages = maxOf(page.total, olderMessages.size + liveMessages.size)
                // Drive the header from the live tail (page 1): real agent name and
                // a connection status that reflects whether support is reachable.
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        workerName = page.agentName ?: it.workerName,
                        isOnline = page.supportOnline
                    )
                }
                rebuildThread(emitPrependCount = false)
                // The user is looking at the thread, so clear any unread admin
                // messages server-side; the inbox badge reflects this next refresh.
                if (page.unreadCount > 0) markThreadRead()
            }
            is Resource.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (surfaceError) result.message else it.errorMessage
                    )
                }
            }
            Resource.Loading -> Unit
        }
    }

    /**
     * Merge older history + live tail (de-duped by id, ordered oldest -> newest),
     * map to UI messages and publish. Setting an equal list is a no-op for
     * StateFlow, so polling that returns the same thread won't recompose.
     */
    private fun rebuildThread(emitPrependCount: Boolean) {
        val merged = dedupeById(olderMessages + liveMessages)
            .sortedBy { it.createdAt.toEpochMillisOrMax() }
        val mapped = merged.map { it.toChatMessage() }
        val previousSize = _uiState.value.messages.size
        _uiState.update { it.copy(messages = mapped, hasMore = merged.size < totalMessages) }
        if (emitPrependCount) {
            val added = mapped.size - previousSize
            if (added > 0) _prependEvents.tryEmit(added)
        }
    }

    private fun dedupeById(messages: List<SupportMessage>): List<SupportMessage> {
        val byId = LinkedHashMap<String, SupportMessage>()
        for (message in messages) byId[message.id] = message
        return byId.values.toList()
    }

    // Unparseable / missing timestamps sort to the newest end so optimistic or
    // malformed rows never jump above real history.
    private fun String?.toEpochMillisOrMax(): Long {
        if (isNullOrBlank()) return Long.MAX_VALUE
        return runCatching { Instant.parse(this).toEpochMilli() }.getOrDefault(Long.MAX_VALUE)
    }

    /** Best-effort read receipt; failures are silent and retried on the next fetch. */
    private fun markThreadRead() {
        viewModelScope.launch { markSupportThreadReadUseCase() }
    }

    private fun sendSupportMessage(text: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, errorMessage = null) }
            when (val result = sendSupportMessageUseCase(text)) {
                is Resource.Success -> {
                    // Re-fetch the canonical thread so our message (and any reply) ordering matches the server.
                    fetchThread(showLoading = false, surfaceError = false)
                    _uiState.update { it.copy(isSending = false, inputText = "") }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            errorMessage = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadPeerThread() {
        viewModelScope.launch { fetchPeerThread(showLoading = true, surfaceError = true) }
    }

    private suspend fun fetchPeerThread(showLoading: Boolean, surfaceError: Boolean) {
        if (showLoading) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        }
        when (val result = fetchNewestOrderPage()) {
            is Resource.Success -> {
                orderMessages = result.data.messages
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                rebuildOrderThread()
                reconcileOrderRead()
            }
            is Resource.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (surfaceError) result.message else it.errorMessage
                    )
                }
            }
            Resource.Loading -> Unit
        }
    }

    /**
     * The history endpoint paginates oldest-first, so the latest messages live on
     * the last page. Read [total] from page 1, then fetch the last page to show the
     * newest tail.
     */
    private suspend fun fetchNewestOrderPage(): Resource<OrderChatPage> {
        val first = getOrderMessagesUseCase(conversationId = conversationId, page = 1, limit = ORDER_PAGE_SIZE)
        if (first !is Resource.Success) return first
        val total = first.data.total
        if (total <= ORDER_PAGE_SIZE) return first
        val lastPage = (total + ORDER_PAGE_SIZE - 1) / ORDER_PAGE_SIZE
        return getOrderMessagesUseCase(conversationId = conversationId, page = lastPage, limit = ORDER_PAGE_SIZE)
    }

    private fun rebuildOrderThread() {
        val mapped = orderMessages.map { it.toChatMessage() }
        _uiState.update { it.copy(messages = mapped, hasMore = false) }
    }

    /** Send a read receipt only when a new incoming (counterparty) message appears. */
    private fun reconcileOrderRead() {
        val last = orderMessages.lastOrNull() ?: return
        if (last.id == lastSeenOrderMessageId) return
        lastSeenOrderMessageId = last.id
        if (last.senderId != currentUserId) {
            viewModelScope.launch { markOrderChatReadUseCase(conversationId) }
        }
    }

    private fun sendPeerMessage(text: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, errorMessage = null) }
            val pesananId = _uiState.value.activeOrder?.pesananId
            when (val result = sendOrderMessageUseCase(conversationId, text, pesananId)) {
                is Resource.Success -> {
                    fetchPeerThread(showLoading = false, surfaceError = false)
                    _uiState.update { it.copy(isSending = false, inputText = "") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isSending = false, errorMessage = result.message) }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun OrderChatMessage.toChatMessage(): ChatMessage {
        val text = if (isFile) "📎 " + (fileName ?: "Lampiran") else message
        val time = formatMessageTime(createdAt)
        return if (currentUserId != null && senderId == currentUserId) {
            ChatMessage.Sent(text = text, time = time)
        } else {
            ChatMessage.Received(
                text = text,
                time = time,
                senderInitials = _uiState.value.workerInitials.ifBlank { initialsOf(peerName) }
            )
        }
    }

    private fun initialsOf(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "?"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }

    private fun SupportMessage.toChatMessage(): ChatMessage =
        if (isFromUser) {
            ChatMessage.Sent(
                text = message,
                time = formatMessageTime(createdAt)
            )
        } else {
            ChatMessage.Received(
                text = message,
                time = formatMessageTime(createdAt),
                senderInitials = "LS"
            )
        }

    private fun formatMessageTime(createdAt: String?): String {
        if (createdAt.isNullOrBlank()) {
            return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        return runCatching {
            Instant.parse(createdAt)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm", Locale("id", "ID")))
        }.getOrDefault(createdAt.take(5))
    }

    private companion object {
        const val POLL_INTERVAL_MS = 4_000L
        const val PAGE_SIZE = 20
        const val ORDER_PAGE_SIZE = 30

        fun parseNavActiveOrder(savedStateHandle: SavedStateHandle): ActiveOrder? {
            val pesananId = savedStateHandle.get<String>("activePesananId").orEmpty()
            val statusRaw = savedStateHandle.get<String>("activeOrderStatus").orEmpty()
            if (pesananId.isBlank() || statusRaw.isBlank()) return null
            val title = savedStateHandle.get<String>("activeOrderTitle")?.takeIf { it.isNotBlank() }
            return ActiveOrder(
                pesananId = pesananId,
                status = PesananStatus.fromApiValue(statusRaw),
                title = title
            )
        }
    }
}
