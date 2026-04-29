package com.app.garapan.presentation.screen.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
}

data class ChatUiState(
    val workerName: String = "",
    val workerInitials: String = "",
    val isOnline: Boolean = true,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workerId: String = savedStateHandle["workerId"] ?: "1"

    private val dummyData = mapOf(
        "1" to ChatUiState(
            workerName = "Ahmad Sumbul",
            workerInitials = "AS",
            isOnline = true,
            messages = listOf(
                ChatMessage.JasaCard(
                    title = "Pembuatan Website Company Profile Modern",
                    price = "Rp 2.500.000",
                    time = "08:46 AM"
                ),
                ChatMessage.Sent(
                    text = "Halo, saya tertarik dengan jasa pembuatan website company profile Anda. Apakah bisa menambahkan animasi transisi yang *smooth* antar section?",
                    time = "10:00 AM"
                ),
                ChatMessage.Received(
                    text = "Halo! Tentu bisa, Kak. Saya menggunakan Framer Motion untuk animasi UI yang premium. Ada referensi desain yang diinginkan?",
                    time = "10:15 AM",
                    senderInitials = "AS"
                ),
                ChatMessage.Sent(
                    text = "Ada, saya suka gaya minimalis ala Apple. Saya butuh selesai dalam 3 hari, bisa?",
                    time = "10:22 AM"
                ),
                ChatMessage.Received(
                    text = "Untuk timeline 3 hari dengan kualitas premium bisa saya usahakan, Kak. Tapi perlu saya lihat dulu scope detailnya. Bisa kirim brief proyeknya?",
                    time = "10:30 AM",
                    senderInitials = "AS"
                )
            )
        )
    )

    private val _uiState = MutableStateFlow(dummyData[workerId] ?: dummyData["1"]!!)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onInputChanged(text: String) = _uiState.update { it.copy(inputText = text) }

    fun onSend() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return
        _uiState.update {
            it.copy(
                messages = it.messages + ChatMessage.Sent(text = text, time = "Now"),
                inputText = ""
            )
        }
    }
}
