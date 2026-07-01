package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.ProjectMahasiswaDto
import com.app.garapan.data.remote.dto.ProjectProposalDto
import com.app.garapan.data.remote.dto.ProjectProposalProjectDto
import com.app.garapan.data.remote.dto.ProjectUserDto
import com.app.garapan.domain.model.ProjectStatus
import com.app.garapan.domain.model.ProposalStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectProposalMappersTest {

    @Test
    fun mapsCoreFieldsAndEmbeddedMahasiswaAndProject() {
        val dto = ProjectProposalDto(
            id = "proposal-1",
            projectId = "project-1",
            mahasiswaId = "mhs-1",
            message = "Saya siap mengerjakan",
            proposedPrice = "250000.00",
            status = "PENDING",
            mahasiswa = ProjectMahasiswaDto(
                id = "mhs-1",
                fullName = "Budi",
                university = "UI",
                rating = 4.5,
                user = ProjectUserDto(id = "user-mhs", name = "Budi", displayName = "Budi")
            ),
            project = ProjectProposalProjectDto(
                id = "project-1",
                title = "Landing Page",
                budget = "500000.00",
                deadline = "2026-12-31T00:00:00.000Z",
                status = "OPEN",
                imageUrl = null
            )
        )

        val proposal = dto.toDomain()

        assertEquals("proposal-1", proposal.id)
        assertEquals(250000.0, proposal.proposedPrice, 0.0)
        assertEquals(ProposalStatus.PENDING, proposal.status)
        assertEquals("Budi", proposal.mahasiswaName)
        assertEquals("UI", proposal.mahasiswaUniversity)
        assertEquals(4.5, proposal.mahasiswaRating, 0.0)
        assertEquals("Landing Page", proposal.projectTitle)
        assertEquals(500000.0, proposal.projectBudget, 0.0)
        assertEquals(ProjectStatus.OPEN, proposal.projectStatus)
    }

    @Test
    fun fallsBackToDisplayNameWhenFullNameBlank() {
        val dto = ProjectProposalDto(
            id = "proposal-2",
            projectId = "project-1",
            mahasiswaId = "mhs-2",
            message = "Halo",
            proposedPrice = "100000",
            status = "REJECTED",
            mahasiswa = ProjectMahasiswaDto(
                id = "mhs-2",
                fullName = null,
                university = "ITB",
                rating = 0.0,
                user = ProjectUserDto(id = "user-2", name = "Ani", displayName = "Ani")
            )
        )

        val proposal = dto.toDomain()

        assertEquals("Ani", proposal.mahasiswaName)
        assertEquals(ProposalStatus.REJECTED, proposal.status)
    }

    @Test
    fun submitProposalRequestFormatsWholeNumberPriceWithTwoDecimals() {
        val request = submitProposalRequest(message = "Halo", proposedPrice = 500000.0)

        assertEquals("Halo", request.message)
        assertEquals("500000.00", request.proposedPrice)
    }
}
