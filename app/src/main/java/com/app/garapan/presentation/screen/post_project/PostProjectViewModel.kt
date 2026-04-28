package com.app.garapan.presentation.screen.post_project

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class PostProjectUiState(
    val title: String = "",
    val selectedCategory: String = "Web Development",
    val teamSize: String = "",
    val description: String = "",
    val minimumBudget: String = "",
    val maximumBudget: String = "",
    val deadline: String = ""
)

@HiltViewModel
class PostProjectViewModel @Inject constructor() : ViewModel() {

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

    private val _uiState = MutableStateFlow(PostProjectUiState())
    val uiState: StateFlow<PostProjectUiState> = _uiState.asStateFlow()

    fun onTitleChanged(value: String) = _uiState.update { it.copy(title = value) }
    fun onCategorySelected(value: String) = _uiState.update { it.copy(selectedCategory = value) }
    fun onTeamSizeSelected(value: String) = _uiState.update { it.copy(teamSize = value) }
    fun onDescriptionChanged(value: String) = _uiState.update { it.copy(description = value) }
    fun onMinimumBudgetChanged(value: String) = _uiState.update { it.copy(minimumBudget = value.filter(Char::isDigit).take(9)) }
    fun onMaximumBudgetChanged(value: String) = _uiState.update { it.copy(maximumBudget = value.filter(Char::isDigit).take(9)) }
    fun onDeadlineChanged(value: String) = _uiState.update { it.copy(deadline = value) }
}
