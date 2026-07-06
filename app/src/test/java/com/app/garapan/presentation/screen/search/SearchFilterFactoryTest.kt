package com.app.garapan.presentation.screen.search

import com.app.garapan.domain.model.Kategori
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class SearchFilterFactoryTest {

    private val categories = listOf(
        Kategori(id = "cat-devops", name = "DevOps / Cloud", icon = "cloud"),
        Kategori(id = "cat-design", name = "UI/UX Design", icon = "palette")
    )

    @Test
    fun `does not send draft filters before apply`() {
        val state = SearchUiState(
            query = "",
            filter = FilterSortState(selectedCategory = "DevOps / Cloud")
        )

        val filters = SearchFilterFactory.build(state, categories, filtersApplied = false)

        assertNull(filters.jasa.kategoriId)
        assertNull(filters.project.kategoriId)
        assertNull(filters.jasa.minPrice)
        assertNull(filters.jasa.maxPrice)
        assertNull(filters.jasa.sort)
        assertFalse(filters.jasa.includeRelatedSkills)
        assertFalse(filters.project.includeRelatedSkills)
    }

    @Test
    fun `applies strict category without related skills`() {
        val state = SearchUiState(
            query = "",
            filter = FilterSortState(selectedCategory = "DevOps / Cloud")
        )

        val filters = SearchFilterFactory.build(state, categories, filtersApplied = true)

        assertEquals("cat-devops", filters.jasa.kategoriId)
        assertEquals("cat-devops", filters.project.kategoriId)
        assertFalse(filters.jasa.includeRelatedSkills)
        assertFalse(filters.project.includeRelatedSkills)
    }

    @Test
    fun `does not send default price range until price is edited`() {
        val state = SearchUiState(
            filter = FilterSortState(
                selectedCategory = "DevOps / Cloud",
                minPrice = "100000",
                maxPrice = "5000000"
            )
        )

        val filters = SearchFilterFactory.build(
            state = state,
            categories = categories,
            filtersApplied = true,
            priceFilterTouched = false
        )

        assertNull(filters.jasa.minPrice)
        assertNull(filters.jasa.maxPrice)
        assertNull(filters.project.minBudget)
        assertNull(filters.project.maxBudget)
    }

    @Test
    fun `sends price range after price is edited`() {
        val state = SearchUiState(
            filter = FilterSortState(
                minPrice = "250000",
                maxPrice = "750000"
            )
        )

        val filters = SearchFilterFactory.build(
            state = state,
            categories = categories,
            filtersApplied = true,
            priceFilterTouched = true
        )

        assertEquals(250000.0, filters.jasa.minPrice)
        assertEquals(750000.0, filters.jasa.maxPrice)
        assertEquals(250000.0, filters.project.minBudget)
        assertEquals(750000.0, filters.project.maxBudget)
    }

    @Test
    fun `maps sort options for jasa and projects`() {
        val state = SearchUiState(
            filter = FilterSortState(sortBy = SortOption.HARGA_TERENDAH)
        )

        val filters = SearchFilterFactory.build(
            state = state,
            categories = categories,
            filtersApplied = true
        )

        assertEquals("price_asc", filters.jasa.sort)
        assertEquals("budget_asc", filters.project.sort)
        assertEquals("Terbaru", SortOption.TERBARU.label)
    }
}
