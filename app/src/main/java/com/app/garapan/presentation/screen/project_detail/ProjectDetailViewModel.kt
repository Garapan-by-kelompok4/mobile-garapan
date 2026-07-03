package com.app.garapan.presentation.screen.project_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Project
import com.app.garapan.domain.model.ProjectProposal
import com.app.garapan.domain.model.ProjectStatus
import com.app.garapan.domain.model.ProposalStatus
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.usecase.AcceptProjectProposalUseCase
import com.app.garapan.domain.usecase.GetMyProposalsUseCase
import com.app.garapan.domain.usecase.GetProjectDetailUseCase
import com.app.garapan.domain.usecase.GetProjectProposalsUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import com.app.garapan.domain.usecase.RejectProjectProposalUseCase
import com.app.garapan.domain.usecase.SubmitProjectProposalUseCase
import com.app.garapan.domain.usecase.WithdrawProjectProposalUseCase
import com.app.garapan.presentation.util.CurrencyFormatter
import com.app.garapan.presentation.util.UserMessageLocalizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class ProjectDetailUiState(
    val id: String = "",
    val category: String = "",
    val title: String = "",
    val deadline: String = "",
    val budget: String = "",
    val teamSize: String = "-",
    val clientName: String = "",
    val clientType: String = "",
    val isVerified: Boolean = true,
    val description: String = "",
    val imageUrl: String = "",
    val status: ProjectStatus = ProjectStatus.OPEN,
    val canEdit: Boolean = false,
    val showEditButton: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isMahasiswa: Boolean = false,
    val canPropose: Boolean = false,
    val myProposalId: String? = null,
    val myProposalStatus: ProposalStatus? = null,
    val proposalMessageInput: String = "",
    val proposalPriceInput: String = "",
    val proposalPriceRangeHint: String = "",
    val isSubmittingProposal: Boolean = false,
    val isWithdrawingProposal: Boolean = false,
    val isKlienOwner: Boolean = false,
    val proposals: List<ProjectProposal> = emptyList(),
    val isLoadingProposals: Boolean = false,
    val respondingProposalId: String? = null
)

