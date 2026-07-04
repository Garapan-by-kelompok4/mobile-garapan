package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.ProjectDto
import com.app.garapan.domain.model.CreateProjectParams
import com.app.garapan.domain.model.ProjectStatus
import com.app.garapan.domain.model.UpdateProjectParams
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProjectMappersTest {

    @Test
    fun `maps budget range fields from backend response`() {
        val project = projectDto(
            budget = "2000000",
            minBudget = "1000000",
            maxBudget = "2000000"
        ).toDomain()

        assertEquals(2000000.0, project.budget, 0.0)
        assertEquals(1000000.0, project.minBudget ?: 0.0, 0.0)
        assertEquals(2000000.0, project.maxBudget ?: 0.0, 0.0)
    }

    @Test
    fun `maps legacy project response with null budget range`() {
        val project = projectDto(
            budget = "750000",
            minBudget = null,
            maxBudget = null
        ).toDomain()

        assertEquals(750000.0, project.budget, 0.0)
        assertNull(project.minBudget)
        assertNull(project.maxBudget)
    }

    @Test
    fun `create request sends max budget as compatibility budget and includes range`() {
        val request = CreateProjectParams(
            title = "check harga",
            description = "test",
            budget = 2000000.0,
            minBudget = 1000000.0,
            maxBudget = 2000000.0,
            deadline = "2026-07-11T00:00:00.000Z",
            kategoriId = "kategori-1"
        ).toRequest()

        assertEquals("2000000.00", request.budget)
        assertEquals("1000000.00", request.minBudget)
        assertEquals("2000000.00", request.maxBudget)
    }

    @Test
    fun `update request includes budget range when provided`() {
        val request = UpdateProjectParams(
            budget = 2000000.0,
            minBudget = 1000000.0,
            maxBudget = 2000000.0
        ).toRequest()

        assertEquals("2000000.00", request.budget)
        assertEquals("1000000.00", request.minBudget)
        assertEquals("2000000.00", request.maxBudget)
    }

    private fun projectDto(
        budget: String,
        minBudget: String?,
        maxBudget: String?
    ) = ProjectDto(
        id = "project-1",
        klienId = "klien-1",
        kategoriId = "kategori-1",
        title = "check harga",
        description = "test",
        budget = budget,
        minBudget = minBudget,
        maxBudget = maxBudget,
        deadline = "2026-07-11T00:00:00.000Z",
        status = ProjectStatus.OPEN.name
    )
}
