package com.app.garapan.presentation.screen.search

import com.app.garapan.domain.model.JasaStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchQueryMatcherTest {

    @Test
    fun `requires at least three characters for text search`() {
        assertFalse(SearchQueryMatcher.isLongEnough("a"))
        assertFalse(SearchQueryMatcher.isLongEnough("ah"))
        assertTrue(SearchQueryMatcher.isLongEnough("aha"))
    }

    @Test
    fun `does not apply text filter when query is shorter than three characters`() {
        val jasa = sampleJasa(description = "tubg sahur")
        assertEquals(1, SearchQueryMatcher.filterJasa(listOf(jasa), "ah").size)
    }

    @Test
    fun `drops irrelevant backend rows for meaningful queries`() {
        val jasa = sampleJasa(title = "testing lag gak", description = "qwer")
        assertTrue(SearchQueryMatcher.filterJasa(listOf(jasa), "tung").isEmpty())
    }

    @Test
    fun `matches title and worker name`() {
        val jasa = sampleJasa(description = "tubg sahur")
        assertEquals(1, SearchQueryMatcher.filterJasa(listOf(jasa), "tung").size)
        assertEquals(1, SearchQueryMatcher.filterJasa(listOf(jasa), "prat").size)
    }

    @Test
    fun `ranks title matches above description matches`() {
        val stronger = sampleJasa(title = "tung tung", description = "other")
        val weaker = sampleJasa(title = "other", description = "mentions tung tung later")
        val ranked = SearchQueryMatcher.filterJasa(listOf(weaker, stronger), "tung")
        assertEquals("tung tung", ranked.first().title)
    }

    @Test
    fun `returns all items when query is not applied`() {
        val items = listOf(
            sampleJasa(title = "alpha"),
            sampleJasa(title = "beta")
        )
        assertEquals(2, SearchQueryMatcher.filterJasa(items, "a").size)
    }

    private fun sampleJasa(
        title: String = "tung tung",
        description: String = "tubg sahur"
    ) = com.app.garapan.domain.model.Jasa(
        id = "1",
        mahasiswaId = "m1",
        kategoriId = "k1",
        title = title,
        description = description,
        price = 750_000.0,
        imageUrl = "",
        status = JasaStatus.ACTIVE,
        workerName = "Andi Pratama"
    )
}
