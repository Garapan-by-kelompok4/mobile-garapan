package com.app.garapan.presentation.screen.checkout

import androidx.lifecycle.SavedStateHandle
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreateJasaParams
import com.app.garapan.domain.model.CreatePaymentTokenParams
import com.app.garapan.domain.model.CreatePesananParams
import com.app.garapan.domain.model.Jasa
import com.app.garapan.domain.model.JasaListFilters
import com.app.garapan.domain.model.JasaStatus
import com.app.garapan.domain.model.UpdateJasaParams
import com.app.garapan.domain.model.PaymentMethod
import com.app.garapan.domain.model.Pembayaran
import com.app.garapan.domain.model.PaymentStatus
import com.app.garapan.domain.model.Pesanan
import com.app.garapan.domain.model.PesananStatus
import com.app.garapan.domain.repository.JasaRepository
import com.app.garapan.domain.repository.PembayaranRepository
import com.app.garapan.domain.repository.PesananRepository
import com.app.garapan.domain.usecase.CreatePaymentTokenUseCase
import com.app.garapan.domain.usecase.CreatePesananUseCase
import com.app.garapan.domain.usecase.GetJasaDetailUseCase
import com.app.garapan.domain.usecase.WaitForPesananPaymentUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {

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
    fun `double tap on pay starts only one create and token flow`() = runTest {
        val pesananRepository = FakePesananRepository(createDelayMs = 200)
        val pembayaranRepository = FakePembayaranRepository(tokenDelayMs = 200)
        val viewModel = createViewModel(
            pesananRepository = pesananRepository,
            pembayaranRepository = pembayaranRepository
        )
        advanceUntilIdle()

        viewModel.onPayNowClicked()
        viewModel.onPayNowClicked()
        advanceTimeBy(500)
        advanceUntilIdle()

        assertEquals(1, pesananRepository.createCallCount)
        assertEquals(1, pembayaranRepository.createTokenCallCount)
        assertTrue(viewModel.uiState.value.isProcessingPayment)
    }

    @Test
    fun `resumed pending order shows continue payment status message`() = runTest {
        val oldCreatedAt = Instant.now().minusSeconds(120).toString()
        val pesananRepository = FakePesananRepository(
            pesanan = samplePesanan(createdAt = oldCreatedAt)
        )
        val pembayaranRepository = FakePembayaranRepository(tokenDelayMs = 10_000)
        val viewModel = createViewModel(
            pesananRepository = pesananRepository,
            pembayaranRepository = pembayaranRepository
        )
        advanceUntilIdle()

        viewModel.onPayNowClicked()
        advanceTimeBy(100)

        assertEquals(
            "Melanjutkan pembayaran pesanan yang ada...",
            viewModel.uiState.value.statusMessage
        )
    }

    @Test
    fun `launch snap payment event emitted once per pay flow`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val events = mutableListOf<CheckoutEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events += it }
        }

        viewModel.onPayNowClicked()
        advanceUntilIdle()

        assertEquals(1, events.filterIsInstance<CheckoutEvent.LaunchSnapPayment>().size)
    }

    private fun createViewModel(
        pesananRepository: FakePesananRepository = FakePesananRepository(),
        pembayaranRepository: FakePembayaranRepository = FakePembayaranRepository(),
        jasaRepository: FakeJasaRepository = FakeJasaRepository()
    ): CheckoutViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("jasaId" to "jasa-1"))
        return CheckoutViewModel(
            savedStateHandle = savedStateHandle,
            getJasaDetailUseCase = GetJasaDetailUseCase(jasaRepository),
            createPesananUseCase = CreatePesananUseCase(pesananRepository),
            createPaymentTokenUseCase = CreatePaymentTokenUseCase(pembayaranRepository),
            waitForPesananPaymentUseCase = WaitForPesananPaymentUseCase(pesananRepository)
        )
    }

    private fun samplePesanan(createdAt: String = Instant.now().toString()) = Pesanan(
        id = "pesanan-1",
        conversationId = null,
        klienId = "klien-1",
        mahasiswaId = "mahasiswa-1",
        jasaId = "jasa-1",
        projectId = null,
        totalPrice = 600_000.0,
        status = PesananStatus.PENDING,
        createdAt = createdAt,
        updatedAt = createdAt,
        jasaTitle = "UI/UX Design",
        projectTitle = "",
        workerName = "Worker",
        clientLabel = "Client",
        workerUserId = null,
        clientUserId = null,
        payment = null
    )

    private class FakeJasaRepository : JasaRepository {
        override suspend fun getJasaDetail(id: String): Resource<Jasa> =
            Resource.Success(
                Jasa(
                    id = id,
                    mahasiswaId = "mahasiswa-1",
                    kategoriId = "kat-1",
                    title = "UI/UX Design",
                    description = "desc",
                    price = 600_000.0,
                    imageUrl = "",
                    status = JasaStatus.ACTIVE,
                    workerName = "Worker"
                )
            )

        override suspend fun getJasaList(filters: JasaListFilters): Resource<List<Jasa>> =
            Resource.Success(emptyList())

        override suspend fun getMyJasaList(filters: JasaListFilters): Resource<List<Jasa>> =
            Resource.Success(emptyList())

        override suspend fun createJasa(params: CreateJasaParams): Resource<Jasa> =
            error("not used")

        override suspend fun updateJasa(id: String, params: UpdateJasaParams): Resource<Jasa> =
            error("not used")

        override suspend fun deleteJasa(id: String): Resource<Unit> = error("not used")
    }

    private class FakePesananRepository(
        private val pesanan: Pesanan = Pesanan(
            id = "pesanan-1",
            conversationId = null,
            klienId = "klien-1",
            mahasiswaId = "mahasiswa-1",
            jasaId = "jasa-1",
            projectId = null,
            totalPrice = 600_000.0,
            status = PesananStatus.PENDING,
            createdAt = Instant.now().toString(),
            updatedAt = Instant.now().toString(),
            jasaTitle = "UI/UX Design",
            projectTitle = "",
            workerName = "Worker",
            clientLabel = "Client",
            workerUserId = null,
            clientUserId = null,
            payment = null
        ),
        private val createDelayMs: Long = 0
    ) : PesananRepository {
        var createCallCount = 0

        override suspend fun createPesanan(params: CreatePesananParams): Resource<Pesanan> {
            createCallCount++
            if (createDelayMs > 0) delay(createDelayMs)
            return Resource.Success(pesanan)
        }

        override suspend fun getMyPesananList(page: Int, limit: Int): Resource<List<Pesanan>> =
            Resource.Success(emptyList())

        override suspend fun getPesananDetail(id: String): Resource<Pesanan> =
            Resource.Success(pesanan.copy(id = id))

        override suspend fun deliverPesanan(id: String): Resource<Pesanan> = error("not used")

        override suspend fun completePesanan(id: String): Resource<Pesanan> = error("not used")

        override suspend fun cancelPesanan(id: String): Resource<Pesanan> = error("not used")
    }

    private class FakePembayaranRepository(
        private val tokenDelayMs: Long = 0
    ) : PembayaranRepository {
        var createTokenCallCount = 0

        override suspend fun createPaymentToken(params: CreatePaymentTokenParams): Resource<Pembayaran> {
            createTokenCallCount++
            if (tokenDelayMs > 0) delay(tokenDelayMs)
            return Resource.Success(
                Pembayaran(
                    id = "pay-1",
                    pesananId = params.pesananId,
                    amount = 600_000.0,
                    method = PaymentMethod.GOPAY,
                    midtransToken = "snap-token",
                    status = PaymentStatus.PENDING,
                    paidAt = null
                )
            )
        }
    }
}