sealed interface ProjectDetailEvent {
    data class ShowMessage(val message: String) : ProjectDetailEvent
    data class NavigateToOrder(val pesananId: String) : ProjectDetailEvent
}

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProjectDetailUseCase: GetProjectDetailUseCase,
    private val submitProjectProposalUseCase: SubmitProjectProposalUseCase,
    private val withdrawProjectProposalUseCase: WithdrawProjectProposalUseCase,
    private val getProjectProposalsUseCase: GetProjectProposalsUseCase,
    private val getMyProposalsUseCase: GetMyProposalsUseCase,
    private val acceptProjectProposalUseCase: AcceptProjectProposalUseCase,
    private val rejectProjectProposalUseCase: RejectProjectProposalUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private val projectId: String = savedStateHandle["projectId"] ?: ""

    private var currentRole: Role? = null
    private var currentKlienId: String? = null
    private var loadedProject: Project? = null

    private val _uiState = MutableStateFlow(ProjectDetailUiState(isLoading = true))
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProjectDetailEvent>()
    val events: SharedFlow<ProjectDetailEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                currentRole = user?.role
                currentKlienId = user?.klien?.id
                loadedProject?.let { project ->
                    _uiState.value = project.toUiState().copy(
                        proposalMessageInput = _uiState.value.proposalMessageInput,
                        proposalPriceInput = _uiState.value.proposalPriceInput
                    )
                    loadSideData(project)
                }
            }
        }
        loadProjectDetail()
    }

    fun retry() = loadProjectDetail()

    fun onProposalMessageChanged(text: String) =
        _uiState.update { it.copy(proposalMessageInput = text) }

    fun onProposalPriceChanged(text: String) =
        _uiState.update { it.copy(proposalPriceInput = text.filter(Char::isDigit).take(9)) }

    fun onSubmitProposal() {
        val state = _uiState.value
        if (state.isSubmittingProposal || !state.canPropose) return
        val message = state.proposalMessageInput.trim()
        val price = state.proposalPriceInput.toDoubleOrNull()
        if (message.isEmpty()) {
            _events.tryEmit(ProjectDetailEvent.ShowMessage("Pesan proposal tidak boleh kosong."))
            return
        }
        if (price == null || price <= 0.0) {
            _events.tryEmit(ProjectDetailEvent.ShowMessage("Harga yang diajukan tidak valid."))
            return
        }
        val budget = loadedProject?.budget
        if (budget != null) {
            val minAllowed = budget * MIN_PROPOSAL_RATIO
            if (price < minAllowed || price > budget) {
                _events.tryEmit(
                    ProjectDetailEvent.ShowMessage(
                        "Harga yang diajukan harus antara ${CurrencyFormatter.formatRupiah(minAllowed)} " +
                            "dan ${CurrencyFormatter.formatRupiah(budget)} (sesuai anggaran proyek)."
                    )
                )
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingProposal = true) }
            when (val result = submitProjectProposalUseCase(projectId, message, price)) {
                is Resource.Success -> {
                    val proposal = result.data
                    _uiState.update {
                        it.copy(
                            isSubmittingProposal = false,
                            myProposalId = proposal.id,
                            myProposalStatus = proposal.status
                        )
                    }
                    _events.emit(ProjectDetailEvent.ShowMessage("Proposal berhasil dikirim."))
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isSubmittingProposal = false) }
                    _events.emit(ProjectDetailEvent.ShowMessage(UserMessageLocalizer.localize(result.message)))
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onWithdrawProposal() {
        val state = _uiState.value
        if (state.isWithdrawingProposal || state.myProposalStatus != ProposalStatus.PENDING) return

        viewModelScope.launch {
            _uiState.update { it.copy(isWithdrawingProposal = true) }
            when (val result = withdrawProjectProposalUseCase(projectId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isWithdrawingProposal = false,
                            myProposalStatus = ProposalStatus.WITHDRAWN,
                            canPropose = it.status == ProjectStatus.OPEN
                        )
                    }
                    _events.emit(ProjectDetailEvent.ShowMessage("Proposal ditarik."))
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isWithdrawingProposal = false) }
                    _events.emit(ProjectDetailEvent.ShowMessage(UserMessageLocalizer.localize(result.message)))
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onAcceptProposal(proposalId: String) {
        if (_uiState.value.respondingProposalId != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(respondingProposalId = proposalId) }
            when (val result = acceptProjectProposalUseCase(projectId, proposalId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(respondingProposalId = null) }
                    _events.emit(ProjectDetailEvent.NavigateToOrder(result.data.id))
                    loadProjectDetail()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(respondingProposalId = null) }
                    _events.emit(ProjectDetailEvent.ShowMessage(UserMessageLocalizer.localize(result.message)))
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onRejectProposal(proposalId: String) {
        if (_uiState.value.respondingProposalId != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(respondingProposalId = proposalId) }
            when (val result = rejectProjectProposalUseCase(projectId, proposalId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            respondingProposalId = null,
                            proposals = it.proposals.filterNot { proposal -> proposal.id == proposalId }
                        )
                    }
                    _events.emit(ProjectDetailEvent.ShowMessage("Proposal ditolak."))
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(respondingProposalId = null) }
                    _events.emit(ProjectDetailEvent.ShowMessage(UserMessageLocalizer.localize(result.message)))
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadProjectDetail() {
        if (projectId.isBlank()) {
            _uiState.value = ProjectDetailUiState(
                isLoading = false,
                errorMessage = "Proyek tidak ditemukan."
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getProjectDetailUseCase(projectId)) {
                is Resource.Success -> {
                    loadedProject = result.data
                    _uiState.value = result.data.toUiState()
                    loadSideData(result.data)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UserMessageLocalizer.localize(result.message)
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadSideData(project: Project) {
        val isMahasiswa = currentRole == Role.MAHASISWA
        val isKlienOwner = currentKlienId == project.klienId && currentRole == Role.KLIEN

        if (isMahasiswa) {
            viewModelScope.launch {
                when (val result = getMyProposalsUseCase(page = 1, limit = 50)) {
                    is Resource.Success -> {
                        val mine = result.data.firstOrNull { it.projectId == project.id }
                        _uiState.update {
                            it.copy(
                                myProposalId = mine?.id,
                                myProposalStatus = mine?.status,
                                proposalMessageInput = mine?.message ?: it.proposalMessageInput,
                                proposalPriceInput = mine?.proposedPrice?.let { price ->
                                    price.toLong().toString()
                                } ?: it.proposalPriceInput,
                                canPropose = project.status == ProjectStatus.OPEN &&
                                    mine?.status != ProposalStatus.ACCEPTED &&
                                    mine?.status != ProposalStatus.REJECTED
                            )
                        }
                    }
                    is Resource.Error -> Unit
                    Resource.Loading -> Unit
                }
            }
        }

        if (isKlienOwner && project.status == ProjectStatus.OPEN) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingProposals = true) }
                when (val result = getProjectProposalsUseCase(project.id)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoadingProposals = false,
                                proposals = result.data.filter { proposal ->
                                    proposal.status == ProposalStatus.PENDING
                                }
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoadingProposals = false) }
                    }
                    Resource.Loading -> Unit
                }
            }
        }
    }

    private fun Project.toUiState(): ProjectDetailUiState {
        val isMahasiswa = currentRole == Role.MAHASISWA
        val isKlienOwner = currentKlienId == klienId && currentRole == Role.KLIEN

        return ProjectDetailUiState(
            id = id,
            category = kategoriName.ifBlank { "Proyek" },
            title = title,
            deadline = formatDeadline(deadline),
            budget = CurrencyFormatter.formatRupiah(budget),
            proposalPriceRangeHint = "Rentang harga: ${CurrencyFormatter.formatRupiah(budget * MIN_PROPOSAL_RATIO)} " +
                "- ${CurrencyFormatter.formatRupiah(budget)}",
            clientName = clientName.ifBlank { "Klien" },
            clientType = kategoriName.ifBlank { "Klien" },
            description = description,
            imageUrl = imageUrl,
            status = status,
            canEdit = isKlienOwner && status == ProjectStatus.OPEN,
            showEditButton = isKlienOwner && status == ProjectStatus.OPEN,
            isLoading = false,
            errorMessage = null,
            isMahasiswa = isMahasiswa,
            isKlienOwner = isKlienOwner
        )
    }

    private fun formatDeadline(deadline: String): String {
        if (deadline.isBlank()) return "-"
        return runCatching {
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
            Instant.parse(deadline).atZone(ZoneId.systemDefault()).format(formatter)
        }.getOrDefault(deadline.take(10))
    }

    private companion object {
        const val MIN_PROPOSAL_RATIO = 0.5
    }
}
