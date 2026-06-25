package com.app.garapan.presentation.screen.profile_services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Jasa
import com.app.garapan.domain.model.JasaStatus
import com.app.garapan.domain.usecase.DeleteJasaUseCase
import com.app.garapan.domain.usecase.GetMyJasaListUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import com.app.garapan.presentation.util.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileServiceItem(
    val id: String,
    val title: String,
    val budget: String,
    val category: String,
    val deadline: String,
    val teamSize: String,
    val status: String,
    val imageUrl: String = ""
)

data class ProfileServicesUiState(
    val skills: List<String> = emptyList(),
    val services: List<ProfileServiceItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isDeleting: Boolean = false
)

@HiltViewModel
class ProfileServicesViewModel @Inject constructor(
    private val getMyJasaListUseCase: GetMyJasaListUseCase,
    private val deleteJasaUseCase: DeleteJasaUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileServicesUiState(isLoading = true))
    val uiState: StateFlow<ProfileServicesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                _uiState.update { state ->
                    state.copy(skills = user?.mahasiswa?.skills.orEmpty())
                }
            }
        }
        loadMyJasa()
    }

    fun loadMyJasa() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getMyJasaListUseCase()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            services = result.data.map(::toProfileServiceItem),
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            services = emptyList(),
                            errorMessage = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onDeleteService(serviceId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            when (val result = deleteJasaUseCase(serviceId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isDeleting = false) }
                    loadMyJasa()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isDeleting = false, errorMessage = result.message)
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun toProfileServiceItem(jasa: Jasa) = ProfileServiceItem(
        id = jasa.id,
        title = jasa.title,
        budget = CurrencyFormatter.formatRupiah(jasa.price),
        category = jasa.kategoriName.ifBlank { "Jasa" },
        deadline = "-",
        teamSize = "-",
        status = when (jasa.status) {
            JasaStatus.ACTIVE -> "Aktif"
            JasaStatus.INACTIVE -> "Nonaktif"
        },
        imageUrl = jasa.imageUrl
    )
}
