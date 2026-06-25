package com.app.garapan.presentation.screen.dispute

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.usecase.SubmitDisputeUseCase
import com.app.garapan.presentation.util.UserMessageLocalizer
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

data class DisputeUiState(
    val pesananId: String = "",
    val reason: String = "",
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null
)

sealed interface DisputeEvent {
    data class ShowMessage(val message: String) : DisputeEvent
    data object NavigateBack : DisputeEvent
}

@HiltViewModel
class DisputeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val submitDisputeUseCase: SubmitDisputeUseCase
) : ViewModel() {

    private val pesananId: String = savedStateHandle["pesananId"] ?: ""

    private val _uiState = MutableStateFlow(DisputeUiState(pesananId = pesananId))
    val uiState: StateFlow<DisputeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DisputeEvent>()
    val events: SharedFlow<DisputeEvent> = _events.asSharedFlow()

    fun onReasonChanged(value: String) {
        _uiState.update { it.copy(reason = value, errorMessage = null) }
    }

    fun onSubmitDispute() {
        val reason = _uiState.value.reason.trim()
        if (reason.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Alasan dispute tidak boleh kosong.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = submitDisputeUseCase(pesananId, reason)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSubmitted = true) }
                    _events.emit(DisputeEvent.ShowMessage("Dispute berhasil diajukan."))
                    _events.emit(DisputeEvent.NavigateBack)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UserMessageLocalizer.localize(result.message)
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }
}
