package com.app.garapan.presentation.screen.public_profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Portofolio
import com.app.garapan.domain.model.PublicProfile
import com.app.garapan.domain.usecase.GetPortofolioUseCase
import com.app.garapan.domain.usecase.GetPublicProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublicPortfolioItem(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val projectUrl: String? = null
)

data class PublicProfileUiState(
    val userId: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val profile: PublicProfile? = null,
    val portfolioItems: List<PublicPortfolioItem> = emptyList(),
    val isPortfolioLoading: Boolean = false
)

@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPublicProfileUseCase: GetPublicProfileUseCase,
    private val getPortofolioUseCase: GetPortofolioUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublicProfileUiState())
    val uiState: StateFlow<PublicProfileUiState> = _uiState.asStateFlow()

    init {
        val userId = savedStateHandle.get<String>("userId").orEmpty()
        _uiState.update { it.copy(userId = userId) }
        if (userId.isNotBlank()) {
            loadProfile(userId)
        } else {
            _uiState.update { it.copy(errorMessage = "Profil tidak ditemukan") }
        }
    }

    fun retry() {
        val userId = _uiState.value.userId
        if (userId.isNotBlank()) loadProfile(userId)
    }

    private fun loadProfile(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getPublicProfileUseCase(userId)) {
                is Resource.Success -> {
                    val profile = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            profile = profile
                        )
                    }
                    profile.mahasiswaId?.let { loadPortfolio(it) }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            profile = null,
                            portfolioItems = emptyList()
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadPortfolio(mahasiswaId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPortfolioLoading = true) }
            when (val result = getPortofolioUseCase(mahasiswaId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isPortfolioLoading = false,
                            portfolioItems = result.data.map(::toPortfolioItem)
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isPortfolioLoading = false, portfolioItems = emptyList()) }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun toPortfolioItem(item: Portofolio) = PublicPortfolioItem(
        id = item.id,
        title = item.title,
        description = item.description,
        imageUrl = item.imageUrl,
        projectUrl = item.projectUrl?.takeIf { it.isNotBlank() }
    )
}
