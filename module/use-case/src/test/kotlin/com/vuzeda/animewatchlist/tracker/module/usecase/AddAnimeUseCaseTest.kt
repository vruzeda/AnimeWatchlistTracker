package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant

class AddAnimeUseCaseTest {

    private val animeRepository: AnimeRepository = mockk()
    private val clock: Clock = mockk {
        coEvery { now() } returns Instant.fromEpochMilliseconds(1770294088886)
    }
    private val useCase = AddAnimeUseCase(animeRepository, clock)

    private val anime = Anime(
        title = "Anime S1",
        titleEnglish = "Anime Season 1",
        titleJapanese = null,
        imageUrl = "s1.jpg",
        synopsis = "First season synopsis",
        genres = listOf("Action"),
    )

    private val seasons = listOf(
        Season(malId = 100, title = "Anime S1", type = "TV", episodeCount = 12, score = 8.5),
        Season(malId = 200, title = "Anime S2", type = "TV", episodeCount = 24, score = 9.0)
    )

    @BeforeEach
    fun setup() {
        coEvery { animeRepository.addAnime(any(), any()) } returns 1L
    }

    @Test
    fun `add anime with current date and status propagated to all seasons`() = runTest {
        useCase(anime, seasons, WatchStatus.WATCHING)

        val animeSlot = slot<Anime>()
        val seasonsSlot = slot<List<Season>>()
        coVerify { animeRepository.addAnime(capture(animeSlot), capture(seasonsSlot)) }

        assertThat(animeSlot.captured).isEqualTo(
            Anime(
                id = 0,
                title = "Anime S1",
                titleEnglish = "Anime Season 1",
                titleJapanese = null,
                imageUrl = "s1.jpg",
                synopsis = "First season synopsis",
                genres = listOf("Action"),
                userRating = null,
                notificationType = NotificationType.NONE,
                addedAt = 1770294088886,
            )
        )
        assertThat(seasonsSlot.captured).hasSize(2)
        assertThat(seasonsSlot.captured.all { it.status == WatchStatus.WATCHING }).isTrue()
    }
}
