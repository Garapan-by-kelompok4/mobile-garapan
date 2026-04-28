package com.app.garapan.presentation.screen.search

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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

val categoryOptions = listOf(
    "Semua Kategori", "Coding", "Fullstack Dev", "Data Analysis", "Backend Dev",
    "UI/UX Design", "Mobile Dev", "DevOps", "AI/ML", "Cyber Security"
)

data class FilterSortState(
    val selectedType: FilterType = FilterType.PROYEK,
    val selectedCategory: String = "Semua Kategori",
    val minPrice: String = "100.000",
    val maxPrice: String = "5.000.000",
    val sortBy: SortOption = SortOption.PALING_POPULER
)

data class SearchUiState(
    val query: String = "",
    val showFilterSheet: Boolean = false,
    val filter: FilterSortState = FilterSortState()
)

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onQueryChanged(query: String) = _uiState.update { it.copy(query = query) }

    fun onShowFilter() = _uiState.update { it.copy(showFilterSheet = true) }
    fun onDismissFilter() = _uiState.update { it.copy(showFilterSheet = false) }

    fun onFilterTypeSelected(type: FilterType) =
        _uiState.update { it.copy(filter = it.filter.copy(selectedType = type)) }

    fun onCategorySelected(category: String) =
        _uiState.update { it.copy(filter = it.filter.copy(selectedCategory = category)) }

    fun onMinPriceChanged(value: String) =
        _uiState.update { it.copy(filter = it.filter.copy(minPrice = value)) }

    fun onMaxPriceChanged(value: String) =
        _uiState.update { it.copy(filter = it.filter.copy(maxPrice = value)) }

    fun onSortSelected(option: SortOption) =
        _uiState.update { it.copy(filter = it.filter.copy(sortBy = option)) }

    fun onApplyFilter() = _uiState.update { it.copy(showFilterSheet = false) }
}
