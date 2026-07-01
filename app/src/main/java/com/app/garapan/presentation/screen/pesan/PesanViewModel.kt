package com.app.garapan.presentation.screen.pesan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Conversation
import com.app.garapan.domain.model.PesananStatus
import com.app.garapan.domain.usecase.GetConversationsUseCase
import com.app.garapan.domain.usecase.GetSupportThreadUseCase
import com.app.garapan.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class ChatPreviewItem(
    val id: String,
    val name: String,
    val preview: String,
    val time: String,
    val unreadCount: Int = 0,
    val statusLabel: String? = null,
    val isAdmin: Boolean = false,
    val accent: ChatAccent = ChatAccent.BLUE,
    val activeOrder: com.app.garapan.domain.model.ActiveOrder? = null
)

enum class ChatAccent {
    BLUE,
    NAVY,
    CORAL,
    GREEN
}

data class PesanUiState(
    val query: String = "",
    val adminChats: List<ChatPreviewItem> = emptyList(),
    val peopleChats: List<ChatPreviewItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PesanViewModel @Inject constructor(
    private val getSupportThreadUseCase: GetSupportThreadUseCase,
    private val getConversationsUseCase: GetConversationsUseCase
) : ViewModel() {

    // The admin (support) entry is always present; its live fields are filled by
    // the support-thread fetch. People chats come from GET /chat.
    private var adminChat: ChatPreviewItem = ChatPreviewItem(
        id = Routes.SUPPORT_WORKER_ID,
        name = "Bantuan Admin",
        preview = "Ada yang bisa kami bantu hari ini?",
        time = "",
        statusLabel = "Support",
        isAdmin = true,
        accent = ChatAccent.BLUE
    )
    private var peopleChats: List<ChatPreviewItem> = emptyList()

    private val _uiState = MutableStateFlow(PesanUiState(adminChats = listOf(adminChat)))
    val uiState: StateFlow<PesanUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null

    /**
     * Poll the inbox while the Chat tab is in the foreground so new messages,
     * previews and unread badges appear live without leaving the screen. The
     * first pass shows the loading spinner; later passes refresh silently.
     */
    fun startPolling() {
        if (pollJob?.isActive == true) return
        pollJob = viewModelScope.launch {
            var first = true
            while (isActive) {
                refreshSupportThreadSnapshot()
                loadConversations(showLoading = first && peopleChats.isEmpty())
                first = false
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    fun refresh() {
        viewModelScope.launch {
            refreshSupportThreadSnapshot()
            loadConversations()
        }
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
        publish()
    }

    /** Refresh the support inbox entry (unread, last message preview, time, agent name). */
    private suspend fun refreshSupportThreadSnapshot() {
        val result = getSupportThreadUseCase()
        if (result is Resource.Success) {
            val page = result.data
            val last = page.messages.lastOrNull()
            adminChat = adminChat.copy(
                name = page.agentName?.takeIf { it.isNotBlank() } ?: adminChat.name,
                preview = last?.message?.takeIf { it.isNotBlank() } ?: adminChat.preview,
                time = formatRelative(last?.createdAt),
                unreadCount = page.unreadCount
            )
            publish()
        }
    }

    private suspend fun loadConversations(showLoading: Boolean = true) {
        if (showLoading) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        }
        when (val result = getConversationsUseCase()) {
            is Resource.Success -> {
                peopleChats = result.data.map { it.toPreviewItem() }
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                publish()
            }
            is Resource.Error -> {
                // Keep the last good list on a silent poll failure; only surface
                // the error when we were explicitly (re)loading.
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (showLoading) result.message else it.errorMessage
                    )
                }
            }
            Resource.Loading -> Unit
        }
    }

    /** Re-apply the current search query to the latest admin + people data. */
    private fun publish() {
        val query = _uiState.value.query.trim()
        _uiState.update {
            it.copy(
                adminChats = listOf(adminChat).filter { chat -> chat.matches(query) },
                peopleChats = peopleChats.filter { chat -> chat.matches(query) }
            )
        }
    }

    private fun ChatPreviewItem.matches(query: String): Boolean {
        if (query.isEmpty()) return true
        return name.contains(query, ignoreCase = true) ||
            preview.contains(query, ignoreCase = true)
    }

    private fun Conversation.toPreviewItem(): ChatPreviewItem {
        val preview = when {
            lastMessageIsFile -> "📎 Lampiran"
            !lastMessage.isNullOrBlank() -> lastMessage
            else -> "Belum ada pesan"
        }
        return ChatPreviewItem(
            id = conversationId,
            name = counterpartyName,
            preview = preview,
            time = formatRelative(lastMessageAt),
            unreadCount = unreadCount,
            statusLabel = activeOrder?.status?.toLabel(),
            isAdmin = false,
            accent = activeOrder?.status.toAccent(),
            activeOrder = activeOrder
        )
    }

    private fun PesananStatus.toLabel(): String = when (this) {
        PesananStatus.PENDING -> "Menunggu"
        PesananStatus.PAID -> "Dibayar"
        PesananStatus.IN_PROGRESS -> "Proyek Aktif"
        PesananStatus.DELIVERED -> "Dikirim"
        PesananStatus.COMPLETED -> "Selesai"
        PesananStatus.DISPUTED -> "Sengketa"
        PesananStatus.CANCELLED -> "Dibatalkan"
    }

    private fun PesananStatus?.toAccent(): ChatAccent = when (this) {
        PesananStatus.COMPLETED -> ChatAccent.GREEN
        PesananStatus.DISPUTED -> ChatAccent.CORAL
        PesananStatus.IN_PROGRESS, PesananStatus.DELIVERED -> ChatAccent.NAVY
        else -> ChatAccent.BLUE
    }

    /** ISO-8601 instant -> friendly inbox time: "HH:mm" today, "Kemarin", weekday, else date. */
    private fun formatRelative(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        val instant = runCatching { Instant.parse(iso) }.getOrNull() ?: return ""
        val zone = ZoneId.systemDefault()
        val dateTime = instant.atZone(zone)
        val today = LocalDate.now(zone)
        val date = dateTime.toLocalDate()
        return when {
            date.isEqual(today) -> dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            date.isEqual(today.minusDays(1)) -> "Kemarin"
            Duration.between(instant, Instant.now()).toDays() < 7 ->
                dateTime.format(DateTimeFormatter.ofPattern("EEEE", Locale("id", "ID")))
            else -> dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yy"))
        }
    }

    private companion object {
        const val POLL_INTERVAL_MS = 5_000L
    }
}
