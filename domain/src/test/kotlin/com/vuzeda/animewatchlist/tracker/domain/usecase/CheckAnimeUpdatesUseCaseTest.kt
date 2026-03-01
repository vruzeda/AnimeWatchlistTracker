package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeBasicInfo
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.domain.model.KnownSequel
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
        lastCheckedAiredEpisodeCount = 12,
        knownSequels = listOf(KnownSequel(200, true))
    )

    @Test
    fun `returns empty list when no notified anime`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns emptyList()

        val updates = useCase()

        assertThat(updates).isEmpty()
    }

    @Test
    fun `detects new episodes when aired episode count increased`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns listOf(sampleAnime)
        coEvery { repository.fetchLastAiredEpisodeNumber(100) } returns Result.success(15)
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.success(
            AnimeFullDetails(malId = 100, episodes = 25, sequels = emptyList())
        )

        val updates = useCase()

        assertThat(updates).hasSize(1)
        val update = updates.first() as AnimeUpdate.NewEpisodes
        assertThat(update.latestAiredEpisode).isEqualTo(15)
        assertThat(update.anime).isEqualTo(sampleAnime)
    }

    @Test
    fun `does not detect new episodes when aired count unchanged`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns listOf(sampleAnime)
        coEvery { repository.fetchLastAiredEpisodeNumber(100) } returns Result.success(12)
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.success(
            AnimeFullDetails(malId = 100, episodes = 25, sequels = listOf(SequelInfo(200, "Known")))
        )

        val updates = useCase()

        assertThat(updates).isEmpty()
    }

    @Test
    fun `does not detect new episodes on first check`() = runTest {
        val firstCheckAnime = sampleAnime.copy(lastCheckedAiredEpisodeCount = null)
        coEvery { repository.getNotifiedAnime() } returns listOf(firstCheckAnime)
        coEvery { repository.fetchLastAiredEpisodeNumber(100) } returns Result.success(12)
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.success(
            AnimeFullDetails(malId = 100, episodes = 25, sequels = emptyList())
        )

        val updates = useCase()

        assertThat(updates).isEmpty()
    }

    @Test
    fun `detects new season when sequel is currently airing`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns listOf(sampleAnime)
        coEvery { repository.fetchLastAiredEpisodeNumber(100) } returns Result.success(12)
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100,
                episodes = 25,
                sequels = listOf(SequelInfo(200, "Known"), SequelInfo(300, "New Season"))
            )
        )
        coEvery { repository.fetchAnimeBasicInfo(300) } returns Result.success(
            AnimeBasicInfo(malId = 300, status = "Currently Airing", airedFrom = "2026-01-01T00:00:00+00:00")
        )

        val updates = useCase()

        assertThat(updates).hasSize(1)
        val update = updates.first() as AnimeUpdate.NewSeason
        assertThat(update.sequelMalId).isEqualTo(300)
        assertThat(update.sequelTitle).isEqualTo("New Season")
    }

    @Test
    fun `does not notify for sequel that is not yet aired`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns listOf(sampleAnime)
        coEvery { repository.fetchLastAiredEpisodeNumber(100) } returns Result.success(12)
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100,
                episodes = 25,
                sequels = listOf(SequelInfo(200, "Known"), SequelInfo(300, "Unconfirmed"))
            )
        )
        coEvery { repository.fetchAnimeBasicInfo(300) } returns Result.success(
            AnimeBasicInfo(malId = 300, status = "Not yet aired", airedFrom = null)
        )

        val updates = useCase()

        assertThat(updates).isEmpty()
    }

    @Test
    fun `notifies for sequel with confirmed air date`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns listOf(sampleAnime)
        coEvery { repository.fetchLastAiredEpisodeNumber(100) } returns Result.success(12)
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100,
                episodes = 25,
                sequels = listOf(SequelInfo(200, "Known"), SequelInfo(300, "Confirmed"))
            )
        )
        coEvery { repository.fetchAnimeBasicInfo(300) } returns Result.success(
            AnimeBasicInfo(malId = 300, status = "Not yet aired", airedFrom = "2026-04-01T00:00:00+00:00")
        )

        val updates = useCase()

        assertThat(updates).hasSize(1)
        val update = updates.first() as AnimeUpdate.NewSeason
        assertThat(update.sequelMalId).isEqualTo(300)
    }

    @Test
    fun `re-checks un-notified sequel and notifies when status changes`() = runTest {
        val animeWithUnnotified = sampleAnime.copy(
            knownSequels = listOf(KnownSequel(200, true), KnownSequel(300, false))
        )
        coEvery { repository.getNotifiedAnime() } returns listOf(animeWithUnnotified)
        coEvery { repository.fetchLastAiredEpisodeNumber(100) } returns Result.success(12)
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100,
                episodes = 25,
                sequels = listOf(SequelInfo(200, "Known"), SequelInfo(300, "Now Airing"))
            )
        )
        coEvery { repository.fetchAnimeBasicInfo(300) } returns Result.success(
            AnimeBasicInfo(malId = 300, status = "Currently Airing", airedFrom = "2026-01-15T00:00:00+00:00")
        )

        val updates = useCase()

        assertThat(updates).hasSize(1)
        assertThat((updates.first() as AnimeUpdate.NewSeason).sequelMalId).isEqualTo(300)
    }

    @Test
    fun `skips already-notified sequel without re-fetching`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns listOf(sampleAnime)
        coEvery { repository.fetchLastAiredEpisodeNumber(100) } returns Result.success(12)
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.success(
            AnimeFullDetails(malId = 100, episodes = 25, sequels = listOf(SequelInfo(200, "Known")))
        )

        useCase()

        coVerify(exactly = 0) { repository.fetchAnimeBasicInfo(200) }
    }

    @Test
    fun `updates notification data after checking`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns listOf(sampleAnime)
        coEvery { repository.fetchLastAiredEpisodeNumber(100) } returns Result.success(15)
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.success(
            AnimeFullDetails(
                malId = 100,
                episodes = 25,
                sequels = listOf(SequelInfo(200, "Known"), SequelInfo(300, "New"))
            )
        )
        coEvery { repository.fetchAnimeBasicInfo(300) } returns Result.success(
            AnimeBasicInfo(malId = 300, status = "Currently Airing", airedFrom = "2026-01-01T00:00:00+00:00")
        )

        useCase()

        coVerify {
            repository.updateNotificationData(
                id = 1L,
                lastCheckedAiredEpisodeCount = 15,
                knownSequels = listOf(KnownSequel(200, true), KnownSequel(300, true))
            )
        }
    }

    @Test
    fun `skips anime without malId`() = runTest {
        val localOnlyAnime = sampleAnime.copy(malId = null)
        coEvery { repository.getNotifiedAnime() } returns listOf(localOnlyAnime)

        val updates = useCase()

        assertThat(updates).isEmpty()
        coVerify(exactly = 0) { repository.fetchLastAiredEpisodeNumber(any()) }
    }

    @Test
    fun `preserves aired count when episodes API fails`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns listOf(sampleAnime)
        coEvery { repository.fetchLastAiredEpisodeNumber(100) } returns Result.failure(RuntimeException("Error"))
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.success(
            AnimeFullDetails(malId = 100, episodes = 25, sequels = emptyList())
        )

        useCase()

        coVerify {
            repository.updateNotificationData(
                id = 1L,
                lastCheckedAiredEpisodeCount = 12,
                knownSequels = emptyList()
            )
        }
    }

    @Test
    fun `preserves known sequels when full details API fails`() = runTest {
        coEvery { repository.getNotifiedAnime() } returns listOf(sampleAnime)
        coEvery { repository.fetchLastAiredEpisodeNumber(100) } returns Result.success(12)
        coEvery { repository.fetchAnimeFullDetails(100) } returns Result.failure(RuntimeException("Error"))

        useCase()

        coVerify {
            repository.updateNotificationData(
                id = 1L,
                lastCheckedAiredEpisodeCount = 12,
                knownSequels = listOf(KnownSequel(200, true))
            )
        }
    }
}
