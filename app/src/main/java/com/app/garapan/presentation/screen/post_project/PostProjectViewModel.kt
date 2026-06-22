package com.app.garapan.presentation.screen.post_project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.usecase.GetKategoriListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class PostProjectUiState(
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

@HiltViewModel
class PostProjectViewModel @Inject constructor(
    private val getKategoriListUseCase: GetKategoriListUseCase
) : ViewModel() {

    val teamOptions = listOf(
        "Individu (1 Orang)",
        "Tim (2 Orang)",
        "Tim (2-3 Orang)",
        "Tim (4+ Orang)"
    )

    private val _uiState = MutableStateFlow(PostProjectUiState())
    val uiState: StateFlow<PostProjectUiState> = _uiState.asStateFlow()

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
