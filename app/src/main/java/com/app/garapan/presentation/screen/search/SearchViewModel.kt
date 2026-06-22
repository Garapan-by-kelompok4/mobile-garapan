package com.app.garapan.presentation.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.usecase.GetKategoriListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class FilterType { PROYEK, JASA }

enum class SortOption(val label: String) {
    PALING_POPULER("Paling Populer"),
    RATING_TERTINGGI("Rating Tertinggi"),
    HARGA_TERENDAH("Harga Terendah")
}

private const val ALL_CATEGORIES = "Semua Kategori"

data class FilterSortState(
    val selectedType: FilterType = FilterType.PROYEK,
    val selectedCategory: String = ALL_CATEGORIES,
    val minPrice: String = "100000",
    val maxPrice: String = "5000000",
    val sortBy: SortOption = SortOption.PALING_POPULER
)

data class SearchResultItem(
    val id: String,
    val title: String,
    val workerName: String,
    val rating: Float,
    val reviewCount: Int,
    val price: String,
    val duration: String
)

data class SearchUiState(
    val query: String = "",
    val categories: List<String> = listOf(ALL_CATEGORIES),
    val isCategoryLoading: Boolean = false,
    val categoryErrorMessage: String? = null,
    val showFilterSheet: Boolean = false,
    val showResults: Boolean = false,
    val filter: FilterSortState = FilterSortState(),
    val results: List<SearchResultItem> = emptyList()
)

private val dummyResults = listOf(
    SearchResultItem("1", "Pembuatan Website Company Profile Modern", "Andi Pratama", 4.9f, 47, "Rp 2.500.000", "5 hari"),
    SearchResultItem("2", "Desain UI/UX Mobile App dari Scratch", "Sari Dewi", 4.8f, 63, "Rp 1.800.000", "7 hari"),
    SearchResultItem("3", "Setup CI/CD Pipeline & Deploy ke VPS", "Rizky Fajar", 5.0f, 29, "Rp 1.200.000", "3 hari"),
    SearchResultItem("4", "Machine Learning Model untuk Klasifikasi Data", "Budi Santoso", 4.7f, 38, "Rp 3.500.000", "10 hari"),
    SearchResultItem("5", "REST API Backend dengan Node.js & PostgreSQL", "Andi Pratama", 4.9f, 21, "Rp 2.000.000", "4 hari"),
    SearchResultItem("6", "Aplikasi Android E-Commerce dengan Jetpack Compose", "Sari Dewi", 4.8f, 15, "Rp 4.500.000", "14 hari"),
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getKategoriListUseCase: GetKategoriListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState(results = dummyResults))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCategoryLoading = true, categoryErrorMessage = null) }
            when (val result = getKategoriListUseCase()) {
                is Resource.Success -> {
                    val categoryNames = result.data.map { it.name }
                    val categories = listOf(ALL_CATEGORIES) + categoryNames
                    _uiState.update { state ->
                        val selectedCategory = state.filter.selectedCategory
                            .takeIf { it in categories }
                            ?: ALL_CATEGORIES
                        state.copy(
                            categories = categories,
                            isCategoryLoading = false,
                            categoryErrorMessage = null,
                            filter = state.filter.copy(selectedCategory = selectedCategory)
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            categories = listOf(ALL_CATEGORIES),
                            isCategoryLoading = false,
                            categoryErrorMessage = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onQueryChanged(query: String) = _uiState.update {
        it.copy(query = query, showResults = query.isNotEmpty())
    }

    fun onShowFilter() = _uiState.update { it.copy(showFilterSheet = true) }
    fun onDismissFilter() = _uiState.update { it.copy(showFilterSheet = false) }

    fun onFilterTypeSelected(type: FilterType) =
        _uiState.update { it.copy(filter = it.filter.copy(selectedType = type)) }

    fun onCategorySelected(category: String) =
        _uiState.update { it.copy(filter = it.filter.copy(selectedCategory = category)) }

    fun onMinPriceChanged(value: String) =
        _uiState.update { it.copy(filter = it.filter.copy(minPrice = value.filter { c -> c.isDigit() }.take(9))) }

    fun onMaxPriceChanged(value: String) =
        _uiState.update { it.copy(filter = it.filter.copy(maxPrice = value.filter { c -> c.isDigit() }.take(9))) }

    fun onSortSelected(option: SortOption) =
        _uiState.update { it.copy(filter = it.filter.copy(sortBy = option)) }

    fun onApplyFilter() = _uiState.update {
        it.copy(showFilterSheet = false, showResults = true)
    }
}
