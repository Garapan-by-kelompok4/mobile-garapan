package com.app.garapan.presentation.screen.edit_project

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.data.util.PortfolioImageReader
import com.app.garapan.data.util.PortfolioImageReadResult
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Kategori
import com.app.garapan.domain.model.UpdateProjectParams
import com.app.garapan.domain.usecase.GetKategoriListUseCase
import com.app.garapan.domain.usecase.GetProjectDetailUseCase
import com.app.garapan.domain.usecase.UpdateProjectUseCase
import com.app.garapan.presentation.util.UserMessageLocalizer
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
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

data class EditProjectUiState(
    val title: String = "",
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "",
    val isCategoryLoading: Boolean = false,
    val categoryErrorMessage: String? = null,
    val description: String = "",
    val minimumBudget: String = "",
    val maximumBudget: String = "",
    val deadline: String = "",
    val existingImageUrl: String = "",
    val imageUri: Uri? = null,
    val preparedImage: com.app.garapan.domain.model.PortofolioImage? = null,
    val isProcessingImage: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed interface EditProjectEvent {
    data object Saved : EditProjectEvent
    data class ShowMessage(val message: String) : EditProjectEvent
}

@HiltViewModel
class EditProjectViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val getKategoriListUseCase: GetKategoriListUseCase,
    private val getProjectDetailUseCase: GetProjectDetailUseCase,
    private val updateProjectUseCase: UpdateProjectUseCase
) : ViewModel() {

    private val projectId: String = savedStateHandle["projectId"] ?: ""

    private var kategoriItems: List<Kategori> = emptyList()

    private val _uiState = MutableStateFlow(EditProjectUiState())
    val uiState: StateFlow<EditProjectUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditProjectEvent>()
    val events: SharedFlow<EditProjectEvent> = _events.asSharedFlow()

    init {
        loadCategories()
        loadProject()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCategoryLoading = true, categoryErrorMessage = null) }
            when (val result = getKategoriListUseCase()) {
                is Resource.Success -> {
                    kategoriItems = result.data
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
                            isCategoryLoading = false,
                            categoryErrorMessage = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadProject() {
        if (projectId.isBlank()) {
            _uiState.update {
                it.copy(isLoading = false, errorMessage = "Proyek tidak ditemukan.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getProjectDetailUseCase(projectId)) {
                is Resource.Success -> {
                    val project = result.data
                    val minimumBudgetText = (project.minBudget ?: project.budget).toLong().toString()
                    val maximumBudgetText = (project.maxBudget ?: project.budget).toLong().toString()
                    _uiState.update { state ->
                        state.copy(
                            title = project.title,
                            selectedCategory = project.kategoriName.ifBlank {
                                kategoriItems.firstOrNull { it.id == project.kategoriId }?.name.orEmpty()
                            },
                            description = project.description,
                            minimumBudget = minimumBudgetText,
                            maximumBudget = maximumBudgetText,
                            deadline = formatIsoToDisplay(project.deadline),
                            existingImageUrl = project.imageUrl,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
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

    fun retry() = loadProject()

    fun onTitleChanged(value: String) = _uiState.update { it.copy(title = value) }
    fun onCategorySelected(value: String) = _uiState.update { it.copy(selectedCategory = value) }
    fun onDescriptionChanged(value: String) = _uiState.update { it.copy(description = value) }
    fun onMinimumBudgetChanged(value: String) =
        _uiState.update { it.copy(minimumBudget = value.filter(Char::isDigit).take(9)) }

    fun onMaximumBudgetChanged(value: String) =
        _uiState.update { it.copy(maximumBudget = value.filter(Char::isDigit).take(9)) }

    fun onDeadlineChanged(value: String) = _uiState.update { it.copy(deadline = value) }

    fun onImageSelected(uri: Uri, readContext: Context = context) {
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
                PortfolioImageReader.readCompressedWithResult(readContext, uri)
            }
            _uiState.update { state ->
                when (result) {
                    is PortfolioImageReadResult.Success -> {
                        if (result.image.bytes.size > PortfolioImageReader.MAX_BYTES) {
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
        if (_uiState.value.isSaving || _uiState.value.isProcessingImage) return

        val validationError = validateForm(_uiState.value)
        if (validationError != null) {
            viewModelScope.launch {
                _events.emit(EditProjectEvent.ShowMessage(validationError))
            }
            return
        }

        val state = _uiState.value
        val kategoriId = kategoriItems.firstOrNull { it.name == state.selectedCategory }?.id
        if (kategoriId.isNullOrBlank()) {
            viewModelScope.launch {
                _events.emit(EditProjectEvent.ShowMessage("Pilih kategori proyek terlebih dahulu."))
            }
            return
        }

        val budget = resolveBudget(state)
        val minBudget = state.minimumBudget.toDoubleOrNull()
        val maxBudget = state.maximumBudget.toDoubleOrNull()
        val deadlineIso = parseDeadlineToIso(state.deadline)
        if (deadlineIso == null) {
            viewModelScope.launch {
                _events.emit(EditProjectEvent.ShowMessage("Format deadline tidak valid."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (
                val result = updateProjectUseCase(
                    projectId,
                    UpdateProjectParams(
                        title = state.title.trim(),
                        description = state.description.trim(),
                        budget = budget,
                        minBudget = minBudget,
                        maxBudget = maxBudget,
                        deadline = deadlineIso,
                        kategoriId = kategoriId,
                        image = state.preparedImage
                    )
                )
            ) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(EditProjectEvent.Saved)
                }
                is Resource.Error -> {
                    val message = UserMessageLocalizer.localize(result.message)
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = message)
                    }
                    _events.emit(EditProjectEvent.ShowMessage(message))
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun validateForm(state: EditProjectUiState): String? {
        val minBudget = state.minimumBudget.toDoubleOrNull()
        val maxBudget = state.maximumBudget.toDoubleOrNull()
        return when {
            state.title.isBlank() -> "Judul proyek wajib diisi."
            state.selectedCategory.isBlank() -> "Kategori proyek wajib dipilih."
            state.description.isBlank() -> "Deskripsi proyek wajib diisi."
            minBudget == null || minBudget <= 0.0 -> "Anggaran minimum wajib diisi."
            maxBudget == null || maxBudget <= 0.0 -> "Anggaran maksimum wajib diisi."
            maxBudget < minBudget -> "Anggaran maksimum tidak boleh lebih kecil dari anggaran minimum."
            state.deadline.isBlank() -> "Deadline proyek wajib dipilih."
            else -> null
        }
    }

    private fun resolveBudget(state: EditProjectUiState): Double {
        val maxBudget = state.maximumBudget.toDoubleOrNull()
        val minBudget = state.minimumBudget.toDoubleOrNull()
        return when {
            maxBudget != null && maxBudget > 0.0 -> maxBudget
            minBudget != null && minBudget > 0.0 -> minBudget
            else -> 0.0
        }
    }

    private fun parseDeadlineToIso(display: String): String? = runCatching {
        val parser = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val date = parser.parse(display) ?: return null
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(date)
    }.getOrNull()

    private fun formatIsoToDisplay(iso: String): String {
        if (iso.isBlank()) return ""
        return runCatching {
            val instant = Instant.parse(iso)
            SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date.from(instant))
        }.getOrDefault(iso.take(10))
    }
}
