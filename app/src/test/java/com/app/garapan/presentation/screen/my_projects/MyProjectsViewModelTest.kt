package com.app.garapan.presentation.screen.my_projects

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreateProjectParams
import com.app.garapan.domain.model.Pesanan
import com.app.garapan.domain.model.PesananStatus
import com.app.garapan.domain.model.Project
import com.app.garapan.domain.model.ProjectListFilters
import com.app.garapan.domain.model.ProjectProposal
import com.app.garapan.domain.model.ProjectStatus
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.TakenProject
import com.app.garapan.domain.model.UpdateProjectParams
import com.app.garapan.domain.model.User
import com.app.garapan.domain.repository.ProjectRepository
import com.app.garapan.domain.repository.SessionRepository
import com.app.garapan.domain.usecase.DeleteProjectUseCase
import com.app.garapan.domain.usecase.GetMyProjectsUseCase
import com.app.garapan.domain.usecase.GetMyTakenProjectsUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyProjectsViewModelTest {

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
    fun `mahasiswa shows pesanan status instead of closed project status`() = runTest {
        val repository = RecordingProjectRepository(
            myProjects = emptyList(),
            takenProjects = listOf(
                TakenProject(
                    project = Project(
                        id = "taken-project",
                        klienId = "",
                        kategoriId = "",
                        title = "landing page",
                        description = "",
                        budget = 4_500_000.0,
                        deadline = "2026-07-22T00:00:00.000Z",
                        status = ProjectStatus.CLOSED
                    ),
                    orderId = "order-1",
                    orderStatus = PesananStatus.IN_PROGRESS
                )
            )
        )
        val viewModel = createViewModel(repository = repository, role = Role.MAHASISWA)

        advanceUntilIdle()

        assertEquals("DIPROSES", viewModel.uiState.value.projects.first().status)
        assertEquals("order-1", viewModel.uiState.value.projects.first().orderId)
    }

    @Test
    fun `mahasiswa without pesanan has no order id for project detail navigation`() = runTest {
        val repository = RecordingProjectRepository(
            myProjects = listOf(project(id = "posted-project")),
            takenProjects = listOf(
                TakenProject(
                    project = project(id = "taken-project", title = "Website Company Profile"),
                    orderStatus = null
                )
            )
        )
        val viewModel = createViewModel(repository = repository, role = Role.MAHASISWA)

        advanceUntilIdle()

        assertEquals("Proyek Diambil", viewModel.uiState.value.screenTitle)
        assertEquals(1, viewModel.uiState.value.projects.size)
        assertEquals("taken-project", viewModel.uiState.value.projects.first().id)
        assertEquals("Website Company Profile", viewModel.uiState.value.projects.first().title)
        assertEquals("DITERIMA", viewModel.uiState.value.projects.first().status)
        assertEquals(null, viewModel.uiState.value.projects.first().orderId)
        assertFalse(viewModel.uiState.value.canDelete)
        assertEquals(0, repository.myProjectListCalls)
        assertEquals(1, repository.takenProjectCalls)
    }

    @Test
    fun `klien loads posted projects`() = runTest {
        val repository = RecordingProjectRepository(
            myProjects = listOf(project(id = "posted-project", title = "Poster Proyek")),
            takenProjects = emptyList()
        )
        val viewModel = createViewModel(repository = repository, role = Role.KLIEN)

        advanceUntilIdle()

        assertEquals("Proyek Saya", viewModel.uiState.value.screenTitle)
        assertEquals(1, viewModel.uiState.value.projects.size)
        assertEquals("posted-project", viewModel.uiState.value.projects.first().id)
        assertEquals(1, repository.myProjectListCalls)
        assertEquals(0, repository.takenProjectCalls)
    }

    private fun createViewModel(
        repository: RecordingProjectRepository,
        role: Role
    ): MyProjectsViewModel {
        val sessionRepository = FakeSessionRepository(role)
        return MyProjectsViewModel(
            getMyProjectsUseCase = GetMyProjectsUseCase(repository),
            getMyTakenProjectsUseCase = GetMyTakenProjectsUseCase(repository),
            deleteProjectUseCase = DeleteProjectUseCase(repository),
            observeCurrentUserUseCase = ObserveCurrentUserUseCase(sessionRepository)
        )
    }

    private fun project(id: String, title: String = "Proyek") = Project(
        id = id,
        klienId = "klien-1",
        kategoriId = "kategori-1",
        title = title,
        description = "Deskripsi",
        budget = 1_000_000.0,
        deadline = "2026-08-01T00:00:00.000Z",
        status = ProjectStatus.OPEN,
        kategoriName = "Web"
    )

    private class FakeSessionRepository(
        role: Role
    ) : SessionRepository {
        private val user = user(role)
        private val _currentUser = MutableStateFlow<User?>(user)
        override val currentUser: StateFlow<User?> = _currentUser

        override fun peekCurrentUser(): User? = _currentUser.value

        override fun setUser(user: User) {
            _currentUser.value = user
        }

        override suspend fun restoreCachedUser(): User? = _currentUser.value

        override fun clear() {
            _currentUser.value = null
        }

        private fun user(role: Role) = User(
            id = "user-1",
            email = "user@example.com",
            role = role,
            emailVerified = true,
            deviceToken = null,
            twoFactorEnabled = false,
            createdAt = "2026-01-01T00:00:00.000Z",
            updatedAt = "2026-01-01T00:00:00.000Z",
            mahasiswa = null,
            klien = null
        )
    }

    private class RecordingProjectRepository(
        private val myProjects: List<Project>,
        private val takenProjects: List<TakenProject>
    ) : ProjectRepository {
        var myProjectListCalls = 0
        var takenProjectCalls = 0

        override suspend fun getProjectList(filters: ProjectListFilters): Resource<List<Project>> =
            Resource.Success(emptyList())

        override suspend fun getMyProjectList(filters: ProjectListFilters): Resource<List<Project>> {
            myProjectListCalls++
            return Resource.Success(myProjects)
        }

        override suspend fun getMyTakenProjects(page: Int, limit: Int): Resource<List<TakenProject>> {
            takenProjectCalls++
            return Resource.Success(takenProjects)
        }

        override suspend fun getProjectDetail(id: String): Resource<Project> =
            Resource.Error("unsupported")

        override suspend fun createProject(params: CreateProjectParams): Resource<Project> =
            Resource.Error("unsupported")

        override suspend fun updateProject(id: String, params: UpdateProjectParams): Resource<Project> =
            Resource.Error("unsupported")

        override suspend fun deleteProject(id: String): Resource<Unit> = Resource.Success(Unit)

        override suspend fun submitProposal(
            projectId: String,
            message: String,
            proposedPrice: Double
        ): Resource<ProjectProposal> = Resource.Error("unsupported")

        override suspend fun withdrawProposal(projectId: String): Resource<Unit> =
            Resource.Error("unsupported")

        override suspend fun getProjectProposals(projectId: String): Resource<List<ProjectProposal>> =
            Resource.Success(emptyList())

        override suspend fun getMyProposals(page: Int, limit: Int): Resource<List<ProjectProposal>> =
            Resource.Success(emptyList())

        override suspend fun acceptProposal(
            projectId: String,
            proposalId: String
        ): Resource<Pesanan> = Resource.Error("unsupported")

        override suspend fun rejectProposal(projectId: String, proposalId: String): Resource<Unit> =
            Resource.Error("unsupported")
    }
}
