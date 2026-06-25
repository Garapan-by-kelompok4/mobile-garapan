package com.app.garapan.presentation.screen.edit_portfolio

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.data.util.PortfolioImageReader
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreatePortofolioParams
import com.app.garapan.domain.model.PortofolioImage
import com.app.garapan.domain.usecase.AddPortofolioUseCase
import com.app.garapan.domain.usecase.DeletePortofolioUseCase
import com.app.garapan.domain.usecase.GetPortofolioUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
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
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

private const val MAX_IMAGE_BYTES = PortfolioImageReader.MAX_BYTES

data class EditPortfolioUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val description: String = "",
    val projectUrl: String = "",
    val existingImageUrl: String = "",
    val newImageUri: Uri? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed interface EditPortfolioEvent {
    data object Saved : EditPortfolioEvent
}

@HiltViewModel
class EditPortfolioViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val getPortofolioUseCase: GetPortofolioUseCase,
    private val addPortofolioUseCase: AddPortofolioUseCase,
    private val deletePortofolioUseCase: DeletePortofolioUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val portfolioId: String = savedStateHandle.get<String>("portfolioId").orEmpty()
    private val mahasiswaId: String? = observeCurrentUserUseCase.snapshot()?.mahasiswa?.id

    private val _uiState = MutableStateFlow(EditPortfolioUiState())
    val uiState: StateFlow<EditPortfolioUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditPortfolioEvent>()
    val events: SharedFlow<EditPortfolioEvent> = _events.asSharedFlow()

    init {
        loadPortfolioItem()
    }

    fun onTitleChanged(value: String) = _uiState.update { it.copy(title = value) }
    fun onDescriptionChanged(value: String) = _uiState.update { it.copy(description = value) }
    fun onProjectUrlChanged(value: String) = _uiState.update { it.copy(projectUrl = value) }

    fun onImageSelected(uri: Uri) {
        _uiState.update { it.copy(newImageUri = uri, errorMessage = null) }
    }

    fun onSave() {
        val state = _uiState.value
        if (state.title.isBlank() || state.description.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Judul dan deskripsi wajib diisi.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val image = resolveImage(state)
            if (image == null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Gagal memuat gambar portofolio."
                    )
                }
                return@launch
            }
            if (image.bytes.size > MAX_IMAGE_BYTES) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = "Ukuran gambar maksimal 5 MB.")
                }
                return@launch
            }

            when (val deleteResult = deletePortofolioUseCase(portfolioId)) {
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = deleteResult.message)
                    }
                    return@launch
                }
                Resource.Loading -> return@launch
                is Resource.Success -> Unit
            }

            val params = CreatePortofolioParams(
                title = state.title.trim(),
                description = state.description.trim(),
                image = image,
                projectUrl = state.projectUrl.trim().ifBlank { null }
            )
            when (val createResult = addPortofolioUseCase(params)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(EditPortfolioEvent.Saved)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = createResult.message)
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadPortfolioItem() {
        val id = mahasiswaId
        if (id.isNullOrBlank() || portfolioId.isBlank()) {
            _uiState.value = EditPortfolioUiState(
                isLoading = false,
                errorMessage = "Portofolio tidak ditemukan."
            )
            return
        }

        viewModelScope.launch {
            when (val result = getPortofolioUseCase(id)) {
                is Resource.Success -> {
                    val item = result.data.firstOrNull { it.id == portfolioId }
                    if (item == null) {
                        _uiState.value = EditPortfolioUiState(
                            isLoading = false,
                            errorMessage = "Portofolio tidak ditemukan."
                        )
                    } else {
                        _uiState.value = EditPortfolioUiState(
                            isLoading = false,
                            title = item.title,
                            description = item.description,
                            projectUrl = item.projectUrl.orEmpty(),
                            existingImageUrl = item.imageUrl
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.value = EditPortfolioUiState(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }

    private suspend fun resolveImage(state: EditPortfolioUiState): PortofolioImage? {
        state.newImageUri?.let { return PortfolioImageReader.readCompressed(context, it) }
        if (state.existingImageUrl.isNotBlank()) {
            val downloaded = downloadImage(state.existingImageUrl) ?: return null
            return PortfolioImageReader.compressBytes(downloaded.bytes, downloaded.fileName)
        }
        return null
    }

    private suspend fun downloadImage(url: String): PortofolioImage? = withContext(Dispatchers.IO) {
        runCatching {
            val response = okHttpClient.newCall(Request.Builder().url(url).build()).execute()
            response.use {
                if (!it.isSuccessful) return@withContext null
                val bytes = it.body?.bytes() ?: return@withContext null
                val mimeType = it.header("Content-Type")?.takeIf { type -> type.startsWith("image/") }
                    ?: "image/jpeg"
                PortofolioImage(bytes = bytes, mimeType = mimeType, fileName = "portfolio.jpg")
            }
        }.getOrNull()
    }
}
