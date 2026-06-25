package com.app.garapan.presentation.screen.add_portfolio

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.data.util.PortfolioImageReader
import com.app.garapan.data.util.PortfolioImageReadResult
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreatePortofolioParams
import com.app.garapan.domain.model.PortofolioImage
import com.app.garapan.domain.usecase.AddPortofolioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val MAX_IMAGE_BYTES = PortfolioImageReader.MAX_BYTES

data class AddPortfolioUiState(
    val title: String = "",
    val description: String = "",
    val imageUri: Uri? = null,
    val preparedImage: PortofolioImage? = null,
    val isProcessingImage: Boolean = false,
    val projectUrl: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AddPortfolioEvent {
    data object Saved : AddPortfolioEvent
}

@HiltViewModel
class AddPortfolioViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val addPortofolioUseCase: AddPortofolioUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPortfolioUiState())
    val uiState: StateFlow<AddPortfolioUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddPortfolioEvent>()
    val events: SharedFlow<AddPortfolioEvent> = _events.asSharedFlow()

    fun onTitleChanged(value: String) = _uiState.update { it.copy(title = value) }
    fun onDescriptionChanged(value: String) = _uiState.update { it.copy(description = value) }
    fun onProjectUrlChanged(value: String) = _uiState.update { it.copy(projectUrl = value) }

    fun onImageSelected(uri: Uri, rawBytes: ByteArray?, readContext: Context = context) {
        _uiState.update {
            it.copy(
                imageUri = uri,
                preparedImage = null,
                isProcessingImage = true,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                if (rawBytes != null) {
                    PortfolioImageReader.readCompressedFromBytes(
                        bytes = rawBytes,
                        fileName = fileNameFromUri(uri),
                        context = readContext,
                        uri = uri
                    )
                } else {
                    PortfolioImageReader.readCompressedWithResult(readContext, uri)
                }
            }
            _uiState.update { state ->
                when (result) {
                    is PortfolioImageReadResult.Success -> {
                        if (result.image.bytes.size > MAX_IMAGE_BYTES) {
                            state.copy(
                                isProcessingImage = false,
                                preparedImage = null,
                                errorMessage = "Ukuran gambar maksimal 5 MB."
                            )
                        } else {
                            state.copy(
                                isProcessingImage = false,
                                preparedImage = result.image,
                                errorMessage = null
                            )
                        }
                    }
                    is PortfolioImageReadResult.Failure -> {
                        state.copy(
                            isProcessingImage = false,
                            errorMessage = "Gagal memproses gambar. Coba pilih gambar lain."
                        )
                    }
                }
            }
        }
    }

    fun onSave() {
        val state = _uiState.value
        if (state.title.isBlank() || state.description.isBlank() || state.imageUri == null) {
            _uiState.update {
                it.copy(errorMessage = "Judul, deskripsi, dan gambar wajib diisi.")
            }
            return
        }
        if (state.isProcessingImage) {
            _uiState.update {
                it.copy(errorMessage = "Gambar masih diproses. Tunggu sebentar.")
            }
            return
        }

        val image = state.preparedImage
        if (image == null) {
            _uiState.update {
                it.copy(errorMessage = "Gagal memproses gambar. Coba pilih gambar lain.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val params = CreatePortofolioParams(
                title = state.title.trim(),
                description = state.description.trim(),
                image = image,
                projectUrl = state.projectUrl.trim().ifBlank { null }
            )
            when (val result = addPortofolioUseCase(params)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(AddPortfolioEvent.Saved)
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

    private fun fileNameFromUri(uri: Uri): String {
        return uri.lastPathSegment
            ?.substringAfterLast('/')
            ?.takeIf { it.isNotBlank() }
            ?.let { if (it.contains('.')) it.substringBeforeLast('.') + ".jpg" else "$it.jpg" }
            ?: "portfolio.jpg"
    }
}
