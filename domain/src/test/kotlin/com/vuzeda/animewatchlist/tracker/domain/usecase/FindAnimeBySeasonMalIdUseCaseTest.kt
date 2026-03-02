package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class FindAnimeBySeasonMalIdUseCaseTest {

    private val repository = mockk<SeasonRepository>()
    private val useCase = FindAnimeBySeasonMalIdUseCase(repository)

    @Test
    fun `returns anime id when season exists in watchlist`() = runTest {
        coEvery { repository.findAnimeIdBySeasonMalId(100) } returns 5L

        val result = useCase(100)

        assertThat(result).isEqualTo(5L)
        coVerify { repository.findAnimeIdBySeasonMalId(100) }
    }

    @Test
    fun `returns null when season is not in watchlist`() = runTest {
        coEvery { repository.findAnimeIdBySeasonMalId(999) } returns null

        val result = useCase(999)

        assertThat(result).isNull()
    }
}
