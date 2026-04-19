package com.vuzeda.animewatchlist.tracker.module.ui.screens.seasons

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsTracker
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchType
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.usecase.AddAnimeFromDetailsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FetchSeasonDetailUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.GetSeasonAnimeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveSeasonFilterUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveWatchlistMalIdsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.RemoveAnimeByMalIdUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetSeasonFilterUseCase
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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SeasonsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getSeasonAnimeUseCase: GetSeasonAnimeUseCase = mockk()
    private val fetchSeasonDetailUseCase: FetchSeasonDetailUseCase = mockk()
    private val addAnimeFromDetailsUseCase: AddAnimeFromDetailsUseCase = mockk()
    private val removeAnimeByMalIdUseCase: RemoveAnimeByMalIdUseCase = mockk()
    private val watchlistMalIdsFlow = MutableStateFlow<Set<Int>>(emptySet())
    private val observeWatchlistMalIdsUseCase: ObserveWatchlistMalIdsUseCase = mockk()
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase = mockk()
    private val seasonFilterFlow = MutableStateFlow(AnimeSearchType.TV)
    private val observeSeasonFilterUseCase: ObserveSeasonFilterUseCase = mockk()
    private val setSeasonFilterUseCase: SetSeasonFilterUseCase = mockk()
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)

    private val samplePage = SeasonalAnimePage(
        results = listOf(
            SearchResult(malId = 1, title = "Frieren", score = 9.1),
            SearchResult(malId = 2, title = "Jujutsu Kaisen", score = 8.5)
        ),
        hasNextPage = true,
        currentPage = 1
    )

    private val samplePageTwo = SeasonalAnimePage(
        results = listOf(
            SearchResult(malId = 3, title = "One Piece", score = 8.7)
        ),
        hasNextPage = false,
        currentPage = 2
    )

    private val sampleDetails = AnimeFullDetails(
        malId = 1,
        title = "Frieren",
        type = "TV",
        episodes = 28,
        score = 9.1,
        sequels = emptyList(),
        prequels = emptyList()
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        watchlistMalIdsFlow.value = emptySet()
        seasonFilterFlow.value = AnimeSearchType.TV
        every { observeWatchlistMalIdsUseCase() } returns watchlistMalIdsFlow
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.DEFAULT)
        every { observeSeasonFilterUseCase() } returns seasonFilterFlow
        coEvery { setSeasonFilterUseCase(any()) } answers { seasonFilterFlow.value = firstArg() }
        coEvery { getSeasonAnimeUseCase(any(), any(), any(), any()) } returns Result.success(samplePage)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SeasonsViewModel(
        getSeasonAnimeUseCase = getSeasonAnimeUseCase,
        fetchSeasonDetailUseCase = fetchSeasonDetailUseCase,
        addAnimeFromDetailsUseCase = addAnimeFromDetailsUseCase,
        removeAnimeByMalIdUseCase = removeAnimeByMalIdUseCase,
        observeWatchlistMalIdsUseCase = observeWatchlistMalIdsUseCase,
        observeTitleLanguageUseCase = observeTitleLanguageUseCase,
        observeSeasonFilterUseCase = observeSeasonFilterUseCase,
        setSeasonFilterUseCase = setSeasonFilterUseCase,
        analyticsTracker = analyticsTracker
    )

    @Test
    fun `initial load fetches current season anime`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val loaded = expectMostRecentItem()
            assertThat(loaded.isLoading).isFalse()
            assertThat(loaded.animeList).hasSize(2)
            assertThat(loaded.animeList[0].title).isEqualTo("Frieren")
            assertThat(loaded.hasNextPage).isTrue()
        }
    }

    @Test
    fun `initial load uses stored season filter`() = runTest {
        seasonFilterFlow.value = AnimeSearchType.MOVIE

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val loaded = expectMostRecentItem()
            assertThat(loaded.seasonFilter).isEqualTo(AnimeSearchType.MOVIE)

            coVerify { getSeasonAnimeUseCase(any(), any(), any(), AnimeSearchType.MOVIE) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectNextSeason clears list and loads new season`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.selectNextSeason()
            testDispatcher.scheduler.advanceUntilIdle()

            val loaded = expectMostRecentItem()
            assertThat(loaded.isLoading).isFalse()
            assertThat(loaded.animeList).hasSize(2)
        }
    }

    @Test
    fun `selectPreviousSeason clears list and loads new season`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.selectPreviousSeason()
            testDispatcher.scheduler.advanceUntilIdle()

            val loaded = expectMostRecentItem()
            assertThat(loaded.isLoading).isFalse()
            assertThat(loaded.animeList).hasSize(2)
        }
    }

    @Test
    fun `loadMore appends results to existing list`() = runTest {
        coEvery {
            getSeasonAnimeUseCase(any(), any(), page = 2, any())
        } returns Result.success(samplePageTwo)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.loadMore()
            testDispatcher.scheduler.advanceUntilIdle()

            val loaded = expectMostRecentItem()
            assertThat(loaded.isLoadingMore).isFalse()
            assertThat(loaded.animeList).hasSize(3)
            assertThat(loaded.hasNextPage).isFalse()
            assertThat(loaded.currentPage).isEqualTo(2)
        }
    }

    @Test
    fun `loadMore does nothing when no next page`() = runTest {
        coEvery {
            getSeasonAnimeUseCase(any(), any(), any(), any())
        } returns Result.success(samplePage.copy(hasNextPage = false))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.loadMore()

            expectNoEvents()
        }
    }

    @Test
    fun `error state is set on fetch failure`() = runTest {
        coEvery {
            getSeasonAnimeUseCase(any(), any(), any(), any())
        } returns Result.failure(IOException("Network error"))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            val error = awaitItem()
            assertThat(error.isLoading).isFalse()
            assertThat(error.errorMessage).isEqualTo("Network error")
            assertThat(error.animeList).isEmpty()
        }
    }

    @Test
    fun `onResultClick sets pending navigation malId`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.onResultClick(samplePage.results[0])

            val navigating = awaitItem()
            assertThat(navigating.pendingNavigationMalId).isEqualTo(1)
        }
    }

    @Test
    fun `onNavigated clears pending navigation`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.onResultClick(samplePage.results[0])
            awaitItem()

            viewModel.onNavigated()

            val cleared = awaitItem()
            assertThat(cleared.pendingNavigationMalId).isNull()
        }
    }

    @Test
    fun `onAddClick fetches details and sets selectedResultForAdd`() = runTest {
        coEvery { fetchSeasonDetailUseCase(1) } returns Result.success(sampleDetails)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.onAddClick(samplePage.results[0])

            val resolving = awaitItem()
            assertThat(resolving.resolvingMalId).isEqualTo(1)

            val ready = awaitItem()
            assertThat(ready.resolvingMalId).isNull()
            assertThat(ready.selectedResultForAdd).isEqualTo(samplePage.results[0])
        }
    }

    @Test
    fun `addToWatchlist adds anime and shows snackbar`() = runTest {
        coEvery { fetchSeasonDetailUseCase(1) } returns Result.success(sampleDetails)
        coEvery { addAnimeFromDetailsUseCase(any(), any()) } returns 10L

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.onAddClick(samplePage.results[0])
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.addToWatchlist(WatchStatus.WATCHING)
            testDispatcher.scheduler.advanceUntilIdle()

            val added = expectMostRecentItem()
            assertThat(added.selectedResultForAdd).isNull()
            assertThat(added.snackbarMessage).isEqualTo("Frieren")
        }
    }

    @Test
    fun `addedMalIds updates reactively from watchlist flow`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertThat(initial.addedMalIds).isEmpty()

            watchlistMalIdsFlow.value = setOf(1, 2)

            val updated = awaitItem()
            assertThat(updated.addedMalIds).containsExactly(1, 2)
        }
    }

    @Test
    fun `onRemoveClick sets selectedResultForDelete`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.onRemoveClick(samplePage.results[0])

            val updated = awaitItem()
            assertThat(updated.selectedResultForDelete).isEqualTo(samplePage.results[0])
        }
    }

    @Test
    fun `confirmRemoveFromWatchlist removes anime`() = runTest {
        coEvery { removeAnimeByMalIdUseCase(1) } returns setOf(1)
        watchlistMalIdsFlow.value = setOf(1)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.onRemoveClick(samplePage.results[0])
            awaitItem()

            viewModel.confirmRemoveFromWatchlist()
            testDispatcher.scheduler.advanceUntilIdle()

            val afterRemove = expectMostRecentItem()
            assertThat(afterRemove.selectedResultForDelete).isNull()

            coVerify { removeAnimeByMalIdUseCase(1) }
        }
    }

    @Test
    fun `dismissBottomSheet clears selection`() = runTest {
        coEvery { fetchSeasonDetailUseCase(1) } returns Result.success(sampleDetails)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.onAddClick(samplePage.results[0])
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.dismissBottomSheet()

            val dismissed = awaitItem()
            assertThat(dismissed.selectedResultForAdd).isNull()
        }
    }

    @Test
    fun `selectFilter resets list and reloads with new filter`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.selectFilter(AnimeSearchType.MOVIE)
            testDispatcher.scheduler.advanceUntilIdle()

            val filtered = expectMostRecentItem()
            assertThat(filtered.seasonFilter).isEqualTo(AnimeSearchType.MOVIE)
            assertThat(filtered.currentPage).isEqualTo(1)

            coVerify { getSeasonAnimeUseCase(any(), any(), 1, AnimeSearchType.MOVIE) }
        }
    }

    @Test
    fun `selectFilter does nothing when same filter selected again`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.selectFilter(AnimeSearchType.TV)

            expectNoEvents()
        }
    }

    @Test
    fun `selectNextSeason preserves current filter`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.selectFilter(AnimeSearchType.MOVIE)
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.selectNextSeason()
            testDispatcher.scheduler.advanceUntilIdle()

            val loaded = expectMostRecentItem()
            assertThat(loaded.seasonFilter).isEqualTo(AnimeSearchType.MOVIE)

            coVerify { getSeasonAnimeUseCase(any(), any(), 1, AnimeSearchType.MOVIE) }
        }
    }

    @Test
    fun `refresh reloads current season without showing full loading state`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.refresh()
            testDispatcher.scheduler.advanceUntilIdle()

            val refreshed = expectMostRecentItem()
            assertThat(refreshed.isRefreshing).isFalse()
            assertThat(refreshed.isLoading).isFalse()
            assertThat(refreshed.animeList).hasSize(2)
        }
    }

    @Test
    fun `seasonFromMonth maps months correctly`() {
        assertThat(SeasonsViewModel.seasonFromMonth(1)).isEqualTo(AnimeSeason.WINTER)
        assertThat(SeasonsViewModel.seasonFromMonth(3)).isEqualTo(AnimeSeason.WINTER)
        assertThat(SeasonsViewModel.seasonFromMonth(4)).isEqualTo(AnimeSeason.SPRING)
        assertThat(SeasonsViewModel.seasonFromMonth(6)).isEqualTo(AnimeSeason.SPRING)
        assertThat(SeasonsViewModel.seasonFromMonth(7)).isEqualTo(AnimeSeason.SUMMER)
        assertThat(SeasonsViewModel.seasonFromMonth(9)).isEqualTo(AnimeSeason.SUMMER)
        assertThat(SeasonsViewModel.seasonFromMonth(10)).isEqualTo(AnimeSeason.FALL)
        assertThat(SeasonsViewModel.seasonFromMonth(12)).isEqualTo(AnimeSeason.FALL)
    }
}
