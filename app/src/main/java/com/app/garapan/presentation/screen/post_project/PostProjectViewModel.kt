package com.app.garapan.presentation.screen.post_project

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.data.util.PortfolioImageReader
import com.app.garapan.data.util.PortfolioImageReadResult
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreateProjectParams
import com.app.garapan.domain.model.Kategori
import com.app.garapan.domain.usecase.CreateProjectUseCase
import com.app.garapan.domain.usecase.GetKategoriListUseCase
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
import java.util.Locale
import java.util.TimeZone
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
    val deadline: String = "",
    val imageUri: Uri? = null,
    val preparedImage: com.app.garapan.domain.model.PortofolioImage? = null,
    val isProcessingImage: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitErrorMessage: String? = null
)

sealed interface PostProjectEvent {
    data class ShowMessage(val message: String) : PostProjectEvent
    data object Published : PostProjectEvent
}

@HiltViewModel
class PostProjectViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getKategoriListUseCase: GetKategoriListUseCase,
    private val createProjectUseCase: CreateProjectUseCase
) : ViewModel() {

    val teamOptions = listOf(
        "Individu (1 Orang)",
        "Tim (2 Orang)",
        "Tim (2-3 Orang)",
        "Tim (4+ Orang)"
    )

    private var kategoriItems: List<Kategori> = emptyList()

    private val _uiState = MutableStateFlow(PostProjectUiState())
    val uiState: StateFlow<PostProjectUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PostProjectEvent>()
    val events: SharedFlow<PostProjectEvent> = _events.asSharedFlow()

    init {
        loadCategories()
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
                submitErrorMessage = null
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
                                submitErrorMessage = "Ukuran gambar maksimal 5 MB."
                            )
                        } else {
                            state.copy(
                                isProcessingImage = false,
                                preparedImage = result.image,
                                submitErrorMessage = null
                            )
                        }
                    }
                    is PortfolioImageReadResult.Failure -> {
                        state.copy(
                            isProcessingImage = false,
                            submitErrorMessage = "Gagal memproses gambar. Coba pilih gambar lain."
                        )
                    }
                }
            }
        }
    }

    fun onPublish() {
        if (_uiState.value.isSubmitting || _uiState.value.isProcessingImage) return

        val validationError = validateForm(_uiState.value)
        if (validationError != null) {
            viewModelScope.launch {
                _events.emit(PostProjectEvent.ShowMessage(validationError))
            }
            return
        }

        val state = _uiState.value
        val kategoriId = kategoriItems.firstOrNull { it.name == state.selectedCategory }?.id
        if (kategoriId.isNullOrBlank()) {
            viewModelScope.launch {
                _events.emit(PostProjectEvent.ShowMessage("Pilih kategori proyek terlebih dahulu."))
            }
            return
        }

        val budget = resolveBudget(state)
        val deadlineIso = parseDeadlineToIso(state.deadline)
        if (deadlineIso == null) {
            viewModelScope.launch {
                _events.emit(PostProjectEvent.ShowMessage("Format deadline tidak valid."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitErrorMessage = null) }
            when (
                val result = createProjectUseCase(
                    CreateProjectParams(
                        title = state.title.trim(),
                        description = state.description.trim(),
                        budget = budget,
                        deadline = deadlineIso,
                        kategoriId = kategoriId,
                        image = state.preparedImage
                    )
                )
            ) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.emit(PostProjectEvent.Published)
                }
                is Resource.Error -> {
                    val message = UserMessageLocalizer.localize(result.message)
                    _uiState.update {
                        it.copy(isSubmitting = false, submitErrorMessage = message)
                    }
                    _events.emit(PostProjectEvent.ShowMessage(message))
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun validateForm(state: PostProjectUiState): String? = when {
        state.title.isBlank() -> "Judul proyek wajib diisi."
        state.selectedCategory.isBlank() -> "Kategori proyek wajib dipilih."
        state.description.isBlank() -> "Deskripsi proyek wajib diisi."
        resolveBudget(state) <= 0.0 -> "Anggaran proyek wajib diisi."
        state.deadline.isBlank() -> "Deadline proyek wajib dipilih."
        else -> null
    }

    private fun resolveBudget(state: PostProjectUiState): Double {
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
}
