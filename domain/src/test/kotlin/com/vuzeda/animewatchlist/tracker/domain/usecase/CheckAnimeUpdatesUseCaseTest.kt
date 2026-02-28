package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.domain.model.SequelInfo
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CheckAnimeUpdatesUseCaseTest {

    private val repository = mockk<AnimeRepository>(relaxed = true)
    private val useCase = CheckAnimeUpdatesUseCase(repository)

    private val sampleAnime = Anime(
        id = 1,
        malId = 100,
        title = "Test Anime",
        status = WatchStatus.WATCHING,
        isNotificationsEnabled = true,
        lastCheckedEpisodeCount = 12,
        knownSequelMalIds = listOf(200)
    )

    @Test
    fun `returns empty list when no notified anime`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns emptyList()

        val updates = useCase()

        assertThat(updates).isEmpty()
    }

    @Test
    fun `detects new episodes when episode count increased`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns listOf(sampleAnime)
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.success(
            AnimeFullDetails(malId = 100, episodes = 24, sequels = emptyList())
        )

        val updates = useCase()

        assertThat(updates).hasSize(1)
        val update = updates.first() as AnimeUpdate.NewEpisodes
        assertThat(update.previousCount).isEqualTo(12)
        assertThat(update.currentCount).isEqualTo(24)
        assertThat(update.anime).isEqualTo(sampleAnime)
    }

    @Test
    fun `does not detect new episodes when count unchanged`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns listOf(sampleAnime)
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.success(
            AnimeFullDetails(malId = 100, episodes = 12, sequels = listOf(SequelInfo(200, "Known Sequel")))
        )

        val updates = useCase()

        assertThat(updates).isEmpty()
    }

    @Test
    fun `detects new season when unknown sequel found`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns listOf(sampleAnime)
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100,
                episodes = 12,
                sequels = listOf(
                    SequelInfo(200, "Known Sequel"),
                    SequelInfo(300, "New Season")
                )
            )
        )

        val updates = useCase()

        assertThat(updates).hasSize(1)
        val update = updates.first() as AnimeUpdate.NewSeason
        assertThat(update.sequelMalId).isEqualTo(300)
        assertThat(update.sequelTitle).isEqualTo("New Season")
    }

    @Test
    fun `detects both new episodes and new season`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns listOf(sampleAnime)
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100,
                episodes = 24,
                sequels = listOf(SequelInfo(200, "Known"), SequelInfo(300, "New"))
            )
        )

        val updates = useCase()

        assertThat(updates).hasSize(2)
        assertThat(updates.filterIsInstance<AnimeUpdate.NewEpisodes>()).hasSize(1)
        assertThat(updates.filterIsInstance<AnimeUpdate.NewSeason>()).hasSize(1)
    }

    @Test
    fun `updates notification data after checking each anime`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns listOf(sampleAnime)
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100,
                episodes = 24,
                sequels = listOf(SequelInfo(200, "Known"), SequelInfo(300, "New"))
            )
        )

        useCase()

        coVerify {
            repository.updateNotificationData(
                id = 1L,
                lastCheckedEpisodeCount = 24,
                knownSequelMalIds = listOf(200, 300)
            )
        }
    }

    @Test
    fun `skips anime without malId`() = runTest {
        val localOnlyAnime = sampleAnime.copy(malId = null)
        coEvery { repository.getNotifiedAnime() } returns listOf(localOnlyAnime)

        val updates = useCase()

        assertThat(updates).isEmpty()
        coVerify(exactly = 0) { repository.fetchAnimeFullDetails(any()) }
    }

    @Test
    fun `skips anime when API call fails`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns listOf(sampleAnime)
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.failure(RuntimeException("Network error"))

        val updates = useCase()

        assertThat(updates).isEmpty()
        coVerify(exactly = 0) { repository.updateNotificationData(any(), any(), any()) }
    }

    @Test
    fun `preserves last checked episode count when API returns null episodes`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns listOf(sampleAnime)
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.success(
            AnimeFullDetails(malId = 100, episodes = null, sequels = emptyList())
        )

        useCase()

        coVerify {
            repository.updateNotificationData(
                id = 1L,
                lastCheckedEpisodeCount = 12,
                knownSequelMalIds = emptyList()
            )
        }
    }
}
