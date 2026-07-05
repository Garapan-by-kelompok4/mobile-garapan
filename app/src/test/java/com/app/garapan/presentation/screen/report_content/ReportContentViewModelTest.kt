package com.app.garapan.presentation.screen.report_content

import androidx.lifecycle.SavedStateHandle
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.ContentReportContentType
import com.app.garapan.domain.repository.ContentReportRepository
import com.app.garapan.domain.usecase.SubmitContentReportUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportContentViewModelTest {

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
    fun `canSubmit is false for short reason`() = runTest {
        val viewModel = createViewModel()

        viewModel.onReasonChanged("pendek")

        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `canSubmit is false while loading`() = runTest {
        val viewModel = createViewModel(
            submitResult = Resource.Loading
        )

        viewModel.onReasonChanged("Alasan laporan yang cukup panjang.")
        viewModel.onSubmitReport()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `submit success clears loading`() = runTest {
        val viewModel = createViewModel(
            submitResult = Resource.Success(Unit)
        )

        viewModel.onReasonChanged("Konten ini terlihat menyesatkan dan tidak sesuai aturan.")
        viewModel.onSubmitReport()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `submit error shows localized message`() = runTest {
        val viewModel = createViewModel(
            submitResult = Resource.Error("You already have a pending report for this content")
        )

        viewModel.onReasonChanged("Konten ini terlihat menyesatkan dan tidak sesuai aturan.")
        viewModel.onSubmitReport()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(
            "Anda sudah memiliki laporan yang sedang ditinjau untuk konten ini.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun `screen title reflects jasa content type`() {
        val viewModel = createViewModel(contentType = "JASA")

        assertEquals("Laporkan Jasa", viewModel.uiState.value.screenTitle)
        assertTrue(viewModel.uiState.value.introText.contains("jasa"))
    }

    private fun createViewModel(
        contentType: String = "JASA",
        contentId: String = "content-1",
        submitResult: Resource<Unit> = Resource.Success(Unit)
    ): ReportContentViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "contentType" to contentType,
                "contentId" to contentId
            )
        )
        val repository = object : ContentReportRepository {
            override suspend fun submitReport(
                type: ContentReportContentType,
                id: String,
                reason: String
            ): Resource<Unit> {
                assertEquals(ContentReportContentType.JASA, type)
                assertEquals(contentId, id)
                assertTrue(reason.length >= ReportValidation.MIN_REASON_LENGTH)
                return submitResult
            }
        }
        return ReportContentViewModel(
            savedStateHandle = savedStateHandle,
            submitContentReportUseCase = SubmitContentReportUseCase(repository)
        )
    }
}
