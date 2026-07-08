package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.TakenProjectDto
import com.app.garapan.data.remote.dto.TakenProjectPesananDto
import com.app.garapan.domain.model.PesananStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class TakenProjectMappersTest {

    @Test
    fun `maps taken project dto with pesanan status`() {
        val dto = TakenProjectDto(
            projectId = "project-1",
            title = "landing page",
            budget = "4500000.00",
            deadline = "2026-07-22T00:00:00.000Z",
            imageUrl = "https://example.com/project.png",
            kategoriName = "Web Development",
            clientName = "Garapan Demo Client",
            proposalId = "proposal-1",
            proposedPrice = "4500000.00",
            acceptedAt = "2026-07-08T00:57:00.000Z",
            pesanan = TakenProjectPesananDto(
                id = "order-1",
                status = "IN_PROGRESS",
                updatedAt = "2026-07-08T00:57:00.000Z"
            )
        )

        val takenProject = dto.toDomain()

        assertEquals("project-1", takenProject.project.id)
        assertEquals("landing page", takenProject.project.title)
        assertEquals(4_500_000.0, takenProject.project.budget, 0.0)
        assertEquals("Web Development", takenProject.project.kategoriName)
        assertEquals("Garapan Demo Client", takenProject.project.clientName)
        assertEquals(PesananStatus.IN_PROGRESS, takenProject.orderStatus)
        assertEquals("order-1", takenProject.orderId)
    }

    @Test
    fun `maps taken project dto without pesanan`() {
        val dto = TakenProjectDto(
            projectId = "project-2",
            title = "fresh website",
            budget = "150000.00",
            deadline = "2026-07-28T00:00:00.000Z",
            proposalId = "proposal-2",
            proposedPrice = "150000.00",
            pesanan = null
        )

        val takenProject = dto.toDomain()

        assertEquals("fresh website", takenProject.project.title)
        assertEquals(null, takenProject.orderStatus)
        assertEquals(null, takenProject.orderId)
    }
}
