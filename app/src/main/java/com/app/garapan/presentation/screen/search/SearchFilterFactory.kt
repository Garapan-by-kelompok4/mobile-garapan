package com.app.garapan.presentation.screen.search

import com.app.garapan.domain.model.JasaListFilters
import com.app.garapan.domain.model.Kategori
import com.app.garapan.domain.model.ProjectListFilters

internal const val ALL_CATEGORIES = "Semua Kategori"

data class SearchRequestFilters(
    val jasa: JasaListFilters,
    val project: ProjectListFilters
)

object SearchFilterFactory {
    fun build(
        state: SearchUiState,
        categories: List<Kategori>,
        filtersApplied: Boolean,
        priceFilterTouched: Boolean = false
    ): SearchRequestFilters {
        val kategoriId = resolveKategoriId(state, categories).takeIf { filtersApplied }
        val minPrice = state.filter.minPrice.toDoubleOrNull().takeIf { filtersApplied && priceFilterTouched }
        val maxPrice = state.filter.maxPrice.toDoubleOrNull().takeIf { filtersApplied && priceFilterTouched }
        val search = state.query.trim().takeIf { SearchQueryMatcher.isLongEnough(it) }

        return SearchRequestFilters(
            jasa = JasaListFilters(
                search = search,
                kategoriId = kategoriId,
                minPrice = minPrice,
                maxPrice = maxPrice,
                sort = state.filter.sortBy.toJasaApiSort().takeIf { filtersApplied },
                includeRelatedSkills = false
            ),
            project = ProjectListFilters(
                search = search,
                kategoriId = kategoriId,
                minBudget = minPrice,
                maxBudget = maxPrice,
                sort = state.filter.sortBy.toProjectApiSort().takeIf { filtersApplied },
                includeRelatedSkills = false
            )
        )
    }

    private fun resolveKategoriId(state: SearchUiState, categories: List<Kategori>): String? =
        categories
            .firstOrNull { it.name == state.filter.selectedCategory }
            ?.id
            ?.takeIf { state.filter.selectedCategory != ALL_CATEGORIES }

    private fun SortOption.toJasaApiSort(): String = when (this) {
        SortOption.TERBARU -> "newest"
        SortOption.RATING_TERTINGGI -> "rating_desc"
        SortOption.HARGA_TERENDAH -> "price_asc"
    }

    private fun SortOption.toProjectApiSort(): String? = when (this) {
        SortOption.TERBARU -> "newest"
        SortOption.RATING_TERTINGGI -> "rating_desc"
        SortOption.HARGA_TERENDAH -> "budget_asc"
    }
}
