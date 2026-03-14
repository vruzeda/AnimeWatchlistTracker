package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.repository.SeasonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class FindSeasonIdByMalIdUseCaseTest {

    private val seasonRepository: SeasonRepository = mockk()
    private val useCase = FindSeasonIdByMalIdUseCase(seasonRepository)

    @Test
    fun `returns season id when found`() = runTest {
        coEvery { seasonRepository.findSeasonIdByMalId(101) } returns 42L

        val result = useCase(101)

        assertThat(result).isEqualTo(42L)
        coVerify { seasonRepository.findSeasonIdByMalId(101) }
    }

    @Test
    fun `returns null when season not found`() = runTest {
        coEvery { seasonRepository.findSeasonIdByMalId(999) } returns null

        val result = useCase(999)

        assertThat(result).isNull()
    }
}
