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

class GetAnimeByIdUseCaseTest {

    private val repository = mockk<AnimeRepository>()
    private val useCase = GetAnimeByIdUseCase(repository)

    @Test
    fun `returns anime when found`() = runTest {
        val anime = Anime(id = 1, title = "Naruto", status = WatchStatus.WATCHING)
        coEvery { repository.getAnimeById(1L) } returns anime

        val result = useCase(1L)

        assertThat(result).isEqualTo(anime)
        coVerify { repository.getAnimeById(1L) }
    }

    @Test
    fun `returns null when anime not found`() = runTest {
        coEvery { repository.getAnimeById(999L) } returns null

        val result = useCase(999L)

        assertThat(result).isNull()
    }
}
