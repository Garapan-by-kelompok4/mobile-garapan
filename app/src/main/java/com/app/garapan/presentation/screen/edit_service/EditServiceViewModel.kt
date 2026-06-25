package com.app.garapan.presentation.screen.edit_service

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.usecase.GetKategoriListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditServiceUiState(
    val title: String = "",
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "",
    val isCategoryLoading: Boolean = false,
    val categoryErrorMessage: String? = null,
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
    savedStateHandle: SavedStateHandle,
    private val getKategoriListUseCase: GetKategoriListUseCase
) : ViewModel() {

    val teamOptions = listOf(
        "Individu (1 Orang)",
        "Tim (2 Orang)",
        "Tim (2-3 Orang)",
        "Tim (4+ Orang)"
    )

    private val serviceId: String = savedStateHandle["serviceId"] ?: "service-1"
    private val _uiState = MutableStateFlow(dummyServices[serviceId] ?: dummyServices["service-1"]!!)
    val uiState: StateFlow<EditServiceUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCategoryLoading = true, categoryErrorMessage = null) }
            when (val result = getKategoriListUseCase()) {
                is Resource.Success -> {
                    val categories = result.data.map { it.name }
                    _uiState.update { state ->
                        val selectedCategory = state.selectedCategory
                            .takeIf { it in categories }
                            ?: categories.firstOrNull().orEmpty()
                        state.copy(
                            categories = categories,
                            selectedCategory = selectedCategory,
                            isCategoryLoading = false,
                            categoryErrorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            categories = emptyList(),
                            selectedCategory = "",
                            isCategoryLoading = false,
                            categoryErrorMessage = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onTitleChanged(value: String) = _uiState.update { it.copy(title = value) }
    fun onCategorySelected(value: String) = _uiState.update { it.copy(selectedCategory = value) }
    fun onTeamSizeSelected(value: String) = _uiState.update { it.copy(teamSize = value) }
    fun onDescriptionChanged(value: String) = _uiState.update { it.copy(description = value) }
    fun onMinimumBudgetChanged(value: String) = _uiState.update { it.copy(minimumBudget = value.filter(Char::isDigit).take(9)) }
    fun onMaximumBudgetChanged(value: String) = _uiState.update { it.copy(maximumBudget = value.filter(Char::isDigit).take(9)) }
    fun onDeadlineChanged(value: String) = _uiState.update { it.copy(deadline = value) }
}
