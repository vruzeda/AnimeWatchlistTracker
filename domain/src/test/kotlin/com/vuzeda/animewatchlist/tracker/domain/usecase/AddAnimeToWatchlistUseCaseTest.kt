package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AddAnimeToWatchlistUseCaseTest {

    private val repository = mockk<AnimeRepository>()
    private val useCase = AddAnimeToWatchlistUseCase(repository)

    @Test
    fun `delegates to repository and returns inserted id`() = runTest {
        val anime = Anime(title = "Attack on Titan")
        coEvery { repository.addAnime(anime) } returns 42L

        val result = useCase(anime)

        assertThat(result).isEqualTo(42L)
        coVerify { repository.addAnime(anime) }
    }
}
