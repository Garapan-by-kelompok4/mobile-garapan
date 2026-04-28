package com.app.garapan.presentation.screen.pesan

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val peopleChats: List<ChatPreviewItem> = emptyList()
)

@HiltViewModel
class PesanViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        PesanUiState(
            adminChats = listOf(
                ChatPreviewItem(
                    id = "admin-1",
                    name = "Bantuan Admin",
                    preview = "Ada yang bisa kami bantu hari ini?",
                    time = "Baru saja",
                    unreadCount = 1,
                    statusLabel = "Support",
                    isAdmin = true,
                    accent = ChatAccent.BLUE
                )
            ),
            peopleChats = listOf(
                ChatPreviewItem(
                    id = "chat-1",
                    name = "Aris Setiawan",
                    preview = "Halo, proyeknya sudah saya review.",
                    time = "10:45",
                    unreadCount = 2,
                    statusLabel = "Proyek Aktif",
                    accent = ChatAccent.NAVY
                ),
                ChatPreviewItem(
                    id = "chat-2",
                    name = "Budi Santoso",
                    preview = "Kapan estimasi selesai?",
                    time = "Kemarin",
                    accent = ChatAccent.GREEN
                ),
                ChatPreviewItem(
                    id = "chat-3",
                    name = "Siti Aisyah",
                    preview = "Terima kasih atas revisinya. Hasilnya sudah sesuai.",
                    time = "Senin",
                    statusLabel = "Selesai",
                    accent = ChatAccent.CORAL
                )
            )
        )
    )
    val uiState: StateFlow<PesanUiState> = _uiState.asStateFlow()

    fun onQueryChanged(query: String) = _uiState.update { it.copy(query = query) }
}
