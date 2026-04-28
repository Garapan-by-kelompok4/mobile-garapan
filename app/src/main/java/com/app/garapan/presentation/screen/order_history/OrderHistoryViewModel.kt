package com.app.garapan.presentation.screen.order_history

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class OrderHistoryItem(
    val title: String,
    val amount: String,
    val workerName: String,
    val time: String,
    val status: String,
    val isIncome: Boolean
)

data class OrderHistoryUiState(
    val items: List<OrderHistoryItem> = emptyList()
)

@HiltViewModel
class OrderHistoryViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        OrderHistoryUiState(
            items = listOf(
                OrderHistoryItem(
                    title = "Pembayaran Jasa: UI/UX App Design",
                    amount = "-Rp1.500.000",
                    workerName = "Andika Rafa",
                    time = "Hari ini, 14:30",
                    status = "SELESAI",
                    isIncome = false
                ),
                OrderHistoryItem(
                    title = "Pembayaran Proyek: Web Development",
                    amount = "-Rp5.000.000",
                    workerName = "Kemal Palevi",
                    time = "12 Okt 2023",
                    status = "DIPROSES",
                    isIncome = false
                ),
                OrderHistoryItem(
                    title = "Refund: Pengembalian Dana Pembatalan Desain Logo",
                    amount = "+Rp500.000",
                    workerName = "Pesanan dibatalkan",
                    time = "10 Okt 2023",
                    status = "DIBATALKAN",
                    isIncome = true
                )
            )
        )
    )
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()
}
