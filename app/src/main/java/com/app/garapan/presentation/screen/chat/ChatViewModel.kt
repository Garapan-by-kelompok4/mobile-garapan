package com.app.garapan.presentation.screen.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.SupportMessage
import com.app.garapan.domain.usecase.GetSupportThreadUseCase
import com.app.garapan.domain.usecase.SendSupportMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val isOnline: Boolean = true,
    val isAdminSupport: Boolean = false,
    val supportLabel: String? = null,
    val dateSeparator: String = "Hari ini",
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSupportThreadUseCase: GetSupportThreadUseCase,
    private val sendSupportMessageUseCase: SendSupportMessageUseCase
) : ViewModel() {

    private val workerId: String = savedStateHandle["workerId"] ?: "1"
    private val isSupportThread = workerId == SUPPORT_WORKER_ID

    private val dummyData = mapOf(
        "1" to ChatUiState(
            workerName = "Ahmad Sumbul",
            workerInitials = "AS",
            isOnline = true,
            messages = listOf(
                ChatMessage.JasaCard(
                    title = "Pembuatan Website Company Profile Modern",
                    price = "Rp 2.500.000",
                    time = "08:46"
                ),
                ChatMessage.Sent(
                    text = "Halo, saya tertarik dengan jasa pembuatan website company profile Anda. Apakah bisa menambahkan animasi transisi yang *smooth* antar section?",
                    time = "10:00"
                ),
                ChatMessage.Received(
                    text = "Halo! Tentu bisa, Kak. Saya menggunakan Framer Motion untuk animasi UI yang premium. Ada referensi desain yang diinginkan?",
                    time = "10:15",
                    senderInitials = "AS"
                ),
                ChatMessage.Sent(
                    text = "Ada, saya suka gaya minimalis ala Apple. Saya butuh selesai dalam 3 hari, bisa?",
                    time = "10:22"
                ),
                ChatMessage.Received(
                    text = "Untuk timeline 3 hari dengan kualitas premium bisa saya usahakan, Kak. Tapi perlu saya lihat dulu scope detailnya. Bisa kirim brief proyeknya?",
                    time = "10:30",
                    senderInitials = "AS"
                ),
                ChatMessage.FileAndOrderConfirmation(
                    fileName = "Proposal_Desain_Minimalis.pdf",
                    fileSize = "3.0 MB",
                    serviceName = "Jasa Landing Page",
                    servicePrice = "Rp 5.000.000",
                    extras = "Revisi Tambahan (3x)",
                    total = "Rp 5.000.000",
                    time = "10:50",
                    senderInitials = "AS"
                )
            )
        ),
        "admin-1" to ChatUiState(
            workerName = "Bantuan Admin",
            workerInitials = "LS",
            isOnline = true,
            isAdminSupport = true,
            supportLabel = "Live Support",
            dateSeparator = "Hari ini, 08:30",
            messages = listOf(
                ChatMessage.Received(
                    text = "Halo! Selamat datang di layanan bantuan GARAPAN. Ada yang bisa saya bantu terkait kendala teknis atau transaksi Anda hari ini?",
                    time = "08:30",
                    senderInitials = "LS"
                ),
                ChatMessage.Sent(
                    text = "Halo min, saya mengalami kendala saat mencoba mengunggah portofolio proyek saya. Muncul error terus dari tadi pagi.",
                    time = "08:45"
                ),
                ChatMessage.Received(
                    text = "Mohon maaf atas ketidaknyamanannya. Boleh tolong kirimkan screenshot pesan error yang muncul, atau beri tahu format dan ukuran file yang Anda coba unggah?",
                    time = "08:46",
                    senderInitials = "LS"
                ),
                ChatMessage.Sent(
                    text = "Baik, saya kirim screenshot-nya sekarang. File saya format PNG ukuran sekitar 3 MB.",
                    time = "08:48"
                )
            )
        )
    )

    private val _uiState = MutableStateFlow(dummyData[workerId] ?: dummyData["1"]!!)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        if (isSupportThread) {
            loadSupportThread()
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
        val sentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        _uiState.update {
            it.copy(
                messages = it.messages + ChatMessage.Sent(text = text, time = sentTime),
                inputText = ""
            )
        }
    }

    fun retry() {
        if (isSupportThread) loadSupportThread()
    }

    private fun loadSupportThread() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getSupportThreadUseCase()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            messages = result.data.map { message -> message.toChatMessage() }
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun sendSupportMessage(text: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, errorMessage = null) }
            when (val result = sendSupportMessageUseCase(text)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            inputText = "",
                            messages = it.messages + result.data.toChatMessage()
                        )
                    }
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
        const val SUPPORT_WORKER_ID = "admin-1"
    }
}
