package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.ArtikelAuthorDto
import com.app.garapan.data.remote.dto.ArtikelDto
import com.app.garapan.data.remote.dto.ArtikelRecommendationDto
import org.junit.Assert.assertEquals
import org.junit.Test

class ArtikelMappersTest {

    @Test
    fun `maps article cms metadata and author`() {
        val article = ArtikelDto(
            id = "artikel-1",
            adminId = "admin-1",
            title = "Judul",
            content = "<p>Isi artikel</p>",
            imageUrl = "https://example.com/cover.jpg",
            category = "Tips Karir",
            tags = listOf("freelance", "career"),
            seoDescription = "Ringkasan SEO",
            views = 12,
            publishedAt = "2026-07-02T13:17:55.727Z",
            createdAt = "2026-07-02T13:17:55.232Z",
            updatedAt = "2026-07-03T07:09:58.220Z",
            status = "Published",
            author = ArtikelAuthorDto(
                name = "ini admin",
                role = "Editor",
                avatarUrl = "https://example.com/avatar.jpg"
            )
        ).toDomain()

        assertEquals("Tips Karir", article.category)
        assertEquals(listOf("freelance", "career"), article.tags)
        assertEquals("Ringkasan SEO", article.seoDescription)
        assertEquals(12, article.views)
        assertEquals("Published", article.status)
        assertEquals("ini admin", article.author.name)
        assertEquals("Editor", article.author.role)
        assertEquals("https://example.com/avatar.jpg", article.author.avatarUrl)
    }

    @Test
    fun `maps recommendation excerpt and metadata`() {
        val recommendation = ArtikelRecommendationDto(
            id = "artikel-2",
            title = "Artikel Terkait",
            excerpt = "Ringkasan rekomendasi",
            imageUrl = null,
            category = "Tutorial",
            publishedAt = "2026-07-01T10:00:00.000Z"
        ).toDomain()

        assertEquals("artikel-2", recommendation.id)
        assertEquals("Artikel Terkait", recommendation.title)
        assertEquals("Ringkasan rekomendasi", recommendation.excerpt)
        assertEquals("Tutorial", recommendation.category)
    }
}
