package com.app.garapan.presentation.screen.blog_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.ArtikelRecommendation
import com.app.garapan.domain.usecase.GetArtikelDetailUseCase
import com.app.garapan.domain.usecase.GetArtikelRecommendationsUseCase
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

data class BlogInlineStyle(
    val start: Int,
    val end: Int,
    val bold: Boolean = false,
    val italic: Boolean = false
)

sealed class BlogBodyBlock {
    data class Paragraph(val text: String, val styles: List<BlogInlineStyle> = emptyList()) : BlogBodyBlock()
    data class Heading(val level: Int, val text: String, val styles: List<BlogInlineStyle> = emptyList()) : BlogBodyBlock()
    data class Quote(val text: String, val styles: List<BlogInlineStyle> = emptyList()) : BlogBodyBlock()
    data class BulletList(
        val items: List<String>,
        val itemStyles: List<List<BlogInlineStyle>> = emptyList()
    ) : BlogBodyBlock()
    data class OrderedList(
        val items: List<String>,
        val itemStyles: List<List<BlogInlineStyle>> = emptyList()
    ) : BlogBodyBlock()
}

data class RecommendationItem(
    val id: String,
    val category: String,
    val title: String,
    val excerpt: String,
    val imageUrl: String? = null
)

data class BlogDetailUiState(
    val blogId: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val category: String = BlogArticleDefaults.CATEGORY,
    val date: String = "",
    val title: String = "",
    val authorName: String = BlogArticleDefaults.AUTHOR_NAME,
    val authorRole: String = BlogArticleDefaults.AUTHOR_ROLE,
    val authorAvatarUrl: String? = null,
    val heroSubtitle: String = "",
    val imageUrl: String? = null,
    val body: List<BlogBodyBlock> = emptyList(),
    val recommendations: List<RecommendationItem> = emptyList()
)

@HiltViewModel
class BlogDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getArtikelDetailUseCase: GetArtikelDetailUseCase,
    private val getArtikelRecommendationsUseCase: GetArtikelRecommendationsUseCase
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
                    val body = BlogContentFormatter.toBlocks(artikel.content)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            category = artikel.category.orFallbackCategory(),
                            date = formatPublishedDate(artikel.publishedAt),
                            title = artikel.title,
                            heroSubtitle = body.firstOrNull()
                                ?.let { block -> (block as? BlogBodyBlock.Paragraph)?.text }
                                ?.take(120)
                                .orEmpty(),
                            imageUrl = artikel.imageUrl,
                            authorName = artikel.author.name,
                            authorRole = artikel.author.role,
                            authorAvatarUrl = artikel.author.avatarUrl,
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
            when (val result = getArtikelRecommendationsUseCase(id = currentId, limit = 2)) {
                is Resource.Success -> {
                    val recommendations = result.data.map(::toRecommendationItem)
                    _uiState.update { it.copy(recommendations = recommendations) }
                }
                is Resource.Error, Resource.Loading -> Unit
            }
        }
    }

    private fun toRecommendationItem(artikel: ArtikelRecommendation) = RecommendationItem(
        id = artikel.id,
        category = artikel.category.orFallbackCategory(),
        title = artikel.title,
        excerpt = artikel.excerpt,
        imageUrl = artikel.imageUrl
    )

    private fun String?.orFallbackCategory(): String =
        takeIf { !it.isNullOrBlank() } ?: BlogArticleDefaults.CATEGORY

    private fun formatPublishedDate(publishedAt: String?): String {
        if (publishedAt.isNullOrBlank()) return ""
        return runCatching {
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
            Instant.parse(publishedAt).atZone(ZoneId.systemDefault()).format(formatter)
        }.getOrDefault("")
    }
}
