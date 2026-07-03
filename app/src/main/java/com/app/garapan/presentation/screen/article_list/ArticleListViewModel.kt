package com.app.garapan.presentation.screen.article_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Artikel
import com.app.garapan.domain.usecase.GetArtikelListUseCase
import com.app.garapan.presentation.screen.blog_detail.BlogArticleDefaults
import com.app.garapan.presentation.screen.home.BlogItem
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

data class ArticleListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val articles: List<BlogItem> = emptyList()
)

@HiltViewModel
class ArticleListViewModel @Inject constructor(
    private val getArtikelListUseCase: GetArtikelListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleListUiState())
    val uiState: StateFlow<ArticleListUiState> = _uiState.asStateFlow()

    init {
        loadArticles()
    }

    fun retry() = loadArticles()

    private fun loadArticles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getArtikelListUseCase(limit = 50)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            articles = result.data.map(::toBlogItem)
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            articles = emptyList()
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun toBlogItem(artikel: Artikel) = BlogItem(
        id = artikel.id,
        title = artikel.title,
        category = artikel.category.takeIf { !it.isNullOrBlank() } ?: BlogArticleDefaults.CATEGORY,
        date = formatPublishedDate(artikel.publishedAt),
        imageUrl = artikel.imageUrl
    )

    private fun formatPublishedDate(publishedAt: String?): String {
        if (publishedAt.isNullOrBlank()) return ""
        return runCatching {
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
            Instant.parse(publishedAt).atZone(ZoneId.systemDefault()).format(formatter)
        }.getOrDefault("")
    }
}
