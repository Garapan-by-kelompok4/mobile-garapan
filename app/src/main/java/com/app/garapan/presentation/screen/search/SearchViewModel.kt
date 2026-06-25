package com.app.garapan.presentation.screen.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Jasa
import com.app.garapan.domain.model.JasaListFilters
import com.app.garapan.domain.model.Kategori
import com.app.garapan.domain.model.Project
import com.app.garapan.domain.model.ProjectListFilters
import com.app.garapan.domain.usecase.GetJasaListUseCase
import com.app.garapan.domain.usecase.GetKategoriListUseCase
import com.app.garapan.domain.usecase.GetProjectListUseCase
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.presentation.util.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
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
    val duration: String,
    val imageUrl: String = ""
)

data class SearchUiState(
    val query: String = "",
    val categories: List<String> = listOf(ALL_CATEGORIES),
    val isCategoryLoading: Boolean = false,
    val categoryErrorMessage: String? = null,
    val showFilterSheet: Boolean = false,
    val showResults: Boolean = false,
    val filter: FilterSortState = FilterSortState(),
    val results: List<SearchResultItem> = emptyList(),
    val isResultsLoading: Boolean = false,
    val resultsErrorMessage: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getKategoriListUseCase: GetKategoriListUseCase,
    private val getJasaListUseCase: GetJasaListUseCase,
    private val getProjectListUseCase: GetProjectListUseCase
) : ViewModel() {

    private val searchFocus: String = savedStateHandle["focus"] ?: Routes.SEARCH_FOCUS_BROWSE

    private var kategoriItems: List<Kategori> = emptyList()
    private var searchJob: Job? = null

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        if (searchFocus == Routes.SEARCH_FOCUS_JASA) {
            _uiState.update {
                it.copy(
                    filter = it.filter.copy(selectedType = FilterType.JASA),
                    showResults = true
                )
            }
            loadJasaResults()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCategoryLoading = true, categoryErrorMessage = null) }
            when (val result = getKategoriListUseCase()) {
                is Resource.Success -> {
                    kategoriItems = result.data
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

    fun onQueryChanged(query: String) {
        if (query.isBlank()) {
            _uiState.update {
                it.copy(
                    query = query,
                    showResults = searchFocus == Routes.SEARCH_FOCUS_JASA,
                    results = emptyList(),
                    resultsErrorMessage = null,
                    isResultsLoading = false
                )
            }
            if (searchFocus == Routes.SEARCH_FOCUS_JASA) {
                loadJasaResults()
            }
            return
        }

        _uiState.update { it.copy(query = query, showResults = true) }
        refreshResultsForCurrentType()
    }

    fun onShowFilter() = _uiState.update { it.copy(showFilterSheet = true) }
    fun onDismissFilter() = _uiState.update { it.copy(showFilterSheet = false) }

    fun onFilterTypeSelected(type: FilterType) {
        _uiState.update { it.copy(filter = it.filter.copy(selectedType = type)) }
    }

    fun onCategorySelected(category: String) =
        _uiState.update { it.copy(filter = it.filter.copy(selectedCategory = category)) }

    fun onMinPriceChanged(value: String) =
        _uiState.update { it.copy(filter = it.filter.copy(minPrice = value.filter { c -> c.isDigit() }.take(9))) }

    fun onMaxPriceChanged(value: String) =
        _uiState.update { it.copy(filter = it.filter.copy(maxPrice = value.filter { c -> c.isDigit() }.take(9))) }

    fun onSortSelected(option: SortOption) =
        _uiState.update { it.copy(filter = it.filter.copy(sortBy = option)) }

    fun onApplyFilter() {
        _uiState.update { it.copy(showFilterSheet = false, showResults = true) }
        refreshResultsForCurrentType()
    }

    fun retryResults() = refreshResultsForCurrentType()

    private fun refreshResultsForCurrentType() {
        when (_uiState.value.filter.selectedType) {
            FilterType.JASA -> loadJasaResults()
            FilterType.PROYEK -> loadProjectResults()
        }
    }

    private fun loadProjectResults() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isResultsLoading = true, resultsErrorMessage = null) }
            val state = _uiState.value
            val kategoriId = kategoriItems
                .firstOrNull { it.name == state.filter.selectedCategory }
                ?.id
                ?.takeIf { state.filter.selectedCategory != ALL_CATEGORIES }
            val filters = ProjectListFilters(
                search = state.query.takeIf { it.isNotBlank() },
                kategoriId = kategoriId,
                minBudget = state.filter.minPrice.toDoubleOrNull(),
                maxBudget = state.filter.maxPrice.toDoubleOrNull(),
                includeRelatedSkills = kategoriId != null
            )
            when (val result = getProjectListUseCase(filters)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            results = result.data.map(::toProjectSearchResultItem),
                            isResultsLoading = false,
                            resultsErrorMessage = null,
                            showResults = true
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            results = emptyList(),
                            isResultsLoading = false,
                            resultsErrorMessage = result.message,
                            showResults = true
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadJasaResults() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isResultsLoading = true, resultsErrorMessage = null) }
            val state = _uiState.value
            val kategoriId = kategoriItems
                .firstOrNull { it.name == state.filter.selectedCategory }
                ?.id
                ?.takeIf { state.filter.selectedCategory != ALL_CATEGORIES }
            val filters = JasaListFilters(
                search = state.query.takeIf { it.isNotBlank() },
                kategoriId = kategoriId,
                minPrice = state.filter.minPrice.toDoubleOrNull(),
                maxPrice = state.filter.maxPrice.toDoubleOrNull(),
                sort = state.filter.sortBy.toApiSort(),
                includeRelatedSkills = kategoriId != null
            )
            when (val result = getJasaListUseCase(filters)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            results = result.data.map(::toJasaSearchResultItem),
                            isResultsLoading = false,
                            resultsErrorMessage = null,
                            showResults = true
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            results = emptyList(),
                            isResultsLoading = false,
                            resultsErrorMessage = result.message,
                            showResults = true
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun toJasaSearchResultItem(jasa: Jasa) = SearchResultItem(
        id = jasa.id,
        title = jasa.title,
        workerName = jasa.workerName.ifBlank { "Freelancer" },
        rating = jasa.rating.toFloat().takeIf { it > 0f } ?: jasa.workerRating.toFloat(),
        reviewCount = jasa.reviewCount,
        price = CurrencyFormatter.formatRupiah(jasa.price),
        duration = "-",
        imageUrl = jasa.imageUrl
    )

    private fun toProjectSearchResultItem(project: Project) = SearchResultItem(
        id = project.id,
        title = project.title,
        workerName = project.clientName.ifBlank { "Klien" },
        rating = 0f,
        reviewCount = 0,
        price = CurrencyFormatter.formatRupiah(project.budget),
        duration = formatDeadline(project.deadline),
        imageUrl = ""
    )

    private fun formatDeadline(deadline: String): String {
        if (deadline.isBlank()) return "-"
        return runCatching {
            val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID"))
            Instant.parse(deadline).atZone(ZoneId.systemDefault()).format(formatter)
        }.getOrDefault(deadline.take(10))
    }

    private fun SortOption.toApiSort(): String? = when (this) {
        SortOption.PALING_POPULER -> "newest"
        SortOption.RATING_TERTINGGI -> "rating_desc"
        SortOption.HARGA_TERENDAH -> "price_asc"
    }
}
