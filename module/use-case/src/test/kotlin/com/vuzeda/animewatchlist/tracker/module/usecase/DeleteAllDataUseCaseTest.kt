package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeleteAllDataUseCaseTest {

    private val repository = mockk<AnimeRepository>(relaxUnitFun = true)
    private val useCase = DeleteAllDataUseCase(repository)

    @Test
    fun `delegates to repository deleteAllData`() = runTest {
        useCase()

        coVerify(exactly = 1) { repository.deleteAllData() }
    }
}
