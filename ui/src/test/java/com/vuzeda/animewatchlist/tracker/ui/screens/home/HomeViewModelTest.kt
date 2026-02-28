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
        Anime(id = 1L, title = "Attack on Titan", status = WatchStatus.WATCHING),
        Anime(id = 2L, title = "One Punch Man", status = WatchStatus.COMPLETED)
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
    fun `initial state loads all anime`() = runTest {
        every { observeWatchlistUseCase(null) } returns flowOf(sampleAnimeList)

        val viewModel = HomeViewModel(observeWatchlistUseCase)

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading.isLoading).isTrue()

            val loaded = awaitItem()
            assertThat(loaded.isLoading).isFalse()
            assertThat(loaded.animeList).hasSize(2)
            assertThat(loaded.selectedTab).isNull()
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
            assertThat(allAnime.animeList).hasSize(2)
        }
    }
}
