package com.app.garapan.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectProposalTakenProjectTest {

    @Test
    fun `accepted proposal maps to taken project using agreed price`() {
        val proposal = ProjectProposal(
            id = "proposal-1",
            projectId = "project-1",
            mahasiswaId = "mahasiswa-1",
            message = "Siap mengerjakan",
            proposedPrice = 750_000.0,
            status = ProposalStatus.ACCEPTED,
            projectTitle = "Landing Page",
            projectBudget = 1_000_000.0,
            projectDeadline = "2026-08-01T00:00:00.000Z",
            projectStatus = ProjectStatus.CLOSED,
            projectImageUrl = "https://example.com/project.png"
        )

        val project = proposal.toTakenProject()

        assertEquals("project-1", project.id)
        assertEquals("Landing Page", project.title)
        assertEquals(750_000.0, project.budget, 0.0)
        assertEquals("2026-08-01T00:00:00.000Z", project.deadline)
        assertEquals(ProjectStatus.CLOSED, project.status)
        assertEquals("https://example.com/project.png", project.imageUrl)
    }
}
