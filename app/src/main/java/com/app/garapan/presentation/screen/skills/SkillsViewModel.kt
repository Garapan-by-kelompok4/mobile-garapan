package com.app.garapan.presentation.screen.skills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.usecase.GetSkillListUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import com.app.garapan.domain.usecase.UpdateSkillsUseCase
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
    val options: List<String> = emptyList(),
    val selectedSkills: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isOptionsEmpty: Boolean = false
)

sealed interface SkillsEvent {
    data object Saved : SkillsEvent
}

@HiltViewModel
class SkillsViewModel @Inject constructor(
    private val getSkillListUseCase: GetSkillListUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val updateSkillsUseCase: UpdateSkillsUseCase
) : ViewModel() {

    private var savedSkills: Set<String> = emptySet()

    private val _uiState = MutableStateFlow(SkillsUiState())
    val uiState: StateFlow<SkillsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SkillsEvent>()
    val events: SharedFlow<SkillsEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                savedSkills = user?.mahasiswa?.skills.orEmpty().toSet()
                _uiState.update { state ->
                    state.copy(
                        selectedSkills = savedSkills,
                        options = mergeSkillOptions(state.options, savedSkills)
                    )
                }
            }
        }
        loadSkillOptions()
    }

    fun retry() = loadSkillOptions()

    private fun loadSkillOptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getSkillListUseCase()) {
                is Resource.Success -> {
                    val apiOptions = result.data.map { it.name }
                    val merged = mergeSkillOptions(apiOptions, savedSkills)
                    _uiState.update {
                        it.copy(
                            options = merged,
                            selectedSkills = savedSkills,
                            isLoading = false,
                            isOptionsEmpty = apiOptions.isEmpty(),
                            errorMessage = if (apiOptions.isEmpty()) {
                                "Daftar keahlian dari server kosong. Hubungi admin atau coba lagi nanti."
                            } else {
                                null
                            }
                        )
                    }
                }
                is Resource.Error -> {
                    val merged = mergeSkillOptions(emptyList(), savedSkills)
                    _uiState.update {
                        it.copy(
                            options = merged,
                            selectedSkills = savedSkills,
                            isLoading = false,
                            isOptionsEmpty = merged.isEmpty(),
                            errorMessage = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun mergeSkillOptions(apiOptions: List<String>, saved: Set<String>): List<String> =
        (apiOptions + saved.toList()).distinct().sorted()

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
                    savedSkills = selected.toSet()
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
