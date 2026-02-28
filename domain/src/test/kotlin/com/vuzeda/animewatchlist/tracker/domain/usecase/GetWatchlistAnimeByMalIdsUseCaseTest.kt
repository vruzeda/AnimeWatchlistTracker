package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetWatchlistAnimeByMalIdsUseCaseTest {

    private val repository = mockk<AnimeRepository>()
    private val useCase = GetWatchlistAnimeByMalIdsUseCase(repository)

    @Test
    fun `returns anime matching given mal ids`() = runTest {
        val anime = listOf(
            Anime(id = 1L, malId = 21, title = "One Punch Man", status = WatchStatus.WATCHING),
            Anime(id = 2L, malId = 30, title = "Naruto", status = WatchStatus.COMPLETED)
        )
        coEvery { repository.getAnimeByMalIds(listOf(21, 30, 99)) } returns anime

        val result = useCase(listOf(21, 30, 99))

        assertThat(result).hasSize(2)
        assertThat(result[0].malId).isEqualTo(21)
        assertThat(result[1].malId).isEqualTo(30)
        coVerify { repository.getAnimeByMalIds(listOf(21, 30, 99)) }
    }

    @Test
    fun `returns empty list when no matches found`() = runTest {
        coEvery { repository.getAnimeByMalIds(listOf(999)) } returns emptyList()

        val result = useCase(listOf(999))

        assertThat(result).isEmpty()
    }
}
