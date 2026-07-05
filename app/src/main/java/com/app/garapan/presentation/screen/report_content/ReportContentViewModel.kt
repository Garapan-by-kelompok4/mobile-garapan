package com.app.garapan.presentation.screen.report_content

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.ContentReportContentType
import com.app.garapan.domain.usecase.SubmitContentReportUseCase
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

data class ReportContentUiState(
    val contentType: ContentReportContentType? = null,
    val contentId: String = "",
    val reason: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val screenTitle: String
        get() = when (contentType) {
            ContentReportContentType.JASA -> "Laporkan Jasa"
            ContentReportContentType.PROJECT -> "Laporkan Proyek"
            null -> "Laporkan"
        }

    val introText: String
        get() = when (contentType) {
            ContentReportContentType.JASA ->
                "Jelaskan masalah pada jasa ini. Laporan Anda akan ditinjau oleh Admin."
            ContentReportContentType.PROJECT ->
                "Jelaskan masalah pada proyek ini. Laporan Anda akan ditinjau oleh Admin."
            null -> ""
        }

    val reasonLength: Int get() = reason.length
    val canSubmit: Boolean get() =
        reason.trim().length >= ReportValidation.MIN_REASON_LENGTH && !isLoading
}

sealed interface ReportContentEvent {
    data class ShowMessage(val message: String) : ReportContentEvent
    data object NavigateBack : ReportContentEvent
}

@HiltViewModel
class ReportContentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val submitContentReportUseCase: SubmitContentReportUseCase
) : ViewModel() {

    private val contentType: ContentReportContentType? =
        ContentReportContentType.fromRouteValue(savedStateHandle.get<String>("contentType").orEmpty())
    private val contentId: String = savedStateHandle.get<String>("contentId").orEmpty()

    private val _uiState = MutableStateFlow(
        ReportContentUiState(contentType = contentType, contentId = contentId)
    )
    val uiState: StateFlow<ReportContentUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ReportContentEvent>()
    val events: SharedFlow<ReportContentEvent> = _events.asSharedFlow()

    fun onReasonChanged(value: String) {
        if (value.length <= ReportValidation.MAX_REASON_LENGTH) {
            _uiState.update { it.copy(reason = value, errorMessage = null) }
        }
    }

    fun onSubmitReport() {
        val type = contentType
        if (type == null || contentId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Data laporan tidak valid.") }
            return
        }

        val reason = _uiState.value.reason.trim()
        val validationError = ReportValidation.validateReason(reason)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = submitContentReportUseCase(type, contentId, reason)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(
                        ReportContentEvent.ShowMessage(
                            "Laporan berhasil dikirim. Admin akan meninjau dalam waktu dekat."
                        )
                    )
                    _events.emit(ReportContentEvent.NavigateBack)
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
