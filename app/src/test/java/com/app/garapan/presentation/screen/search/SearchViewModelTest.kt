package com.app.garapan.presentation.screen.search

import androidx.lifecycle.SavedStateHandle
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreateJasaParams
import com.app.garapan.domain.model.CreateProjectParams
import com.app.garapan.domain.model.Jasa
import com.app.garapan.domain.model.JasaListFilters
import com.app.garapan.domain.model.JasaStatus
import com.app.garapan.domain.model.Kategori
import com.app.garapan.domain.model.Pesanan
import com.app.garapan.domain.model.Project
import com.app.garapan.domain.model.ProjectListFilters
import com.app.garapan.domain.model.ProjectProposal
import com.app.garapan.domain.model.UpdateJasaParams
import com.app.garapan.domain.model.UpdateProjectParams
import com.app.garapan.domain.repository.JasaRepository
import com.app.garapan.domain.repository.KategoriRepository
import com.app.garapan.domain.repository.ProjectRepository
import com.app.garapan.domain.usecase.GetJasaListUseCase
import com.app.garapan.domain.usecase.GetKategoriListUseCase
import com.app.garapan.domain.usecase.GetProjectListUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `keeps backend skill matched jasa results that do not locally match text fields`() = runTest {
        val jasaRepository = FakeJasaRepository()
        val viewModel = SearchViewModel(
            savedStateHandle = SavedStateHandle(),
            getKategoriListUseCase = GetKategoriListUseCase(FakeKategoriRepository()),
            getJasaListUseCase = GetJasaListUseCase(jasaRepository),
            getProjectListUseCase = GetProjectListUseCase(FakeProjectRepository())
        )
        advanceUntilIdle()

        viewModel.onQueryChanged("dev")
        advanceTimeBy(SEARCH_DEBOUNCE_MS)
        advanceUntilIdle()

        assertEquals("skill-match", viewModel.uiState.value.jasaResults.single().id)
    }

    private class FakeKategoriRepository : KategoriRepository {
        override suspend fun getKategoriList(): Resource<List<Kategori>> =
            Resource.Success(
                listOf(Kategori(id = "cat-design", name = "UI/UX Design", icon = "palette"))
            )
    }

    private class FakeJasaRepository : JasaRepository {
        override suspend fun getJasaList(filters: JasaListFilters): Resource<List<Jasa>> =
            if (filters.search == "dev") Resource.Success(listOf(skillMatchedJasa())) else Resource.Success(emptyList())

        override suspend fun getMyJasaList(filters: JasaListFilters): Resource<List<Jasa>> = unused()
        override suspend fun getJasaDetail(id: String): Resource<Jasa> = unused()
        override suspend fun createJasa(params: CreateJasaParams): Resource<Jasa> = unused()
        override suspend fun updateJasa(id: String, params: UpdateJasaParams): Resource<Jasa> = unused()
        override suspend fun deleteJasa(id: String): Resource<Unit> = unused()

        private fun skillMatchedJasa() = Jasa(
            id = "skill-match",
            mahasiswaId = "m1",
            kategoriId = "cat-design",
            title = "Landing page polish",
            description = "Interface cleanup",
            price = 500_000.0,
            imageUrl = "",
            status = JasaStatus.ACTIVE,
            kategoriName = "UI/UX Design",
            workerName = "Rina"
        )
    }

    private class FakeProjectRepository : ProjectRepository {
        override suspend fun getProjectList(filters: ProjectListFilters): Resource<List<Project>> =
            Resource.Success(emptyList())

        override suspend fun getMyProjectList(filters: ProjectListFilters): Resource<List<Project>> = unused()
        override suspend fun getProjectDetail(id: String): Resource<Project> = unused()
        override suspend fun createProject(params: CreateProjectParams): Resource<Project> = unused()
        override suspend fun updateProject(id: String, params: UpdateProjectParams): Resource<Project> = unused()
        override suspend fun deleteProject(id: String): Resource<Unit> = unused()
        override suspend fun submitProposal(projectId: String, message: String, proposedPrice: Double): Resource<ProjectProposal> = unused()
        override suspend fun withdrawProposal(projectId: String): Resource<Unit> = unused()
        override suspend fun getProjectProposals(projectId: String): Resource<List<ProjectProposal>> = unused()
        override suspend fun getMyProposals(page: Int, limit: Int): Resource<List<ProjectProposal>> = unused()
        override suspend fun getMyTakenProjects(page: Int, limit: Int): Resource<List<com.app.garapan.domain.model.TakenProject>> = unused()
        override suspend fun acceptProposal(projectId: String, proposalId: String): Resource<Pesanan> = unused()
        override suspend fun rejectProposal(projectId: String, proposalId: String): Resource<Unit> = unused()
    }
}

private fun unused(): Nothing = error("unused")
