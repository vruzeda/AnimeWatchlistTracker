package com.vuzeda.animewatchlist.tracker.ui.screens.seasondetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.domain.model.EpisodePage
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.usecase.FetchEpisodesUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveSeasonByIdUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.UpdateSeasonProgressUseCase
import com.vuzeda.animewatchlist.tracker.ui.navigation.Route
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeasonDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val observeSeasonByIdUseCase: ObserveSeasonByIdUseCase = mockk()
    private val fetchEpisodesUseCase: FetchEpisodesUseCase = mockk()
    private val updateSeasonProgressUseCase: UpdateSeasonProgressUseCase = mockk(relaxed = true)

    private val sampleSeason = Season(
        id = 1L,
        animeId = 1L,
        malId = 16498,
        title = "Attack on Titan",
        episodeCount = 25,
        currentEpisode = 12,
        score = 8.5,
        type = "TV",
        airingStatus = "Finished Airing"
    )

    private val sampleEpisodes = listOf(
        EpisodeInfo(number = 1, title = "Episode 1", aired = "2013-04-07", isFiller = false, isRecap = false),
        EpisodeInfo(number = 2, title = "Episode 2", aired = "2013-04-14", isFiller = false, isRecap = false)
    )

    private lateinit var seasonFlow: MutableStateFlow<Season?>

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        seasonFlow = MutableStateFlow(sampleSeason)
        every { observeSeasonByIdUseCase(1L) } returns seasonFlow
        coEvery { fetchEpisodesUseCase(malId = 16498, page = 1) } returns Result.success(
            EpisodePage(episodes = sampleEpisodes, hasNextPage = true, nextPage = 2)
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(seasonId: Long = 1L): SeasonDetailViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(Route.SeasonDetail.ARG_SEASON_ID to seasonId)
        )
        return SeasonDetailViewModel(
            savedStateHandle = savedStateHandle,
            observeSeasonByIdUseCase = observeSeasonByIdUseCase,
            fetchEpisodesUseCase = fetchEpisodesUseCase,
            updateSeasonProgressUseCase = updateSeasonProgressUseCase
        )
    }

    @Test
    fun `loads season and episodes on init`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(SeasonDetailUiState.Loading::class.java)

            val loadingEpisodes = awaitItem() as SeasonDetailUiState.Success
            assertThat(loadingEpisodes.season.title).isEqualTo("Attack on Titan")
            assertThat(loadingEpisodes.isLoadingEpisodes).isTrue()

            val loaded = awaitItem() as SeasonDetailUiState.Success
            assertThat(loaded.episodes).hasSize(2)
            assertThat(loaded.hasMoreEpisodes).isTrue()
            assertThat(loaded.isLoadingEpisodes).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shows not found when season does not exist`() = runTest {
        val emptyFlow = MutableStateFlow<Season?>(null)
        every { observeSeasonByIdUseCase(999L) } returns emptyFlow

        val viewModel = createViewModel(seasonId = 999L)

        viewModel.uiState.test {
            awaitItem()

            val notFound = awaitItem()
            assertThat(notFound).isInstanceOf(SeasonDetailUiState.NotFound::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMoreEpisodes appends episodes`() = runTest {
        val moreEpisodes = listOf(
            EpisodeInfo(number = 3, title = "Episode 3", aired = "2013-04-21", isFiller = false, isRecap = false)
        )
        coEvery { fetchEpisodesUseCase(malId = 16498, page = 2) } returns Result.success(
            EpisodePage(episodes = moreEpisodes, hasNextPage = false, nextPage = 3)
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val initial = expectMostRecentItem() as SeasonDetailUiState.Success
            assertThat(initial.episodes).hasSize(2)
            assertThat(initial.hasMoreEpisodes).isTrue()

            viewModel.loadMoreEpisodes()
            testDispatcher.scheduler.advanceUntilIdle()

            val updated = expectMostRecentItem() as SeasonDetailUiState.Success
            assertThat(updated.episodes).hasSize(3)
            assertThat(updated.hasMoreEpisodes).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateEpisodeProgress delegates to use case`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.updateEpisodeProgress(15)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { updateSeasonProgressUseCase(sampleSeason, 15) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateEpisodeProgress clamps to max episodes`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.updateEpisodeProgress(100)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { updateSeasonProgressUseCase(sampleSeason, 25) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateEpisodeProgress clamps to zero minimum`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.updateEpisodeProgress(-5)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { updateSeasonProgressUseCase(sampleSeason, 0) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `season updates reactively from database`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val initial = expectMostRecentItem() as SeasonDetailUiState.Success
            assertThat(initial.season.currentEpisode).isEqualTo(12)

            seasonFlow.value = sampleSeason.copy(currentEpisode = 20)

            val updated = awaitItem() as SeasonDetailUiState.Success
            assertThat(updated.season.currentEpisode).isEqualTo(20)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
