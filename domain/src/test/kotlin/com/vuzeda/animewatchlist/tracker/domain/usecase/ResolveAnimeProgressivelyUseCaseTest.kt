package com.vuzeda.animewatchlist.tracker.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.SequelInfo
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRemoteRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ResolveAnimeProgressivelyUseCaseTest {

    private val remoteRepository = mockk<AnimeRemoteRepository>()
    private val useCase = ResolveAnimeProgressivelyUseCase(remoteRepository)

    @Test
    fun `emits single entry immediately when no prequels or sequels`() = runTest {
        val entry = AnimeFullDetails(
            malId = 1,
            title = "Solo Anime",
            type = "TV",
            episodes = 12,
            score = 8.0,
            sequels = emptyList(),
            prequels = emptyList()
        )
        coEvery { remoteRepository.fetchAnimeFullById(1) } returns Result.success(entry)

        useCase(1).test {
            val result = awaitItem()
            assertThat(result.title).isEqualTo("Solo Anime")
            assertThat(result.seasons).hasSize(1)
            assertThat(result.seasons[0].malId).isEqualTo(1)
            assertThat(result.isResolvingPrequels).isFalse()
            assertThat(result.isResolvingSequels).isFalse()

            awaitComplete()
        }
    }

    @Test
    fun `emits progressively when walking sequels`() = runTest {
        val season1 = AnimeFullDetails(
            malId = 1,
            title = "Season 1",
            type = "TV",
            episodes = 12,
            sequels = listOf(SequelInfo(2, "Season 2")),
            prequels = emptyList()
        )
        val season2 = AnimeFullDetails(
            malId = 2,
            title = "Season 2",
            type = "TV",
            episodes = 12,
            sequels = emptyList(),
            prequels = listOf(SequelInfo(1, "Season 1"))
        )
        coEvery { remoteRepository.fetchAnimeFullById(1) } returns Result.success(season1)
        coEvery { remoteRepository.fetchAnimeFullById(2) } returns Result.success(season2)

        useCase(1).test {
            val first = awaitItem()
            assertThat(first.title).isEqualTo("Season 1")
            assertThat(first.seasons).hasSize(1)
            assertThat(first.isResolvingPrequels).isFalse()
            assertThat(first.isResolvingSequels).isTrue()

            val second = awaitItem()
            assertThat(second.seasons).hasSize(2)
            assertThat(second.seasons[0].malId).isEqualTo(1)
            assertThat(second.seasons[1].malId).isEqualTo(2)
            assertThat(second.isResolvingSequels).isFalse()

            val final_ = awaitItem()
            assertThat(final_.isResolvingPrequels).isFalse()
            assertThat(final_.isResolvingSequels).isFalse()

            awaitComplete()
        }
    }

    @Test
    fun `emits progressively when walking prequels then sequels`() = runTest {
        val season1 = AnimeFullDetails(
            malId = 1,
            title = "Season 1",
            type = "TV",
            episodes = 24,
            sequels = listOf(SequelInfo(2, "Season 2")),
            prequels = emptyList()
        )
        val season2 = AnimeFullDetails(
            malId = 2,
            title = "Season 2",
            type = "TV",
            episodes = 12,
            sequels = listOf(SequelInfo(3, "Season 3")),
            prequels = listOf(SequelInfo(1, "Season 1"))
        )
        val season3 = AnimeFullDetails(
            malId = 3,
            title = "Season 3",
            type = "TV",
            episodes = 12,
            sequels = emptyList(),
            prequels = listOf(SequelInfo(2, "Season 2"))
        )
        coEvery { remoteRepository.fetchAnimeFullById(1) } returns Result.success(season1)
        coEvery { remoteRepository.fetchAnimeFullById(2) } returns Result.success(season2)
        coEvery { remoteRepository.fetchAnimeFullById(3) } returns Result.success(season3)

        useCase(2).test {
            val initial = awaitItem()
            assertThat(initial.title).isEqualTo("Season 2")
            assertThat(initial.seasons).hasSize(1)
            assertThat(initial.seasons[0].malId).isEqualTo(2)
            assertThat(initial.isResolvingPrequels).isTrue()
            assertThat(initial.isResolvingSequels).isTrue()

            val withPrequel = awaitItem()
            assertThat(withPrequel.title).isEqualTo("Season 1")
            assertThat(withPrequel.seasons).hasSize(2)
            assertThat(withPrequel.seasons[0].malId).isEqualTo(1)
            assertThat(withPrequel.seasons[1].malId).isEqualTo(2)
            assertThat(withPrequel.isResolvingPrequels).isFalse()
            assertThat(withPrequel.isResolvingSequels).isTrue()

            val withSequel = awaitItem()
            assertThat(withSequel.seasons).hasSize(3)
            assertThat(withSequel.seasons[2].malId).isEqualTo(3)
            assertThat(withSequel.isResolvingSequels).isFalse()

            val final_ = awaitItem()
            assertThat(final_.isResolvingPrequels).isFalse()
            assertThat(final_.isResolvingSequels).isFalse()
            assertThat(final_.seasons).hasSize(3)

            awaitComplete()
        }
    }

    @Test
    fun `uses root metadata for title after walking prequels`() = runTest {
        val root = AnimeFullDetails(
            malId = 1,
            title = "Root Title",
            imageUrl = "root.jpg",
            synopsis = "Root synopsis",
            genres = listOf("Action"),
            type = "TV",
            episodes = 24,
            sequels = listOf(SequelInfo(2, "Season 2")),
            prequels = emptyList()
        )
        val start = AnimeFullDetails(
            malId = 2,
            title = "Season 2",
            type = "TV",
            episodes = 12,
            sequels = emptyList(),
            prequels = listOf(SequelInfo(1, "Root Title"))
        )
        coEvery { remoteRepository.fetchAnimeFullById(2) } returns Result.success(start)
        coEvery { remoteRepository.fetchAnimeFullById(1) } returns Result.success(root)

        useCase(2).test {
            val initial = awaitItem()
            assertThat(initial.title).isEqualTo("Season 2")

            val withRoot = awaitItem()
            assertThat(withRoot.title).isEqualTo("Root Title")
            assertThat(withRoot.imageUrl).isEqualTo("root.jpg")
            assertThat(withRoot.synopsis).isEqualTo("Root synopsis")
            assertThat(withRoot.genres).containsExactly("Action")

            val final_ = awaitItem()
            assertThat(final_.title).isEqualTo("Root Title")

            awaitComplete()
        }
    }

    @Test
    fun `throws when initial fetch fails`() = runTest {
        coEvery { remoteRepository.fetchAnimeFullById(999) } returns Result.failure(Exception("Not found"))

        useCase(999).test {
            awaitError()
        }
    }

    @Test
    fun `stops walking prequels when type is not allowed`() = runTest {
        val ova = AnimeFullDetails(
            malId = 1,
            title = "OVA",
            type = "OVA",
            episodes = 1,
            sequels = listOf(SequelInfo(2, "Season 1")),
            prequels = emptyList()
        )
        val start = AnimeFullDetails(
            malId = 2,
            title = "Season 1",
            type = "TV",
            episodes = 12,
            sequels = emptyList(),
            prequels = listOf(SequelInfo(1, "OVA"))
        )
        coEvery { remoteRepository.fetchAnimeFullById(2) } returns Result.success(start)
        coEvery { remoteRepository.fetchAnimeFullById(1) } returns Result.success(ova)

        useCase(2).test {
            val initial = awaitItem()
            assertThat(initial.seasons).hasSize(1)
            assertThat(initial.isResolvingPrequels).isTrue()

            val final_ = awaitItem()
            assertThat(final_.seasons).hasSize(1)
            assertThat(final_.isResolvingPrequels).isFalse()
            assertThat(final_.isResolvingSequels).isFalse()

            awaitComplete()
        }
    }
}
