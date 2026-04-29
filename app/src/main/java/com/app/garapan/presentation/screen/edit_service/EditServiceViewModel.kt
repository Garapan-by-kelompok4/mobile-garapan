package com.app.garapan.presentation.screen.edit_service

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class EditServiceUiState(
    val title: String = "",
    val selectedCategory: String = "Web Development",
    val teamSize: String = "",
    val description: String = "",
    val minimumBudget: String = "",
    val maximumBudget: String = "",
    val deadline: String = ""
)

private val dummyServices = mapOf(
    "service-1" to EditServiceUiState(
        title = "Platform e-Learning Interaktif untuk Bimbingan Belajar",
        selectedCategory = "Web Development",
        teamSize = "Tim (2-3 Orang)",
        description = "Kami mencari talenta mahasiswa berbakat untuk membantu membangun platform e-learning interaktif yang akan digunakan oleh ribuan pelajar di seluruh Indonesia.",
        minimumBudget = "5000000",
        maximumBudget = "8000000",
        deadline = "04/20/2026"
    ),
    "service-2" to EditServiceUiState(
        title = "Aplikasi Manajemen Inventaris Gudang",
        selectedCategory = "Mobile Apps",
        teamSize = "Tim (1-2 Orang)",
        description = "Dibutuhkan developer untuk membangun aplikasi manajemen inventaris gudang berbasis mobile dengan scan barcode dan laporan stok real-time.",
        minimumBudget = "3000000",
        maximumBudget = "5000000",
        deadline = "05/18/2026"
    )
)

@HiltViewModel
class EditServiceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val categories = listOf(
        "Web Development",
        "Mobile Apps",
        "UI/UX Design",
        "Data Science",
        "Lainnya"
    )
    val teamOptions = listOf(
        "Individu (1 Orang)",
        "Tim (2 Orang)",
        "Tim (2-3 Orang)",
        "Tim (4+ Orang)"
    )

    private val serviceId: String = savedStateHandle["serviceId"] ?: "service-1"
    private val _uiState = MutableStateFlow(dummyServices[serviceId] ?: dummyServices["service-1"]!!)
    val uiState: StateFlow<EditServiceUiState> = _uiState.asStateFlow()

    fun onTitleChanged(value: String) = _uiState.update { it.copy(title = value) }
    fun onCategorySelected(value: String) = _uiState.update { it.copy(selectedCategory = value) }
    fun onTeamSizeSelected(value: String) = _uiState.update { it.copy(teamSize = value) }
    fun onDescriptionChanged(value: String) = _uiState.update { it.copy(description = value) }
    fun onMinimumBudgetChanged(value: String) = _uiState.update { it.copy(minimumBudget = value.filter(Char::isDigit).take(9)) }
    fun onMaximumBudgetChanged(value: String) = _uiState.update { it.copy(maximumBudget = value.filter(Char::isDigit).take(9)) }
    fun onDeadlineChanged(value: String) = _uiState.update { it.copy(deadline = value) }
}
