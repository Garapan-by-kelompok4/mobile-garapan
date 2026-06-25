package com.app.garapan.presentation.screen.blog_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Artikel
import com.app.garapan.domain.usecase.GetArtikelDetailUseCase
import com.app.garapan.domain.usecase.GetArtikelListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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

sealed class BlogBodyBlock {
    data class Paragraph(val text: String) : BlogBodyBlock()
    data class Heading(val number: Int, val text: String) : BlogBodyBlock()
    data class Quote(val text: String) : BlogBodyBlock()
}

data class RecommendationItem(
    val id: String,
    val category: String,
    val title: String,
    val excerpt: String
)

data class BlogDetailUiState(
    val blogId: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val category: String = "BLOG",
    val date: String = "",
    val title: String = "",
    val authorName: String = "Admin GARAPAN",
    val authorRole: String = "Tech & Career Editor",
    val readTime: String = "",
    val heroSubtitle: String = "",
    val body: List<BlogBodyBlock> = emptyList(),
    val recommendations: List<RecommendationItem> = emptyList()
)

@HiltViewModel
class BlogDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getArtikelDetailUseCase: GetArtikelDetailUseCase,
    private val getArtikelListUseCase: GetArtikelListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlogDetailUiState())
    val uiState: StateFlow<BlogDetailUiState> = _uiState.asStateFlow()

    init {
        val blogId = savedStateHandle.get<String>("blogId").orEmpty()
        _uiState.update { it.copy(blogId = blogId) }
        if (blogId.isNotBlank()) {
            loadArtikel(blogId)
        } else {
            _uiState.update { it.copy(errorMessage = "Artikel tidak ditemukan") }
        }
    }

    fun retry() {
        val blogId = _uiState.value.blogId
        if (blogId.isNotBlank()) loadArtikel(blogId)
    }

    private fun loadArtikel(blogId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getArtikelDetailUseCase(blogId)) {
                is Resource.Success -> {
                    val artikel = result.data
                    val body = parseContent(artikel.content)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            category = "BLOG",
                            date = formatPublishedDate(artikel.publishedAt),
                            title = artikel.title,
                            readTime = "${estimateReadTime(artikel.content)} baca",
                            heroSubtitle = body.firstOrNull()
                                ?.let { block -> (block as? BlogBodyBlock.Paragraph)?.text }
                                ?.take(120)
                                .orEmpty(),
                            body = body
                        )
                    }
                    loadRecommendations(blogId)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            body = emptyList(),
                            recommendations = emptyList()
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadRecommendations(currentId: String) {
        viewModelScope.launch {
            when (val result = getArtikelListUseCase(limit = 5)) {
                is Resource.Success -> {
                    val recommendations = result.data
                        .filter { it.id != currentId }
                        .take(2)
                        .map(::toRecommendationItem)
                    _uiState.update { it.copy(recommendations = recommendations) }
                }
                is Resource.Error, Resource.Loading -> Unit
            }
        }
    }

    private fun toRecommendationItem(artikel: Artikel) = RecommendationItem(
        id = artikel.id,
        category = "BLOG",
        title = artikel.title,
        excerpt = artikel.content.lines().firstOrNull { it.isNotBlank() }?.take(100).orEmpty()
    )

    private fun parseContent(content: String): List<BlogBodyBlock> {
        if (content.isBlank()) return emptyList()

        return content.trim()
            .split(Regex("\n\n+"))
            .mapNotNull { block ->
                val trimmed = block.trim()
                when {
                    trimmed.isBlank() -> null
                    trimmed.startsWith(">") -> BlogBodyBlock.Quote(trimmed.removePrefix(">").trim())
                    trimmed.startsWith("## ") -> BlogBodyBlock.Heading(
                        number = 1,
                        text = trimmed.removePrefix("## ").trim()
                    )
                    else -> BlogBodyBlock.Paragraph(trimmed.replace("\n", " "))
                }
            }
    }

    private fun estimateReadTime(content: String): String {
        val minutes = (content.split(Regex("\\s+")).count { it.isNotBlank() } / 200).coerceAtLeast(1)
        return "$minutes menit"
    }

    private fun formatPublishedDate(publishedAt: String?): String {
        if (publishedAt.isNullOrBlank()) return ""
        return runCatching {
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
            Instant.parse(publishedAt).atZone(ZoneId.systemDefault()).format(formatter)
        }.getOrDefault("")
    }
}
