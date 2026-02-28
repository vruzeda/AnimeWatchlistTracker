package com.vuzeda.animewatchlist.tracker.ui.screens.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveWatchlistUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val observeWatchlistUseCase: ObserveWatchlistUseCase = mockk()

    private val sampleAnimeList = listOf(
        Anime(id = 1L, title = "Attack on Titan", status = WatchStatus.WATCHING, score = 9.0, userRating = 8, currentEpisode = 10, episodeCount = 25, addedAt = 1000L),
        Anime(id = 2L, title = "One Punch Man", status = WatchStatus.COMPLETED, score = 8.5, userRating = 9, currentEpisode = 12, episodeCount = 12, addedAt = 3000L),
        Anime(id = 3L, title = "Bleach", status = WatchStatus.WATCHING, score = 7.9, userRating = 7, currentEpisode = 50, episodeCount = 366, addedAt = 2000L)
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads all anime sorted alphabetically`() = runTest {
        every { observeWatchlistUseCase(null) } returns flowOf(sampleAnimeList)

        val viewModel = HomeViewModel(observeWatchlistUseCase)

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading.isLoading).isTrue()

            val loaded = awaitItem()
            assertThat(loaded.isLoading).isFalse()
            assertThat(loaded.animeList).hasSize(3)
            assertThat(loaded.animeList[0].title).isEqualTo("Attack on Titan")
            assertThat(loaded.animeList[1].title).isEqualTo("Bleach")
            assertThat(loaded.animeList[2].title).isEqualTo("One Punch Man")
            assertThat(loaded.selectedTab).isNull()
            assertThat(loaded.sortOption).isEqualTo(HomeSortOption.ALPHABETICAL)
        }
    }

    @Test
    fun `selectTab filters by status`() = runTest {
        every { observeWatchlistUseCase(null) } returns flowOf(sampleAnimeList)
        every { observeWatchlistUseCase(WatchStatus.WATCHING) } returns flowOf(
            listOf(sampleAnimeList[0])
        )

        val viewModel = HomeViewModel(observeWatchlistUseCase)

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectTab(WatchStatus.WATCHING)

            val tabChanged = awaitItem()
            assertThat(tabChanged.selectedTab).isEqualTo(WatchStatus.WATCHING)
            assertThat(tabChanged.isLoading).isTrue()

            val filtered = awaitItem()
            assertThat(filtered.animeList).hasSize(1)
            assertThat(filtered.animeList[0].title).isEqualTo("Attack on Titan")
            assertThat(filtered.isLoading).isFalse()
        }
    }

    @Test
    fun `selectTab with null shows all anime`() = runTest {
        every { observeWatchlistUseCase(null) } returns flowOf(sampleAnimeList)
        every { observeWatchlistUseCase(WatchStatus.WATCHING) } returns flowOf(
            listOf(sampleAnimeList[0])
        )

        val viewModel = HomeViewModel(observeWatchlistUseCase)

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectTab(WatchStatus.WATCHING)
            skipItems(2)

            viewModel.selectTab(null)

            val tabChanged = awaitItem()
            assertThat(tabChanged.selectedTab).isNull()

            val allAnime = awaitItem()
            assertThat(allAnime.animeList).hasSize(3)
        }
    }

    @Test
    fun `selectSort with MAL_SCORE sorts by score descending`() = runTest {
        every { observeWatchlistUseCase(null) } returns flowOf(sampleAnimeList)

        val viewModel = HomeViewModel(observeWatchlistUseCase)

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectSort(HomeSortOption.MAL_SCORE)

            val sorted = awaitItem()
            assertThat(sorted.sortOption).isEqualTo(HomeSortOption.MAL_SCORE)
            assertThat(sorted.animeList[0].title).isEqualTo("Attack on Titan")
            assertThat(sorted.animeList[1].title).isEqualTo("One Punch Man")
            assertThat(sorted.animeList[2].title).isEqualTo("Bleach")
        }
    }

    @Test
    fun `selectSort with USER_RATING sorts by user rating descending`() = runTest {
        every { observeWatchlistUseCase(null) } returns flowOf(sampleAnimeList)

        val viewModel = HomeViewModel(observeWatchlistUseCase)

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectSort(HomeSortOption.USER_RATING)

            val sorted = awaitItem()
            assertThat(sorted.animeList[0].title).isEqualTo("One Punch Man")
            assertThat(sorted.animeList[1].title).isEqualTo("Attack on Titan")
            assertThat(sorted.animeList[2].title).isEqualTo("Bleach")
        }
    }

    @Test
    fun `selectSort with PROGRESS sorts by progress descending`() = runTest {
        every { observeWatchlistUseCase(null) } returns flowOf(sampleAnimeList)

        val viewModel = HomeViewModel(observeWatchlistUseCase)

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectSort(HomeSortOption.PROGRESS)

            val sorted = awaitItem()
            assertThat(sorted.animeList[0].title).isEqualTo("One Punch Man")
            assertThat(sorted.animeList[1].title).isEqualTo("Attack on Titan")
            assertThat(sorted.animeList[2].title).isEqualTo("Bleach")
        }
    }

    @Test
    fun `selectSort with RECENTLY_ADDED sorts by addedAt descending`() = runTest {
        every { observeWatchlistUseCase(null) } returns flowOf(sampleAnimeList)

        val viewModel = HomeViewModel(observeWatchlistUseCase)

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectSort(HomeSortOption.RECENTLY_ADDED)

            val sorted = awaitItem()
            assertThat(sorted.sortOption).isEqualTo(HomeSortOption.RECENTLY_ADDED)
            assertThat(sorted.animeList[0].title).isEqualTo("One Punch Man")
            assertThat(sorted.animeList[1].title).isEqualTo("Bleach")
            assertThat(sorted.animeList[2].title).isEqualTo("Attack on Titan")
        }
    }

    @Test
    fun `sort persists when new data arrives from Flow`() = runTest {
        val watchlistFlow = MutableStateFlow(sampleAnimeList)
        every { observeWatchlistUseCase(null) } returns watchlistFlow

        val viewModel = HomeViewModel(observeWatchlistUseCase)

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectSort(HomeSortOption.MAL_SCORE)
            val sorted = awaitItem()
            assertThat(sorted.animeList[0].score).isEqualTo(9.0)

            val updatedList = sampleAnimeList + Anime(
                id = 4L, title = "Demon Slayer", status = WatchStatus.WATCHING,
                score = 8.7, currentEpisode = 5, episodeCount = 26
            )
            watchlistFlow.value = updatedList

            val updated = awaitItem()
            assertThat(updated.sortOption).isEqualTo(HomeSortOption.MAL_SCORE)
            assertThat(updated.animeList[0].score).isEqualTo(9.0)
            assertThat(updated.animeList[1].score).isEqualTo(8.7)
        }
    }
}
