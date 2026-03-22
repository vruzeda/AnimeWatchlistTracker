package com.vuzeda.animewatchlist.tracker.module.ui.screens.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.usecase.AddAnimeFromDetailsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FetchSeasonDetailUseCase
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveWatchlistMalIdsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.RemoveAnimeByMalIdUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SearchAnimeUseCase
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
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val searchAnimeUseCase: SearchAnimeUseCase = mockk()
    private val fetchSeasonDetailUseCase: FetchSeasonDetailUseCase = mockk()
    private val addAnimeFromDetailsUseCase: AddAnimeFromDetailsUseCase = mockk()
    private val removeAnimeByMalIdUseCase: RemoveAnimeByMalIdUseCase = mockk()
    private val watchlistMalIdsFlow = MutableStateFlow<Set<Int>>(emptySet())
    private val observeWatchlistMalIdsUseCase: ObserveWatchlistMalIdsUseCase = mockk()
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase = mockk()

    private lateinit var viewModel: SearchViewModel

    private val sampleResult = SearchResult(malId = 21, title = "One Punch Man", score = 8.5)
    private val sampleResultB = SearchResult(malId = 30, title = "Attack on Titan", score = 9.0)
    private val sampleResultC = SearchResult(malId = 40, title = "Bleach", score = 7.9)

    private val multiResults = listOf(sampleResult, sampleResultB, sampleResultC)

    private val sampleDetails = AnimeFullDetails(
        malId = 21,
        title = "One Punch Man",
        type = "TV",
        episodes = 12,
        score = 8.5,
        sequels = emptyList(),
        prequels = emptyList()
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        watchlistMalIdsFlow.value = emptySet()
        every { observeWatchlistMalIdsUseCase() } returns watchlistMalIdsFlow
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.DEFAULT)
        viewModel = SearchViewModel(
            searchAnimeUseCase = searchAnimeUseCase,
            fetchSeasonDetailUseCase = fetchSeasonDetailUseCase,
            addAnimeFromDetailsUseCase = addAnimeFromDetailsUseCase,
            removeAnimeByMalIdUseCase = removeAnimeByMalIdUseCase,
            observeWatchlistMalIdsUseCase = observeWatchlistMalIdsUseCase,
            observeTitleLanguageUseCase = observeTitleLanguageUseCase
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.query).isEmpty()
            assertThat(state.results).isEmpty()
            assertThat(state.isLoading).isFalse()
            assertThat(state.hasSearched).isFalse()
        }
    }

    @Test
    fun `updateQuery updates the query in state`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.updateQuery("one punch")

            val updated = awaitItem()
            assertThat(updated.query).isEqualTo("one punch")
        }
    }

    @Test
    fun `search with results updates state correctly`() = runTest {
        coEvery { searchAnimeUseCase("one punch") } returns Result.success(listOf(sampleResult))

        viewModel.uiState.test {
            awaitItem()

            viewModel.updateQuery("one punch")
            awaitItem()

            viewModel.search()
            testDispatcher.scheduler.advanceUntilIdle()

            val loaded = expectMostRecentItem()
            assertThat(loaded.isLoading).isFalse()
            assertThat(loaded.results).hasSize(1)
            assertThat(loaded.displayedResults).hasSize(1)
            assertThat(loaded.results[0].title).isEqualTo("One Punch Man")
            assertThat(loaded.hasSearched).isTrue()
        }
    }

    @Test
    fun `search with error updates error state`() = runTest {
        coEvery { searchAnimeUseCase("test") } returns Result.failure(IOException("Network error"))

        viewModel.uiState.test {
            awaitItem()

            viewModel.updateQuery("test")
            awaitItem()

            viewModel.search()

            awaitItem()

            val error = awaitItem()
            assertThat(error.isLoading).isFalse()
            assertThat(error.errorMessage).isEqualTo("Network error")
            assertThat(error.hasSearched).isTrue()
        }
    }

    @Test
    fun `search with blank query does nothing`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.updateQuery("   ")
            awaitItem()

            viewModel.search()

            expectNoEvents()
        }
    }

    @Test
    fun `onResultClick sets pending navigation malId`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onResultClick(sampleResult)

            val navigating = awaitItem()
            assertThat(navigating.pendingNavigationMalId).isEqualTo(21)
        }
    }

    @Test
    fun `onAddClick fetches details and sets selectedResultForAdd`() = runTest {
        coEvery { fetchSeasonDetailUseCase(21) } returns Result.success(sampleDetails)

        viewModel.uiState.test {
            awaitItem()

            viewModel.onAddClick(sampleResult)

            val resolving = awaitItem()
            assertThat(resolving.resolvingMalId).isEqualTo(21)

            val resolved = awaitItem()
            assertThat(resolved.resolvingMalId).isNull()
            assertThat(resolved.selectedResultForAdd).isEqualTo(sampleResult)
        }
    }

    @Test
    fun `dismissBottomSheet clears selected result`() = runTest {
        coEvery { fetchSeasonDetailUseCase(21) } returns Result.success(sampleDetails)

        viewModel.uiState.test {
            awaitItem()

            viewModel.onAddClick(sampleResult)
            awaitItem()
            awaitItem()

            viewModel.dismissBottomSheet()

            val dismissed = awaitItem()
            assertThat(dismissed.selectedResultForAdd).isNull()
        }
    }

    @Test
    fun `addToWatchlist adds anime and shows snackbar`() = runTest {
        coEvery { fetchSeasonDetailUseCase(21) } returns Result.success(sampleDetails)
        coEvery { addAnimeFromDetailsUseCase(any(), any()) } returns 10L

        viewModel.uiState.test {
            awaitItem()

            viewModel.onAddClick(sampleResult)
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.addToWatchlist(WatchStatus.PLAN_TO_WATCH)
            testDispatcher.scheduler.advanceUntilIdle()

            val updated = expectMostRecentItem()
            assertThat(updated.selectedResultForAdd).isNull()
            assertThat(updated.snackbarMessage).isEqualTo("One Punch Man")

            coVerify { addAnimeFromDetailsUseCase(sampleDetails, WatchStatus.PLAN_TO_WATCH) }
        }
    }

    @Test
    fun `addedMalIds updates reactively from watchlist flow`() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.addedMalIds).isEmpty()

            watchlistMalIdsFlow.value = setOf(21)

            val updated = awaitItem()
            assertThat(updated.addedMalIds).containsExactly(21)
        }
    }

    @Test
    fun `onNavigated clears pending navigation`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onResultClick(sampleResult)
            val withNav = awaitItem()
            assertThat(withNav.pendingNavigationMalId).isEqualTo(21)

            viewModel.onNavigated()

            val cleared = awaitItem()
            assertThat(cleared.pendingNavigationMalId).isNull()
        }
    }

    @Test
    fun `selectSort toggles direction when same option is selected again`() = runTest {
        coEvery { searchAnimeUseCase("anime") } returns Result.success(multiResults)

        viewModel.uiState.test {
            awaitItem()

            viewModel.updateQuery("anime")
            awaitItem()
            viewModel.search()
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.selectSort(SearchSortOption.ALPHABETICAL)
            val ascending = awaitItem()
            assertThat(ascending.isSortAscending).isTrue()
            assertThat(ascending.displayedResults[0].title).isEqualTo("Attack on Titan")

            viewModel.selectSort(SearchSortOption.ALPHABETICAL)
            val descending = awaitItem()
            assertThat(descending.isSortAscending).isFalse()
            assertThat(descending.displayedResults[0].title).isEqualTo("One Punch Man")
        }
    }

    @Test
    fun `selectSort with ALPHABETICAL sorts results by title`() = runTest {
        coEvery { searchAnimeUseCase("anime") } returns Result.success(multiResults)

        viewModel.uiState.test {
            awaitItem()

            viewModel.updateQuery("anime")
            awaitItem()
            viewModel.search()
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.selectSort(SearchSortOption.ALPHABETICAL)

            val sorted = awaitItem()
            assertThat(sorted.sortOption).isEqualTo(SearchSortOption.ALPHABETICAL)
            assertThat(sorted.displayedResults[0].title).isEqualTo("Attack on Titan")
            assertThat(sorted.displayedResults[1].title).isEqualTo("Bleach")
            assertThat(sorted.displayedResults[2].title).isEqualTo("One Punch Man")
        }
    }

    @Test
    fun `selectSort with SCORE sorts results by score descending`() = runTest {
        coEvery { searchAnimeUseCase("anime") } returns Result.success(multiResults)

        viewModel.uiState.test {
            awaitItem()

            viewModel.updateQuery("anime")
            awaitItem()
            viewModel.search()
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.selectSort(SearchSortOption.SCORE)

            val sorted = awaitItem()
            assertThat(sorted.displayedResults[0].title).isEqualTo("Attack on Titan")
            assertThat(sorted.displayedResults[1].title).isEqualTo("One Punch Man")
            assertThat(sorted.displayedResults[2].title).isEqualTo("Bleach")
        }
    }

    @Test
    fun `clearSnackbar clears the snackbar message`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.snackbarMessage).isNull()

            viewModel.clearSnackbar()

            expectNoEvents()
        }
    }

    @Test
    fun `onRemoveClick sets selectedResultForDelete`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onRemoveClick(sampleResult)

            val updated = awaitItem()
            assertThat(updated.selectedResultForDelete).isEqualTo(sampleResult)
        }
    }

    @Test
    fun `dismissDeleteConfirmation clears selectedResultForDelete`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onRemoveClick(sampleResult)
            awaitItem()

            viewModel.dismissDeleteConfirmation()

            val dismissed = awaitItem()
            assertThat(dismissed.selectedResultForDelete).isNull()
        }
    }

    @Test
    fun `refresh re-runs search and updates results`() = runTest {
        coEvery { searchAnimeUseCase("one punch") } returns Result.success(listOf(sampleResult))

        viewModel.uiState.test {
            awaitItem()

            viewModel.updateQuery("one punch")
            awaitItem()
            viewModel.search()
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.refresh()
            testDispatcher.scheduler.advanceUntilIdle()

            val refreshed = expectMostRecentItem()
            assertThat(refreshed.isRefreshing).isFalse()
            assertThat(refreshed.results).hasSize(1)
        }
    }

    @Test
    fun `refresh does nothing when hasSearched is false`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.refresh()

            expectNoEvents()
        }
    }

    @Test
    fun `confirmRemoveFromWatchlist deletes anime`() = runTest {
        coEvery { removeAnimeByMalIdUseCase(21) } returns setOf(21, 22)
        watchlistMalIdsFlow.value = setOf(21)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.onRemoveClick(sampleResult)
            val withDialog = awaitItem()
            assertThat(withDialog.selectedResultForDelete).isEqualTo(sampleResult)

            viewModel.confirmRemoveFromWatchlist()
            testDispatcher.scheduler.advanceUntilIdle()

            val afterRemove = expectMostRecentItem()
            assertThat(afterRemove.selectedResultForDelete).isNull()

            coVerify { removeAnimeByMalIdUseCase(21) }
        }
    }
}
