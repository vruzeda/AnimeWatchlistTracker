package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateAnimeUseCaseTest {

    private val repository = mockk<AnimeRepository>()
    private val useCase = UpdateAnimeUseCase(repository)

    @Test
    fun `delegates anime update to repository`() = runTest {
        val anime = Anime(
            id = 1,
            title = "Naruto",
            status = WatchStatus.WATCHING
        )
        coEvery { repository.updateAnime(anime) } returns Unit

        useCase(anime)

        coVerify { repository.updateAnime(anime) }
    }
}
