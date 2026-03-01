package com.vuzeda.animewatchlist.tracker.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.SeasonData
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRemoteRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ResolveAnimeProgressivelyUseCaseTest {

    private val remoteRepository = mockk<AnimeRemoteRepository>()
    private val useCase = ResolveAnimeProgressivelyUseCase(remoteRepository)

    private val sampleSeasons = listOf(
        SeasonData(malId = 1, title = "Season 1", type = "TV", episodeCount = 12, score = 8.0),
        SeasonData(malId = 2, title = "Season 2", type = "TV", episodeCount = 12, score = 8.5)
    )

    @Test
    fun `emits all seasons immediately from watch order`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(1) } returns Result.success(sampleSeasons)
        coEvery { remoteRepository.fetchAnimeFullById(1) } returns Result.success(
            AnimeFullDetails(
                malId = 1,
                title = "Season 1 Full",
                type = "TV",
                episodes = 12,
                score = 8.0,
                synopsis = "A great anime.",
                genres = listOf("Action"),
                sequels = emptyList(),
                prequels = emptyList()
            )
        )

        useCase(1).test {
            val first = awaitItem()
            assertThat(first.title).isEqualTo("Season 1")
            assertThat(first.seasons).hasSize(2)
            assertThat(first.synopsis).isNull()
            assertThat(first.isResolvingPrequels).isFalse()
            assertThat(first.isResolvingSequels).isFalse()

            val enriched = awaitItem()
            assertThat(enriched.title).isEqualTo("Season 1 Full")
            assertThat(enriched.synopsis).isEqualTo("A great anime.")
            assertThat(enriched.genres).containsExactly("Action")
            assertThat(enriched.seasons).hasSize(2)

            awaitComplete()
        }
    }

    @Test
    fun `emits single season when only one exists`() = runTest {
        val singleSeason = listOf(
            SeasonData(malId = 5, title = "Solo Anime", type = "TV", episodeCount = 24, score = 9.0)
        )
        coEvery { remoteRepository.fetchWatchOrder(5) } returns Result.success(singleSeason)
        coEvery { remoteRepository.fetchAnimeFullById(5) } returns Result.success(
            AnimeFullDetails(
                malId = 5,
                title = "Solo Anime",
                type = "TV",
                episodes = 24,
                score = 9.0,
                synopsis = "A standalone story.",
                genres = listOf("Drama"),
                sequels = emptyList(),
                prequels = emptyList()
            )
        )

        useCase(5).test {
            val first = awaitItem()
            assertThat(first.seasons).hasSize(1)
            assertThat(first.seasons[0].malId).isEqualTo(5)

            val enriched = awaitItem()
            assertThat(enriched.synopsis).isEqualTo("A standalone story.")

            awaitComplete()
        }
    }

    @Test
    fun `completes without enrichment when Jikan call fails`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(1) } returns Result.success(sampleSeasons)
        coEvery { remoteRepository.fetchAnimeFullById(1) } returns Result.failure(Exception("API error"))

        useCase(1).test {
            val result = awaitItem()
            assertThat(result.title).isEqualTo("Season 1")
            assertThat(result.seasons).hasSize(2)
            assertThat(result.synopsis).isNull()

            awaitComplete()
        }
    }

    @Test
    fun `throws when watch order fetch fails`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(999) } returns Result.failure(Exception("Not found"))

        useCase(999).test {
            awaitError()
        }
    }

    @Test
    fun `uses root season title and image in initial emission`() = runTest {
        val seasons = listOf(
            SeasonData(malId = 10, title = "Root Season", imageUrl = "root.jpg", type = "TV"),
            SeasonData(malId = 11, title = "Season 2", imageUrl = "s2.jpg", type = "TV")
        )
        coEvery { remoteRepository.fetchWatchOrder(10) } returns Result.success(seasons)
        coEvery { remoteRepository.fetchAnimeFullById(10) } returns Result.success(
            AnimeFullDetails(
                malId = 10,
                title = "Root Full Title",
                imageUrl = "root_full.jpg",
                synopsis = "Root synopsis.",
                genres = listOf("Fantasy"),
                type = "TV",
                episodes = 24,
                sequels = emptyList(),
                prequels = emptyList()
            )
        )

        useCase(10).test {
            val initial = awaitItem()
            assertThat(initial.title).isEqualTo("Root Season")
            assertThat(initial.imageUrl).isEqualTo("root.jpg")

            val enriched = awaitItem()
            assertThat(enriched.title).isEqualTo("Root Full Title")
            assertThat(enriched.imageUrl).isEqualTo("root_full.jpg")
            assertThat(enriched.synopsis).isEqualTo("Root synopsis.")
            assertThat(enriched.genres).containsExactly("Fantasy")

            awaitComplete()
        }
    }

    @Test
    fun `emits only Jikan result when Chiaki returns empty seasons`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(42) } returns Result.success(emptyList())
        coEvery { remoteRepository.fetchAnimeFullById(42) } returns Result.success(
            AnimeFullDetails(
                malId = 42,
                title = "Standalone Anime",
                type = "TV",
                episodes = 12,
                score = 7.5,
                synopsis = "An anime with no Chiaki data.",
                genres = listOf("Slice of Life"),
                imageUrl = "https://example.com/standalone.jpg",
                sequels = emptyList(),
                prequels = emptyList()
            )
        )

        useCase(42).test {
            val result = awaitItem()
            assertThat(result.title).isEqualTo("Standalone Anime")
            assertThat(result.synopsis).isEqualTo("An anime with no Chiaki data.")
            assertThat(result.genres).containsExactly("Slice of Life")
            assertThat(result.imageUrl).isEqualTo("https://example.com/standalone.jpg")
            assertThat(result.seasons).isEmpty()

            awaitComplete()
        }
    }

    @Test
    fun `completes with no emissions when Chiaki returns empty and Jikan fails`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(42) } returns Result.success(emptyList())
        coEvery { remoteRepository.fetchAnimeFullById(42) } returns Result.failure(Exception("API error"))

        useCase(42).test {
            awaitComplete()
        }
    }

    @Test
    fun `fetches full details using original malId not root season malId`() = runTest {
        val seasons = listOf(
            SeasonData(malId = 100, title = "Prequel", type = "TV"),
            SeasonData(malId = 200, title = "Main", type = "TV")
        )
        coEvery { remoteRepository.fetchWatchOrder(200) } returns Result.success(seasons)
        coEvery { remoteRepository.fetchAnimeFullById(200) } returns Result.success(
            AnimeFullDetails(
                malId = 200,
                title = "Main Full",
                type = "TV",
                episodes = 24,
                score = 8.0,
                synopsis = "The main anime.",
                genres = listOf("Action"),
                sequels = emptyList(),
                prequels = emptyList()
            )
        )

        useCase(200).test {
            val initial = awaitItem()
            assertThat(initial.title).isEqualTo("Prequel")

            val enriched = awaitItem()
            assertThat(enriched.title).isEqualTo("Main Full")
            assertThat(enriched.synopsis).isEqualTo("The main anime.")

            awaitComplete()
        }

        coEvery { remoteRepository.fetchAnimeFullById(100) } returns Result.failure(Exception("Should not be called"))
    }

    @Test
    fun `image is null when both Jikan and Chiaki images are null`() = runTest {
        coEvery { remoteRepository.fetchWatchOrder(50) } returns Result.success(emptyList())
        coEvery { remoteRepository.fetchAnimeFullById(50) } returns Result.success(
            AnimeFullDetails(
                malId = 50,
                title = "No Image Anime",
                type = "TV",
                episodes = null,
                imageUrl = null,
                sequels = emptyList(),
                prequels = emptyList()
            )
        )

        useCase(50).test {
            val result = awaitItem()
            assertThat(result.imageUrl).isNull()

            awaitComplete()
        }
    }

    @Test
    fun `falls back to chiaki image when Jikan image is null`() = runTest {
        val seasons = listOf(
            SeasonData(malId = 20, title = "Test Anime", imageUrl = "chiaki.jpg", type = "TV")
        )
        coEvery { remoteRepository.fetchWatchOrder(20) } returns Result.success(seasons)
        coEvery { remoteRepository.fetchAnimeFullById(20) } returns Result.success(
            AnimeFullDetails(
                malId = 20,
                title = "Test Anime Full",
                imageUrl = null,
                type = "TV",
                episodes = null,
                sequels = emptyList(),
                prequels = emptyList()
            )
        )

        useCase(20).test {
            awaitItem()

            val enriched = awaitItem()
            assertThat(enriched.imageUrl).isEqualTo("chiaki.jpg")

            awaitComplete()
        }
    }
}
