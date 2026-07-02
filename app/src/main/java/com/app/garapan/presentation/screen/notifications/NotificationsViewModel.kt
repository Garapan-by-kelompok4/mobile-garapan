package com.app.garapan.presentation.screen.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Notification
import com.app.garapan.domain.model.NotificationMeta
import com.app.garapan.domain.model.NotificationType
import com.app.garapan.domain.usecase.GetNotificationsUseCase
import com.app.garapan.domain.usecase.MarkAllNotificationsReadUseCase
import com.app.garapan.domain.usecase.MarkNotificationReadUseCase
import com.app.garapan.presentation.navigation.Routes
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val read: Boolean,
    val timeLabel: String,
    val meta: NotificationMeta? = null
)

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val isMarkingAllRead: Boolean = false,
    val errorMessage: String? = null,
    val notifications: List<NotificationItem> = emptyList()
) {
    val unreadCount: Int get() = notifications.count { !it.read }
}

sealed interface NotificationsEvent {
    data class NavigateToOrderDetail(val pesananId: String) : NotificationsEvent
    data class NavigateToAllReviews(val jasaId: String) : NotificationsEvent
    data class NavigateToChat(val route: String) : NotificationsEvent
    data class NavigateToProjectDetail(val projectId: String) : NotificationsEvent
    data class ShowMessage(val message: String) : NotificationsEvent
    data object NavigateToOrderHistory : NotificationsEvent
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase,
    private val markAllNotificationsReadUseCase: MarkAllNotificationsReadUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<NotificationsEvent>()
    val events: SharedFlow<NotificationsEvent> = _events.asSharedFlow()

    init {
        loadNotifications()
    }

    fun refresh() = loadNotifications()

    fun retry() = loadNotifications()

    fun markAllRead() {
        if (_uiState.value.unreadCount == 0 || _uiState.value.isMarkingAllRead) return
        viewModelScope.launch {
            _uiState.update { it.copy(isMarkingAllRead = true) }
            when (val result = markAllNotificationsReadUseCase()) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isMarkingAllRead = false,
                            notifications = state.notifications.map { item ->
                                item.copy(read = true)
                            }
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isMarkingAllRead = false) }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onNotificationClick(item: NotificationItem) {
        viewModelScope.launch {
            if (!item.read) {
                when (val result = markNotificationReadUseCase(item.id)) {
                    is Resource.Success -> {
                        _uiState.update { state ->
                            state.copy(
                                notifications = state.notifications.map { notification ->
                                    if (notification.id == item.id) {
                                        notification.copy(read = true)
                                    } else {
                                        notification
                                    }
                                }
                            )
                        }
                    }
                    is Resource.Error -> Unit
                    Resource.Loading -> Unit
                }
            }
            _events.emit(resolveNavigationEvent(item))
        }
    }

    private fun resolveNavigationEvent(item: NotificationItem): NotificationsEvent {
        val meta = item.meta
        val jasaId = meta?.jasaId
        if (!jasaId.isNullOrBlank() && item.type == NotificationType.REVIEW_RECEIVED) {
            return NotificationsEvent.NavigateToAllReviews(jasaId)
        }
        if (item.type == NotificationType.CHAT_MESSAGE) {
            val conversationId = meta?.conversationId
            return if (!conversationId.isNullOrBlank()) {
                NotificationsEvent.NavigateToChat(Routes.chatRoute(conversationId))
            } else {
                NotificationsEvent.NavigateToChat(Routes.supportChatRoute())
            }
        }
        val projectId = meta?.projectId
        if (
            !projectId.isNullOrBlank() &&
            (
                item.type == NotificationType.PROPOSAL_RECEIVED ||
                    item.type == NotificationType.PROPOSAL_REJECTED
                )
        ) {
            return NotificationsEvent.NavigateToProjectDetail(projectId)
        }
        val pesananId = meta?.pesananId
        if (!pesananId.isNullOrBlank()) {
            return NotificationsEvent.NavigateToOrderDetail(pesananId)
        }
        return when (item.type) {
            NotificationType.REVIEW_RECEIVED -> NotificationsEvent.ShowMessage(
                "Ulasan tidak dapat dibuka dari notifikasi ini."
            )
            NotificationType.PROPOSAL_RECEIVED,
            NotificationType.PROPOSAL_REJECTED -> NotificationsEvent.ShowMessage(
                "Proyek tidak dapat dibuka dari notifikasi ini."
            )
            else -> NotificationsEvent.NavigateToOrderHistory
        }
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getNotificationsUseCase()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            notifications = result.data.map(::toNotificationItem)
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            notifications = emptyList()
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun toNotificationItem(notification: Notification) = NotificationItem(
        id = notification.id,
        title = notification.title,
        body = notification.body,
        type = notification.type,
        read = notification.read,
        timeLabel = formatRelativeTime(notification.createdAt),
        meta = notification.meta
    )

    private fun formatRelativeTime(createdAt: String): String {
        return runCatching {
            val instant = Instant.parse(createdAt)
            val now = Instant.now()
            val duration = Duration.between(instant, now)
            when {
                duration.toMinutes() < 1 -> "Baru saja"
                duration.toHours() < 1 -> "${duration.toMinutes()} menit lalu"
                duration.toDays() < 1 -> "${duration.toHours()} jam lalu"
                duration.toDays() < 7 -> "${duration.toDays()} hari lalu"
                else -> {
                    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID"))
                    instant.atZone(ZoneId.systemDefault()).format(formatter)
                }
            }
        }.getOrDefault("")
    }
}
