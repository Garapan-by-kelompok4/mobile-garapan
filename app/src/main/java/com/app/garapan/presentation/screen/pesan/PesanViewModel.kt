package com.app.garapan.presentation.screen.pesan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Conversation
import com.app.garapan.domain.model.PesananStatus
import com.app.garapan.domain.usecase.GetConversationsUseCase
import com.app.garapan.domain.usecase.GetSupportThreadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val accent: ChatAccent = ChatAccent.BLUE
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
        id = SUPPORT_CHAT_ID,
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

    init {
        refresh()
    }

    fun refresh() {
        refreshSupportThread()
        loadConversations()
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
        publish()
    }

    /** Refresh the support inbox entry (unread, last message preview, time, agent name). */
    fun refreshSupportThread() {
        viewModelScope.launch {
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
    }

    private fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getConversationsUseCase()) {
                is Resource.Success -> {
                    peopleChats = result.data.map { it.toPreviewItem() }
                    _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                    publish()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                Resource.Loading -> Unit
            }
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
            id = pesananId,
            name = counterpartyName,
            preview = preview,
            time = formatRelative(lastMessageAt),
            unreadCount = unreadCount,
            statusLabel = status?.toLabel(),
            isAdmin = false,
            accent = status.toAccent()
        )
    }

    private fun PesananStatus.toLabel(): String = when (this) {
        PesananStatus.PENDING -> "Menunggu"
        PesananStatus.PAID -> "Dibayar"
        PesananStatus.IN_PROGRESS -> "Proyek Aktif"
        PesananStatus.DELIVERED -> "Dikirim"
        PesananStatus.COMPLETED -> "Selesai"
        PesananStatus.DISPUTED -> "Sengketa"
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
        const val SUPPORT_CHAT_ID = "admin-1"
    }
}
