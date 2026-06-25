package com.app.garapan.presentation.screen.edit_service

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.data.util.PortfolioImageReader
import com.app.garapan.data.util.PortfolioImageReadResult
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreateJasaParams
import com.app.garapan.domain.model.Jasa
import com.app.garapan.domain.model.Kategori
import com.app.garapan.domain.model.PortofolioImage
import com.app.garapan.domain.model.UpdateJasaParams
import com.app.garapan.domain.usecase.CreateJasaUseCase
import com.app.garapan.domain.usecase.GetJasaDetailUseCase
import com.app.garapan.domain.usecase.GetKategoriListUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import com.app.garapan.domain.usecase.UpdateJasaUseCase
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

const val NEW_JASA_ID = "new"

data class EditServiceUiState(
    val title: String = "",
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "",
    val isCategoryLoading: Boolean = false,
    val categoryErrorMessage: String? = null,
    val description: String = "",
    val price: String = "",
    val existingImageUrl: String = "",
    val imageUri: Uri? = null,
    val preparedImage: PortofolioImage? = null,
    val isProcessingImage: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed interface EditServiceEvent {
    data class Saved(val jasa: Jasa) : EditServiceEvent
}

@HiltViewModel
class EditServiceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val getKategoriListUseCase: GetKategoriListUseCase,
    private val getJasaDetailUseCase: GetJasaDetailUseCase,
    private val createJasaUseCase: CreateJasaUseCase,
    private val updateJasaUseCase: UpdateJasaUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private val serviceId: String = savedStateHandle["serviceId"] ?: NEW_JASA_ID
    val isNewJasa: Boolean = serviceId == NEW_JASA_ID

    private var kategoriItems: List<Kategori> = emptyList()
    private var suggestedKategoriName: String? = null

    private val _uiState = MutableStateFlow(EditServiceUiState())
    val uiState: StateFlow<EditServiceUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditServiceEvent>()
    val events: SharedFlow<EditServiceEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                suggestedKategoriName = user?.mahasiswa?.suggestedKategoriName?.takeIf { it.isNotBlank() }
                applySuggestedKategoriIfNeeded()
            }
        }
        loadCategories()
        if (!isNewJasa) {
            loadJasaDetail()
        }
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
                            ?: resolveSuggestedCategory(categories)
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

    private fun applySuggestedKategoriIfNeeded() {
        if (!isNewJasa) return
        _uiState.update { state ->
            if (state.selectedCategory.isNotBlank() && state.selectedCategory in state.categories) {
                state
            } else {
                val suggested = resolveSuggestedCategory(state.categories)
                if (suggested != null) state.copy(selectedCategory = suggested) else state
            }
        }
    }

    private fun resolveSuggestedCategory(categories: List<String>): String? =
        suggestedKategoriName?.takeIf { it in categories }

    private fun loadJasaDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getJasaDetailUseCase(serviceId)) {
                is Resource.Success -> {
                    val jasa = result.data
                    _uiState.update {
                        it.copy(
                            title = jasa.title,
                            selectedCategory = jasa.kategoriName.ifBlank {
                                kategoriItems.firstOrNull { k -> k.id == jasa.kategoriId }?.name.orEmpty()
                            },
                            description = jasa.description,
                            price = jasa.price.toLong().toString(),
                            existingImageUrl = jasa.imageUrl,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onTitleChanged(value: String) = _uiState.update { it.copy(title = value) }
    fun onCategorySelected(value: String) = _uiState.update { it.copy(selectedCategory = value) }
    fun onDescriptionChanged(value: String) = _uiState.update { it.copy(description = value) }
    fun onPriceChanged(value: String) = _uiState.update { it.copy(price = value.filter(Char::isDigit).take(9)) }

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
        val state = _uiState.value
        val kategoriId = kategoriItems.firstOrNull { it.name == state.selectedCategory }?.id
        val price = state.price.toDoubleOrNull()

        when {
            state.title.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Judul wajib diisi.") }
                return
            }
            kategoriId.isNullOrBlank() -> {
                _uiState.update { it.copy(errorMessage = "Kategori wajib dipilih.") }
                return
            }
            state.description.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Deskripsi wajib diisi.") }
                return
            }
            price == null || price <= 0 -> {
                _uiState.update { it.copy(errorMessage = "Harga wajib diisi.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = if (isNewJasa) {
                val image = state.preparedImage
                if (image == null) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "Gambar wajib diisi untuk jasa baru."
                        )
                    }
                    return@launch
                }
                createJasaUseCase(
                    CreateJasaParams(
                        kategoriId = kategoriId,
                        title = state.title.trim(),
                        description = state.description.trim(),
                        price = price,
                        image = image
                    )
                )
            } else {
                updateJasaUseCase(
                    serviceId,
                    UpdateJasaParams(
                        title = state.title.trim(),
                        description = state.description.trim(),
                        price = price,
                        kategoriId = kategoriId
                    )
                )
            }

            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(EditServiceEvent.Saved(result.data))
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
