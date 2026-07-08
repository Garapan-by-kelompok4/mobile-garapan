package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Pesanan
import com.app.garapan.domain.model.PesananStatus
import com.app.garapan.domain.model.Project
import com.app.garapan.domain.model.ProjectListFilters
import com.app.garapan.domain.model.ProjectProposal
import com.app.garapan.domain.model.ProjectStatus
import com.app.garapan.domain.model.TakenProject
import com.app.garapan.domain.repository.ProjectRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetMyTakenProjectsUseCaseTest {

    @Test
    fun `loads taken projects from repository`() = runTest {
        val repository = FakeProjectRepository(
            takenProjects = listOf(
                TakenProject(
                    project = Project(
                        id = "project-1",
                        klienId = "",
                        kategoriId = "",
                        title = "landing page",
                        description = "",
                        budget = 4_500_000.0,
                        deadline = "2026-07-22T00:00:00.000Z",
                        status = ProjectStatus.OPEN,
                        kategoriName = "Web Development",
                        clientName = "Garapan Demo Client"
                    ),
                    orderStatus = PesananStatus.IN_PROGRESS
                ),
                TakenProject(
                    project = Project(
                        id = "project-2",
                        klienId = "",
                        kategoriId = "",
                        title = "CMS admin panel",
                        description = "",
                        budget = 2_500_000.0,
                        deadline = "2026-07-15T00:00:00.000Z",
                        status = ProjectStatus.OPEN
                    ),
                    orderStatus = PesananStatus.COMPLETED
                )
            )
        )
        val useCase = GetMyTakenProjectsUseCase(repository)

        val result = useCase(page = 1, limit = 20)

        assertTrue(result is Resource.Success)
        val projects = (result as Resource.Success).data
        assertEquals(2, projects.size)
        assertEquals("landing page", projects[0].project.title)
        assertEquals(PesananStatus.IN_PROGRESS, projects[0].orderStatus)
        assertEquals(PesananStatus.COMPLETED, projects[1].orderStatus)
        assertEquals(1, repository.takenProjectCalls)
    }

    @Test
    fun `returns accepted project without pesanan as null order status`() = runTest {
        val repository = FakeProjectRepository(
            takenProjects = listOf(
                TakenProject(
                    project = Project(
                        id = "project-1",
                        klienId = "",
                        kategoriId = "",
                        title = "fresh website",
                        description = "",
                        budget = 150_000.0,
                        deadline = "2026-07-28T00:00:00.000Z",
                        status = ProjectStatus.OPEN
                    ),
                    orderStatus = null
                )
            )
        )
        val useCase = GetMyTakenProjectsUseCase(repository)

        val result = useCase()

        assertTrue(result is Resource.Success)
        assertNull((result as Resource.Success).data.first().orderStatus)
    }

    private class FakeProjectRepository(
        private val takenProjects: List<TakenProject> = emptyList()
    ) : ProjectRepository by UnsupportedProjectRepository() {
        var takenProjectCalls = 0

        override suspend fun getMyTakenProjects(page: Int, limit: Int): Resource<List<TakenProject>> {
            takenProjectCalls++
            return Resource.Success(takenProjects)
        }
    }

    private open class UnsupportedProjectRepository : ProjectRepository {
        override suspend fun getProjectList(filters: ProjectListFilters): Resource<List<Project>> =
            unsupported()

        override suspend fun getMyProjectList(filters: ProjectListFilters): Resource<List<Project>> =
            unsupported()

        override suspend fun getProjectDetail(id: String): Resource<Project> = unsupported()

        override suspend fun createProject(params: com.app.garapan.domain.model.CreateProjectParams): Resource<Project> =
            unsupported()

        override suspend fun updateProject(
            id: String,
            params: com.app.garapan.domain.model.UpdateProjectParams
        ): Resource<Project> = unsupported()

        override suspend fun deleteProject(id: String): Resource<Unit> = unsupported()

        override suspend fun submitProposal(
            projectId: String,
            message: String,
            proposedPrice: Double
        ): Resource<ProjectProposal> = unsupported()

        override suspend fun withdrawProposal(projectId: String): Resource<Unit> = unsupported()

        override suspend fun getProjectProposals(projectId: String): Resource<List<ProjectProposal>> =
            unsupported()

        override suspend fun getMyProposals(page: Int, limit: Int): Resource<List<ProjectProposal>> =
            unsupported()

        override suspend fun getMyTakenProjects(page: Int, limit: Int): Resource<List<TakenProject>> =
            unsupported()

        override suspend fun acceptProposal(
            projectId: String,
            proposalId: String
        ): Resource<Pesanan> = unsupported()

        override suspend fun rejectProposal(projectId: String, proposalId: String): Resource<Unit> =
            unsupported()

        private fun <T> unsupported(): Resource<T> =
            throw UnsupportedOperationException("Not needed for this test")
    }
}
