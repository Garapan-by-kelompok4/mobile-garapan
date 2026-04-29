package com.app.garapan.presentation.screen.profile_services

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ProfileServiceItem(
    val id: String,
    val title: String,
    val budget: String,
    val category: String,
    val deadline: String,
    val teamSize: String,
    val status: String
)

data class ProfileServicesUiState(
    val skills: List<String> = emptyList(),
    val services: List<ProfileServiceItem> = emptyList()
)

@HiltViewModel
class ProfileServicesViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileServicesUiState(
            skills = listOf(
                "Web Development",
                "Mobile Apps",
                "UI/UX Design",
                "Data Science"
            ),
            services = listOf(
                ProfileServiceItem(
                    id = "service-1",
                    title = "Platform e-Learning Interaktif untuk Bimbingan Belajar",
                    budget = "Rp 5.000.000 - Rp 8.000.000",
                    category = "Ed-Tech",
                    deadline = "20 April 2026",
                    teamSize = "Tim (2-3 Orang)",
                    status = "Mencari"
                ),
                ProfileServiceItem(
                    id = "service-2",
                    title = "Aplikasi Manajemen Inventaris Gudang",
                    budget = "Rp 3.000.000 - Rp 5.000.000",
                    category = "Mobile Dev",
                    deadline = "18 Mei 2026",
                    teamSize = "Tim (1-2 Orang)",
                    status = "Selesai"
                )
            )
        )
    )
    val uiState: StateFlow<ProfileServicesUiState> = _uiState.asStateFlow()

    fun onDeleteService(serviceId: String) {
        _uiState.update { state ->
            state.copy(
                services = state.services.filterNot { service -> service.id == serviceId }
            )
        }
    }
}
