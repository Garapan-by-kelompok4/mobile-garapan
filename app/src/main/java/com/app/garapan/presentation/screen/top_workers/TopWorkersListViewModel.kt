package com.app.garapan.presentation.screen.top_workers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.TopWorker
import com.app.garapan.domain.usecase.GetTopWorkersUseCase
import com.app.garapan.presentation.screen.home.TopWorkerItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TopWorkersListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val workers: List<TopWorkerItem> = emptyList()
)

@HiltViewModel
class TopWorkersListViewModel @Inject constructor(
    private val getTopWorkersUseCase: GetTopWorkersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopWorkersListUiState())
    val uiState: StateFlow<TopWorkersListUiState> = _uiState.asStateFlow()

    init {
        loadWorkers()
    }

    fun retry() = loadWorkers()

    private fun loadWorkers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getTopWorkersUseCase()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            workers = result.data
                                .filter { it.rating > 0f }
                                .map(::toTopWorkerItem)
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            workers = emptyList()
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun toTopWorkerItem(worker: TopWorker) = TopWorkerItem(
        id = worker.mahasiswaId,
        userId = worker.userId,
        name = worker.displayName,
        skill = worker.skills.firstOrNull() ?: worker.university.ifBlank { "Freelancer IT" },
        rating = worker.rating,
        projectsDone = worker.completedOrders,
        avatarUrl = worker.avatarUrl
    )
}
