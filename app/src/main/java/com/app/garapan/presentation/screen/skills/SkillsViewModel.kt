package com.app.garapan.presentation.screen.skills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import com.app.garapan.domain.usecase.UpdateSkillsUseCase
import com.app.garapan.presentation.screen.auth.studentExpertiseOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SkillsUiState(
    val options: List<String> = studentExpertiseOptions,
    val selectedSkills: Set<String> = emptySet(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed interface SkillsEvent {
    data object Saved : SkillsEvent
}

@HiltViewModel
class SkillsViewModel @Inject constructor(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val updateSkillsUseCase: UpdateSkillsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SkillsUiState())
    val uiState: StateFlow<SkillsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SkillsEvent>()
    val events: SharedFlow<SkillsEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                val skills = user?.mahasiswa?.skills.orEmpty()
                _uiState.update { state ->
                    state.copy(
                        selectedSkills = skills.filter { it in state.options }.toSet()
                    )
                }
            }
        }
    }

    fun onToggleSkill(skill: String) {
        _uiState.update { state ->
            val updated = if (skill in state.selectedSkills) {
                state.selectedSkills - skill
            } else {
                state.selectedSkills + skill
            }
            state.copy(selectedSkills = updated, errorMessage = null)
        }
    }

    fun onSave() {
        val selected = _uiState.value.selectedSkills.toList()
        if (selected.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = updateSkillsUseCase(selected)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(SkillsEvent.Saved)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = result.message)
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }
}
