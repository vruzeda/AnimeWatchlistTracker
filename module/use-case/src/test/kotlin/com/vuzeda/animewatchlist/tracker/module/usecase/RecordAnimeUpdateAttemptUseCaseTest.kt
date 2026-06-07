package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdateResult
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RecordAnimeUpdateAttemptUseCaseTest {

    private val repository = mockk<AnimeRepository>(relaxUnitFun = true)
    private val useCase = RecordAnimeUpdateAttemptUseCase(repository)

    @Test
    fun `delegates Success to repository`() = runTest {
        useCase(AnimeUpdateResult.Success)

        coVerify(exactly = 1) { repository.recordAnimeUpdateAttempt(AnimeUpdateResult.Success) }
    }

    @Test
    fun `delegates Failure to repository`() = runTest {
        val result = AnimeUpdateResult.Failure("timeout")

        useCase(result)

        coVerify(exactly = 1) { repository.recordAnimeUpdateAttempt(result) }
    }

    @Test
    fun `delegates WillRetry to repository`() = runTest {
        val result = AnimeUpdateResult.WillRetry(reason = "Network error", retryCount = 2)
        useCase(result)

        coVerify(exactly = 1) { repository.recordAnimeUpdateAttempt(result) }
    }
}
