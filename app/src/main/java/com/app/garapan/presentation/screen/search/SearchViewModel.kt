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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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

enum class SearchResultType { JASA, PROYEK }

enum class SortOption(val label: String) {
    PALING_POPULER("Paling Populer"),
    RATING_TERTINGGI("Rating Tertinggi"),
    HARGA_TERENDAH("Harga Terendah")
}

private const val ALL_CATEGORIES = "Semua Kategori"

data class FilterSortState(
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
    val imageUrl: String = "",
    val type: SearchResultType
)

data class SearchUiState(
    val query: String = "",
    val categories: List<String> = listOf(ALL_CATEGORIES),
    val isCategoryLoading: Boolean = false,
    val categoryErrorMessage: String? = null,
    val showFilterSheet: Boolean = false,
    val showResults: Boolean = false,
    val filter: FilterSortState = FilterSortState(),
    val jasaResults: List<SearchResultItem> = emptyList(),
    val projectResults: List<SearchResultItem> = emptyList(),
    val isResultsLoading: Boolean = false,
    val resultsErrorMessage: String? = null,
    val queryTooShort: Boolean = false
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
    private var unifiedSearchMode = false
    private var filtersApplied = false

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        when (searchFocus) {
            Routes.SEARCH_FOCUS_JASA -> {
                _uiState.update { it.copy(showResults = true) }
                loadJasaBrowseResults()
            }
            else -> {
                _uiState.update { it.copy(showResults = true) }
                loadBrowseResults()
            }
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
            unifiedSearchMode = false
            searchJob?.cancel()
            _uiState.update {
                it.copy(
                    query = query,
                    showResults = true,
                    jasaResults = emptyList(),
                    projectResults = emptyList(),
                    resultsErrorMessage = null,
                    isResultsLoading = false,
                    queryTooShort = false
                )
            }
            if (searchFocus == Routes.SEARCH_FOCUS_JASA) {
                loadJasaBrowseResults()
            } else {
                loadBrowseResults()
            }
            return
        }

        unifiedSearchMode = true
        _uiState.update {
            it.copy(
                query = query,
                showResults = true,
                queryTooShort = !SearchQueryMatcher.isLongEnough(query),
                jasaResults = if (SearchQueryMatcher.isLongEnough(query)) it.jasaResults else emptyList(),
                projectResults = if (SearchQueryMatcher.isLongEnough(query)) it.projectResults else emptyList(),
                resultsErrorMessage = null,
                isResultsLoading = false
            )
        }

        if (!SearchQueryMatcher.isLongEnough(query)) {
            searchJob?.cancel()
            return
        }

        scheduleUnifiedSearch()
    }

    fun onShowFilter() = _uiState.update { it.copy(showFilterSheet = true) }
    fun onDismissFilter() = _uiState.update { it.copy(showFilterSheet = false) }

    fun onCategorySelected(category: String) =
        _uiState.update { it.copy(filter = it.filter.copy(selectedCategory = category)) }

    fun onMinPriceChanged(value: String) =
        _uiState.update { it.copy(filter = it.filter.copy(minPrice = value.filter { c -> c.isDigit() }.take(9))) }

    fun onMaxPriceChanged(value: String) =
        _uiState.update { it.copy(filter = it.filter.copy(maxPrice = value.filter { c -> c.isDigit() }.take(9))) }

    fun onSortSelected(option: SortOption) =
        _uiState.update { it.copy(filter = it.filter.copy(sortBy = option)) }

    fun onApplyFilter() {
        filtersApplied = true
        unifiedSearchMode = true
        _uiState.update {
            it.copy(
                showFilterSheet = false,
                showResults = true,
                queryTooShort = false
            )
        }
        refreshResults()
    }

    fun retryResults() = refreshResults()

    fun refreshResults() {
        when {
            shouldLoadJasaBrowseOnly() -> loadJasaBrowseResults()
            _uiState.value.query.isBlank() -> loadBrowseResults()
            else -> loadUnifiedResults()
        }
    }

    private fun shouldLoadJasaBrowseOnly(): Boolean =
        _uiState.value.query.isBlank() &&
            !unifiedSearchMode &&
            searchFocus == Routes.SEARCH_FOCUS_JASA

    private fun scheduleUnifiedSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            val query = _uiState.value.query
            if (!SearchQueryMatcher.isLongEnough(query)) return@launch
            loadUnifiedResults()
        }
    }

    private fun loadJasaBrowseResults() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isResultsLoading = true,
                    resultsErrorMessage = null,
                    projectResults = emptyList()
                )
            }
            val state = _uiState.value
            val filters = buildJasaFilters(state)
            when (val result = getJasaListUseCase(filters)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            jasaResults = result.data.map(::toJasaSearchResultItem),
                            projectResults = emptyList(),
                            isResultsLoading = false,
                            resultsErrorMessage = null,
                            showResults = true
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            jasaResults = emptyList(),
                            projectResults = emptyList(),
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

    private fun loadBrowseResults() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isResultsLoading = true, resultsErrorMessage = null) }
            val state = _uiState.value
            val jasaFilters = buildJasaFilters(state)
            val projectFilters = buildProjectFilters(state)

            val (jasaResult, projectResult) = coroutineScope {
                val jasaDeferred = async { getJasaListUseCase(jasaFilters) }
                val projectDeferred = async { getProjectListUseCase(projectFilters) }
                Pair(jasaDeferred.await(), projectDeferred.await())
            }

            val jasaItems = (jasaResult as? Resource.Success)?.data.orEmpty()
            val projectItems = (projectResult as? Resource.Success)?.data.orEmpty()
            val jasaError = (jasaResult as? Resource.Error)?.message
            val projectError = (projectResult as? Resource.Error)?.message

            val errorMessage = when {
                jasaItems.isEmpty() && projectItems.isEmpty() && jasaError != null && projectError != null ->
                    jasaError
                jasaItems.isEmpty() && projectItems.isEmpty() && jasaError != null -> jasaError
                jasaItems.isEmpty() && projectItems.isEmpty() && projectError != null -> projectError
                else -> null
            }

            _uiState.update {
                it.copy(
                    jasaResults = jasaItems.map(::toJasaSearchResultItem),
                    projectResults = projectItems.map(::toProjectSearchResultItem),
                    isResultsLoading = false,
                    resultsErrorMessage = errorMessage,
                    showResults = true,
                    queryTooShort = false
                )
            }
        }
    }

    private fun loadUnifiedResults() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isResultsLoading = true, resultsErrorMessage = null) }
            val state = _uiState.value
            val jasaFilters = buildJasaFilters(state)
            val projectFilters = buildProjectFilters(state)

            val (jasaResult, projectResult) = coroutineScope {
                val jasaDeferred = async { getJasaListUseCase(jasaFilters) }
                val projectDeferred = async { getProjectListUseCase(projectFilters) }
                Pair(jasaDeferred.await(), projectDeferred.await())
            }

            val jasaItems = (jasaResult as? Resource.Success)?.data.orEmpty().let { items ->
                if (SearchQueryMatcher.isLongEnough(state.query)) {
                    SearchQueryMatcher.filterJasa(items, state.query.trim())
                } else {
                    items
                }
            }
            val projectItems = (projectResult as? Resource.Success)?.data.orEmpty().let { items ->
                if (SearchQueryMatcher.isLongEnough(state.query)) {
                    SearchQueryMatcher.filterProjects(items, state.query.trim())
                } else {
                    items
                }
            }
            val jasaError = (jasaResult as? Resource.Error)?.message
            val projectError = (projectResult as? Resource.Error)?.message

            val errorMessage = when {
                jasaItems.isEmpty() && projectItems.isEmpty() && jasaError != null && projectError != null ->
                    jasaError
                jasaItems.isEmpty() && projectItems.isEmpty() && jasaError != null -> jasaError
                jasaItems.isEmpty() && projectItems.isEmpty() && projectError != null -> projectError
                else -> null
            }

            _uiState.update {
                it.copy(
                    jasaResults = jasaItems.map(::toJasaSearchResultItem),
                    projectResults = projectItems.map(::toProjectSearchResultItem),
                    isResultsLoading = false,
                    resultsErrorMessage = errorMessage,
                    showResults = true,
                    queryTooShort = false
                )
            }
        }
    }

    private fun buildJasaFilters(state: SearchUiState) = JasaListFilters(
        search = state.query.trim().takeIf { SearchQueryMatcher.isLongEnough(it) },
        kategoriId = resolveKategoriId(state),
        minPrice = state.filter.minPrice.toDoubleOrNull().takeIf { filtersApplied },
        maxPrice = state.filter.maxPrice.toDoubleOrNull().takeIf { filtersApplied },
        sort = state.filter.sortBy.toApiSort().takeIf { filtersApplied },
        includeRelatedSkills = resolveKategoriId(state) != null && filtersApplied
    )

    private fun buildProjectFilters(state: SearchUiState) = ProjectListFilters(
        search = state.query.trim().takeIf { SearchQueryMatcher.isLongEnough(it) },
        kategoriId = resolveKategoriId(state).takeIf { filtersApplied },
        minBudget = state.filter.minPrice.toDoubleOrNull().takeIf { filtersApplied },
        maxBudget = state.filter.maxPrice.toDoubleOrNull().takeIf { filtersApplied },
        includeRelatedSkills = resolveKategoriId(state) != null && filtersApplied
    )

    private fun resolveKategoriId(state: SearchUiState): String? =
        kategoriItems
            .firstOrNull { it.name == state.filter.selectedCategory }
            ?.id
            ?.takeIf { state.filter.selectedCategory != ALL_CATEGORIES }

    private fun toJasaSearchResultItem(jasa: Jasa) = SearchResultItem(
        id = jasa.id,
        title = jasa.title,
        workerName = jasa.workerName.ifBlank { "Freelancer" },
        rating = jasa.rating.toFloat().takeIf { it > 0f } ?: jasa.workerRating.toFloat(),
        reviewCount = jasa.reviewCount,
        price = CurrencyFormatter.formatRupiah(jasa.price),
        duration = "-",
        imageUrl = jasa.imageUrl,
        type = SearchResultType.JASA
    )

    private fun toProjectSearchResultItem(project: Project) = SearchResultItem(
        id = project.id,
        title = project.title,
        workerName = project.clientName.ifBlank { "Klien" },
        rating = 0f,
        reviewCount = 0,
        price = CurrencyFormatter.formatRupiah(project.budget),
        duration = formatDeadline(project.deadline),
        imageUrl = project.imageUrl,
        type = SearchResultType.PROYEK
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
