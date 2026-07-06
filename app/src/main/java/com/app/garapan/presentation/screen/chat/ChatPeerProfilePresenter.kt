package com.app.garapan.presentation.screen.chat

import com.app.garapan.domain.model.ActiveOrder
import com.app.garapan.domain.model.Conversation

data class ChatPeerProfile(
    val name: String,
    val initials: String,
    val avatarUrl: String?,
    val activeOrder: ActiveOrder?
)

object ChatPeerProfilePresenter {
    fun needsHydration(peerName: String): Boolean = peerName.isBlank()

    fun from(conversation: Conversation): ChatPeerProfile = ChatPeerProfile(
        name = conversation.counterpartyName,
        initials = initialsOf(conversation.counterpartyName),
        avatarUrl = conversation.counterpartyAvatarUrl?.takeIf { it.isNotBlank() },
        activeOrder = conversation.activeOrder
    )

    fun initialsOf(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "?"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }
}
