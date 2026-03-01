package com.vuzeda.animewatchlist.tracker.ui.screens.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.ResolvedSeries
import com.vuzeda.animewatchlist.tracker.domain.model.SearchResult
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.SeasonData
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddSeasonsToAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.FetchSeasonDetailUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.FindAnimeBySeasonMalIdUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.GetSeasonsForAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ResolveAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.SearchAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.UpdateAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.UpdateSeasonUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private val resolveAnimeUseCase: ResolveAnimeUseCase = mockk()
    private val addAnimeUseCase: AddAnimeUseCase = mockk()
    private val updateAnimeUseCase: UpdateAnimeUseCase = mockk(relaxed = true)
    private val updateSeasonUseCase: UpdateSeasonUseCase = mockk(relaxed = true)
    private val getSeasonsForAnimeUseCase: GetSeasonsForAnimeUseCase = mockk()
    private val addSeasonsToAnimeUseCase: AddSeasonsToAnimeUseCase = mockk(relaxed = true)
    private val findAnimeBySeasonMalIdUseCase: FindAnimeBySeasonMalIdUseCase = mockk()

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
        coEvery { findAnimeBySeasonMalIdUseCase(any()) } returns null
        coEvery { resolveAnimeUseCase(any()) } returns Result.success(
            ResolvedSeries(
                title = "One Punch Man",
                seasons = listOf(SeasonData(malId = 21, title = "Season 1", type = "TV"))
            )
        )
        coEvery { getSeasonsForAnimeUseCase(any()) } returns listOf(
            Season(id = 1L, animeId = 10L, malId = 21, title = "Season 1", orderIndex = 0)
        )
        viewModel = SearchViewModel(
            searchAnimeUseCase = searchAnimeUseCase,
            fetchSeasonDetailUseCase = fetchSeasonDetailUseCase,
            resolveAnimeUseCase = resolveAnimeUseCase,
            addAnimeUseCase = addAnimeUseCase,
            updateAnimeUseCase = updateAnimeUseCase,
            updateSeasonUseCase = updateSeasonUseCase,
            getSeasonsForAnimeUseCase = getSeasonsForAnimeUseCase,
            addSeasonsToAnimeUseCase = addSeasonsToAnimeUseCase,
            findAnimeBySeasonMalIdUseCase = findAnimeBySeasonMalIdUseCase
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

            val loading = awaitItem()
            assertThat(loading.isLoading).isTrue()

            val loaded = awaitItem()
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
    fun `addToWatchlist immediately adds single season and updates addedMalIds`() = runTest {
        coEvery { fetchSeasonDetailUseCase(21) } returns Result.success(sampleDetails)
        coEvery { addAnimeUseCase(any(), any(), any()) } returns 10L

        viewModel.uiState.test {
            awaitItem()

            viewModel.onAddClick(sampleResult)
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.addToWatchlist(WatchStatus.PLAN_TO_WATCH)
            testDispatcher.scheduler.advanceUntilIdle()

            val updated = expectMostRecentItem()
            assertThat(updated.selectedResultForAdd).isNull()
            assertThat(updated.addedMalIds).contains(21)
            assertThat(updated.snackbarMessage).isEqualTo("One Punch Man")

            coVerify { addAnimeUseCase(any(), match { it.size == 1 }, eq(WatchStatus.PLAN_TO_WATCH)) }
        }
    }

    @Test
    fun `addToWatchlist triggers background resolution of remaining seasons`() = runTest {
        val detailsWithSequels = sampleDetails.copy(
            sequels = listOf(com.vuzeda.animewatchlist.tracker.domain.model.SequelInfo(22, "Season 2"))
        )
        coEvery { fetchSeasonDetailUseCase(21) } returns Result.success(detailsWithSequels)
        coEvery { addAnimeUseCase(any(), any(), any()) } returns 10L
        coEvery { resolveAnimeUseCase(21) } returns Result.success(
            ResolvedSeries(
                title = "One Punch Man",
                seasons = listOf(
                    SeasonData(malId = 21, title = "Season 1", type = "TV"),
                    SeasonData(malId = 22, title = "Season 2", type = "TV")
                )
            )
        )

        viewModel.uiState.test {
            awaitItem()

            viewModel.onAddClick(sampleResult)
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.addToWatchlist(WatchStatus.PLAN_TO_WATCH)
            testDispatcher.scheduler.advanceUntilIdle()

            val updated = expectMostRecentItem()
            assertThat(updated.addedMalIds).containsAtLeast(21, 22)

            coVerify { addSeasonsToAnimeUseCase(10L, any()) }
            coVerify { updateAnimeUseCase(match { it.id == 10L && it.title == "One Punch Man" }) }
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
            awaitItem()
            awaitItem()

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
            awaitItem()
            awaitItem()

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
            awaitItem()
            awaitItem()

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
}
