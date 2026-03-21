package com.vuzeda.animewatchlist.tracker.module.ui.screens.seasondetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.usecase.AddAnimeFromDetailsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.AddSeasonToWatchlistUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.DeleteSeasonUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FetchEpisodesUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FetchSeasonDetailUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FindSeasonIdByMalIdUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveSeasonByIdUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveSeasonsForAnimeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ToggleSeasonEpisodeNotificationsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.UpdateSeasonProgressUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.UpdateSeasonStatusUseCase
import io.mockk.coEvery
import io.mockk.coVerify
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
class SeasonDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val observeSeasonByIdUseCase: ObserveSeasonByIdUseCase = mockk()
    private val observeSeasonsForAnimeUseCase: ObserveSeasonsForAnimeUseCase = mockk()
    private val fetchSeasonDetailUseCase: FetchSeasonDetailUseCase = mockk()
    private val fetchEpisodesUseCase: FetchEpisodesUseCase = mockk()
    private val updateSeasonProgressUseCase: UpdateSeasonProgressUseCase = mockk(relaxed = true)
    private val updateSeasonStatusUseCase: UpdateSeasonStatusUseCase = mockk(relaxed = true)
    private val deleteSeasonUseCase: DeleteSeasonUseCase = mockk(relaxed = true)
    private val addSeasonToWatchlistUseCase: AddSeasonToWatchlistUseCase = mockk(relaxed = true)
    private val addAnimeFromDetailsUseCase: AddAnimeFromDetailsUseCase = mockk(relaxed = true)
    private val findSeasonIdByMalIdUseCase: FindSeasonIdByMalIdUseCase = mockk()
    private val toggleSeasonEpisodeNotificationsUseCase: ToggleSeasonEpisodeNotificationsUseCase = mockk(relaxed = true)
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase = mockk()

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
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.DEFAULT)
        every { observeSeasonsForAnimeUseCase(any()) } returns flowOf(listOf(sampleSeason))
        coEvery { findSeasonIdByMalIdUseCase(any()) } returns null
        coEvery { fetchEpisodesUseCase(malId = 16498, page = 1) } returns Result.success(
            EpisodePage(episodes = sampleEpisodes, hasNextPage = true, nextPage = 2)
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(seasonId: Long = 1L, malId: Int = 0): SeasonDetailViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "seasonId" to seasonId,
                "malId" to malId
            )
        )
        return SeasonDetailViewModel(
            savedStateHandle = savedStateHandle,
            observeSeasonByIdUseCase = observeSeasonByIdUseCase,
            observeSeasonsForAnimeUseCase = observeSeasonsForAnimeUseCase,
            fetchSeasonDetailUseCase = fetchSeasonDetailUseCase,
            fetchEpisodesUseCase = fetchEpisodesUseCase,
            updateSeasonProgressUseCase = updateSeasonProgressUseCase,
            updateSeasonStatusUseCase = updateSeasonStatusUseCase,
            deleteSeasonUseCase = deleteSeasonUseCase,
            addSeasonToWatchlistUseCase = addSeasonToWatchlistUseCase,
            addAnimeFromDetailsUseCase = addAnimeFromDetailsUseCase,
            findSeasonIdByMalIdUseCase = findSeasonIdByMalIdUseCase,
            toggleSeasonEpisodeNotificationsUseCase = toggleSeasonEpisodeNotificationsUseCase,
            observeTitleLanguageUseCase = observeTitleLanguageUseCase
        )
    }

    @Test
    fun `loads season and episodes on init`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(SeasonDetailUiState.Loading::class.java)

            testDispatcher.scheduler.advanceUntilIdle()

            val loaded = expectMostRecentItem() as SeasonDetailUiState.Success
            assertThat(loaded.season.title).isEqualTo("Attack on Titan")
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

    @Test
    fun `loads season from API when malId is provided`() = runTest {
        val apiDetails = AnimeFullDetails(
            malId = 50265,
            title = "Spy x Family",
            imageUrl = "https://example.com/spy.jpg",
            type = "TV",
            episodes = 12,
            score = 8.53,
            airingStatus = "Finished Airing",
            sequels = emptyList(),
            prequels = emptyList()
        )
        coEvery { fetchSeasonDetailUseCase(50265) } returns Result.success(apiDetails)
        coEvery { fetchEpisodesUseCase(malId = 50265, page = 1) } returns Result.success(
            EpisodePage(episodes = sampleEpisodes, hasNextPage = false, nextPage = 2)
        )

        val viewModel = createViewModel(seasonId = 0L, malId = 50265)

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(SeasonDetailUiState.Loading::class.java)

            testDispatcher.scheduler.advanceUntilIdle()

            val success = expectMostRecentItem() as SeasonDetailUiState.Success
            assertThat(success.season.title).isEqualTo("Spy x Family")
            assertThat(success.season.malId).isEqualTo(50265)
            assertThat(success.isInWatchlist).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shows not found when API fetch fails for malId`() = runTest {
        coEvery { fetchSeasonDetailUseCase(999) } returns Result.failure(Exception("Not found"))

        val viewModel = createViewModel(seasonId = 0L, malId = 999)

        viewModel.uiState.test {
            awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()

            val notFound = expectMostRecentItem()
            assertThat(notFound).isInstanceOf(SeasonDetailUiState.NotFound::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shows not found when both seasonId and malId are zero`() = runTest {
        val viewModel = createViewModel(seasonId = 0L, malId = 0)

        viewModel.uiState.test {
            val notFound = awaitItem()
            assertThat(notFound).isInstanceOf(SeasonDetailUiState.NotFound::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showDeleteConfirmation and dismissDeleteConfirmation toggle visibility`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.showDeleteConfirmation()
            val shown = awaitItem() as SeasonDetailUiState.Success
            assertThat(shown.isDeleteConfirmationVisible).isTrue()

            viewModel.dismissDeleteConfirmation()
            val hidden = awaitItem() as SeasonDetailUiState.Success
            assertThat(hidden.isDeleteConfirmationVisible).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `confirmDelete calls delete use case and marks season as not in watchlist`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.confirmDelete()
            testDispatcher.scheduler.advanceUntilIdle()

            val deleted = expectMostRecentItem() as SeasonDetailUiState.Success
            assertThat(deleted.isInWatchlist).isFalse()
            assertThat(deleted.isDeleteConfirmationVisible).isFalse()
            coVerify { deleteSeasonUseCase(sampleSeason) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showAddSheet and dismissAddSheet toggle visibility`() = runTest {
        val apiDetails = AnimeFullDetails(
            malId = 50265,
            title = "Spy x Family",
            type = "TV",
            episodes = 12,
            score = 8.53,
            sequels = emptyList(),
            prequels = emptyList()
        )
        coEvery { fetchSeasonDetailUseCase(50265) } returns Result.success(apiDetails)
        coEvery { fetchEpisodesUseCase(malId = 50265, page = 1) } returns Result.success(
            EpisodePage(episodes = emptyList(), hasNextPage = false, nextPage = 2)
        )

        val viewModel = createViewModel(seasonId = 0L, malId = 50265)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val initial = expectMostRecentItem() as SeasonDetailUiState.Success
            assertThat(initial.isInWatchlist).isFalse()

            viewModel.showAddSheet()
            val shown = awaitItem() as SeasonDetailUiState.Success
            assertThat(shown.isAddSheetVisible).isTrue()

            viewModel.dismissAddSheet()
            val hidden = awaitItem() as SeasonDetailUiState.Success
            assertThat(hidden.isAddSheetVisible).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addToWatchlist adds season and transitions to watchlist mode`() = runTest {
        val apiDetails = AnimeFullDetails(
            malId = 50265,
            title = "Spy x Family",
            type = "TV",
            episodes = 12,
            score = 8.53,
            sequels = emptyList(),
            prequels = emptyList()
        )
        coEvery { fetchSeasonDetailUseCase(50265) } returns Result.success(apiDetails)
        coEvery { fetchEpisodesUseCase(malId = 50265, page = 1) } returns Result.success(
            EpisodePage(episodes = emptyList(), hasNextPage = false, nextPage = 2)
        )
        coEvery { addAnimeFromDetailsUseCase(any(), any()) } returns 10L
        coEvery { findSeasonIdByMalIdUseCase(50265) } returns null

        val addedSeasonFlow = MutableStateFlow<Season?>(sampleSeason.copy(id = 5L, animeId = 10L, malId = 50265, title = "Spy x Family"))
        every { observeSeasonByIdUseCase(5L) } returns addedSeasonFlow

        val viewModel = createViewModel(seasonId = 0L, malId = 50265)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val resolved = expectMostRecentItem() as SeasonDetailUiState.Success
            assertThat(resolved.isInWatchlist).isFalse()

            viewModel.addToWatchlist(WatchStatus.PLAN_TO_WATCH)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { addAnimeFromDetailsUseCase(apiDetails, WatchStatus.PLAN_TO_WATCH) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleEpisodeNotifications delegates to use case`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.toggleEpisodeNotifications()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify {
                toggleSeasonEpisodeNotificationsUseCase(
                    seasonId = 1L,
                    enabled = true
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isInWatchlist reflects season isInWatchlist field`() = runTest {
        val nonWatchlistSeason = sampleSeason.copy(isInWatchlist = false)
        seasonFlow.value = nonWatchlistSeason

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val state = expectMostRecentItem() as SeasonDetailUiState.Success
            assertThat(state.isInWatchlist).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isLastSeason counts only in-watchlist siblings`() = runTest {
        val nonWatchlistSibling = Season(id = 2L, animeId = 1L, malId = 200, title = "S2", isInWatchlist = false)
        every { observeSeasonsForAnimeUseCase(1L) } returns flowOf(listOf(sampleSeason, nonWatchlistSibling))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val state = expectMostRecentItem() as SeasonDetailUiState.Success
            assertThat(state.isLastSeason).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addToWatchlist for in-DB non-watchlist season calls addSeasonToWatchlistUseCase`() = runTest {
        val nonWatchlistSeason = sampleSeason.copy(isInWatchlist = false)
        seasonFlow.value = nonWatchlistSeason

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.addToWatchlist(WatchStatus.WATCHING)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { addSeasonToWatchlistUseCase(nonWatchlistSeason, WatchStatus.WATCHING) }
            coVerify(exactly = 0) { addAnimeFromDetailsUseCase(any(), any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showStatusSheet and dismissStatusSheet toggle visibility`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.showStatusSheet()
            val shown = awaitItem() as SeasonDetailUiState.Success
            assertThat(shown.isStatusSheetVisible).isTrue()

            viewModel.dismissStatusSheet()
            val hidden = awaitItem() as SeasonDetailUiState.Success
            assertThat(hidden.isStatusSheetVisible).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateStatus delegates to use case`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.updateStatus(WatchStatus.COMPLETED)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { updateSeasonStatusUseCase(sampleSeason, WatchStatus.COMPLETED) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `navigateToAnimeDetail sets pendingNavigationMalId`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.navigateToAnimeDetail()

            val updated = awaitItem() as SeasonDetailUiState.Success
            assertThat(updated.pendingNavigationMalId).isEqualTo(16498)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
