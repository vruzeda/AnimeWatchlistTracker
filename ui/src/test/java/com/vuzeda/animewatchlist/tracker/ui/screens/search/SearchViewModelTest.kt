package com.vuzeda.animewatchlist.tracker.ui.screens.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddAnimeToWatchlistUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveWatchlistAnimeByMalIdsUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.SearchAnimeUseCase
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
    private val addAnimeToWatchlistUseCase: AddAnimeToWatchlistUseCase = mockk()
    private val observeWatchlistAnimeByMalIdsUseCase: ObserveWatchlistAnimeByMalIdsUseCase = mockk()

    private lateinit var viewModel: SearchViewModel

    private val sampleAnime = Anime(malId = 21, title = "One Punch Man", score = 8.5, status = WatchStatus.PLAN_TO_WATCH)
    private val sampleAnimeB = Anime(malId = 30, title = "Attack on Titan", score = 9.0, status = WatchStatus.PLAN_TO_WATCH)
    private val sampleAnimeC = Anime(malId = 40, title = "Bleach", score = 7.9, status = WatchStatus.PLAN_TO_WATCH)

    private val sampleResults = listOf(sampleAnime)
    private val multiResults = listOf(sampleAnime, sampleAnimeB, sampleAnimeC)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { observeWatchlistAnimeByMalIdsUseCase(any()) } returns flowOf(emptyList())
        viewModel = SearchViewModel(searchAnimeUseCase, addAnimeToWatchlistUseCase, observeWatchlistAnimeByMalIdsUseCase)
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
        coEvery { searchAnimeUseCase("one punch") } returns Result.success(sampleResults)

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
    fun `search populates watchlistEntries from database`() = runTest {
        val watchlistAnime = listOf(
            Anime(id = 5L, malId = 21, title = "One Punch Man", status = WatchStatus.WATCHING)
        )
        coEvery { searchAnimeUseCase("one punch") } returns Result.success(sampleResults)
        every { observeWatchlistAnimeByMalIdsUseCase(listOf(21)) } returns flowOf(watchlistAnime)

        viewModel.uiState.test {
            awaitItem()

            viewModel.updateQuery("one punch")
            awaitItem()

            viewModel.search()
            awaitItem()
            awaitItem()

            val withEntries = awaitItem()
            assertThat(withEntries.watchlistEntries).containsKey(21)
            assertThat(withEntries.watchlistEntries[21]?.localId).isEqualTo(5L)
            assertThat(withEntries.watchlistEntries[21]?.status).isEqualTo(WatchStatus.WATCHING)
        }
    }

    @Test
    fun `watchlistEntries update reactively when database changes`() = runTest {
        val watchlistFlow = MutableStateFlow<List<Anime>>(emptyList())
        coEvery { searchAnimeUseCase("one punch") } returns Result.success(sampleResults)
        every { observeWatchlistAnimeByMalIdsUseCase(listOf(21)) } returns watchlistFlow

        viewModel.uiState.test {
            awaitItem()

            viewModel.updateQuery("one punch")
            awaitItem()

            viewModel.search()
            awaitItem()

            val loaded = awaitItem()
            assertThat(loaded.watchlistEntries).isEmpty()

            watchlistFlow.value = listOf(
                Anime(id = 5L, malId = 21, title = "One Punch Man", status = WatchStatus.WATCHING)
            )

            val updated = awaitItem()
            assertThat(updated.watchlistEntries).containsKey(21)
            assertThat(updated.watchlistEntries[21]?.status).isEqualTo(WatchStatus.WATCHING)

            watchlistFlow.value = emptyList()

            val removed = awaitItem()
            assertThat(removed.watchlistEntries).isEmpty()
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
    fun `onAddClick shows bottom sheet for status selection`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onAddClick(sampleAnime)

            val updated = awaitItem()
            assertThat(updated.selectedAnimeForAdd).isEqualTo(sampleAnime)
        }
    }

    @Test
    fun `onAnimeClick navigates by malId when not in watchlist`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onAnimeClick(sampleAnime)

            val navigating = awaitItem()
            assertThat(navigating.pendingNavigationMalId).isEqualTo(21)
            assertThat(navigating.pendingNavigationId).isNull()
        }
    }

    @Test
    fun `onAnimeClick navigates directly when already in watchlist`() = runTest {
        val watchlistFlow = MutableStateFlow(listOf(
            Anime(id = 10L, malId = 21, title = "One Punch Man", status = WatchStatus.WATCHING)
        ))
        coEvery { searchAnimeUseCase("one punch") } returns Result.success(sampleResults)
        every { observeWatchlistAnimeByMalIdsUseCase(listOf(21)) } returns watchlistFlow

        viewModel.uiState.test {
            awaitItem()

            viewModel.updateQuery("one punch")
            awaitItem()
            viewModel.search()
            awaitItem()
            awaitItem()
            val afterSearch = awaitItem()
            assertThat(afterSearch.watchlistEntries[21]?.localId).isEqualTo(10L)

            viewModel.onAnimeClick(sampleAnime)

            val navigating = awaitItem()
            assertThat(navigating.pendingNavigationId).isEqualTo(10L)
        }
    }

    @Test
    fun `onStatusSelected adds anime and stays on search screen`() = runTest {
        coEvery { addAnimeToWatchlistUseCase(any()) } returns 7L

        viewModel.uiState.test {
            awaitItem()

            viewModel.onAddClick(sampleAnime)
            awaitItem()

            viewModel.onStatusSelected(WatchStatus.WATCHING)

            awaitItem()

            val afterAdd = awaitItem()
            assertThat(afterAdd.pendingNavigationId).isNull()
            assertThat(afterAdd.snackbarMessage).isEqualTo("One Punch Man added to watchlist")

            coVerify {
                addAnimeToWatchlistUseCase(match { it.status == WatchStatus.WATCHING })
            }
        }
    }

    @Test
    fun `dismissBottomSheet clears selected anime`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onAddClick(sampleAnime)
            awaitItem()

            viewModel.dismissBottomSheet()

            val dismissed = awaitItem()
            assertThat(dismissed.selectedAnimeForAdd).isNull()
        }
    }

    @Test
    fun `clearSnackbar clears the snackbar message`() = runTest {
        coEvery { addAnimeToWatchlistUseCase(any()) } returns 1L

        viewModel.uiState.test {
            awaitItem()

            viewModel.onAddClick(sampleAnime)
            awaitItem()
            viewModel.onStatusSelected(WatchStatus.WATCHING)
            awaitItem()
            val withSnackbar = awaitItem()
            assertThat(withSnackbar.snackbarMessage).isNotNull()

            viewModel.clearSnackbar()

            val cleared = awaitItem()
            assertThat(cleared.snackbarMessage).isNull()
        }
    }

    @Test
    fun `onNavigated clears pending navigation ids`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onAnimeClick(sampleAnime)

            val withNav = awaitItem()
            assertThat(withNav.pendingNavigationMalId).isEqualTo(21)

            viewModel.onNavigated()

            val cleared = awaitItem()
            assertThat(cleared.pendingNavigationMalId).isNull()
            assertThat(cleared.pendingNavigationId).isNull()
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
    fun `selectFilter NOT_ADDED hides anime already in watchlist`() = runTest {
        val watchlistFlow = MutableStateFlow(listOf(
            Anime(id = 5L, malId = 21, title = "One Punch Man", status = WatchStatus.WATCHING)
        ))
        coEvery { searchAnimeUseCase("anime") } returns Result.success(multiResults)
        every { observeWatchlistAnimeByMalIdsUseCase(any()) } returns watchlistFlow

        viewModel.uiState.test {
            awaitItem()

            viewModel.updateQuery("anime")
            awaitItem()
            viewModel.search()
            awaitItem()
            awaitItem()
            awaitItem()

            viewModel.selectFilter(SearchFilter.NOT_ADDED)

            val filtered = awaitItem()
            assertThat(filtered.selectedFilter).isEqualTo(SearchFilter.NOT_ADDED)
            assertThat(filtered.displayedResults).hasSize(2)
            assertThat(filtered.displayedResults.none { it.malId == 21 }).isTrue()
        }
    }

    @Test
    fun `selectFilter ALREADY_ADDED shows only anime in watchlist`() = runTest {
        val watchlistFlow = MutableStateFlow(listOf(
            Anime(id = 5L, malId = 21, title = "One Punch Man", status = WatchStatus.WATCHING)
        ))
        coEvery { searchAnimeUseCase("anime") } returns Result.success(multiResults)
        every { observeWatchlistAnimeByMalIdsUseCase(any()) } returns watchlistFlow

        viewModel.uiState.test {
            awaitItem()

            viewModel.updateQuery("anime")
            awaitItem()
            viewModel.search()
            awaitItem()
            awaitItem()
            awaitItem()

            viewModel.selectFilter(SearchFilter.ALREADY_ADDED)

            val filtered = awaitItem()
            assertThat(filtered.displayedResults).hasSize(1)
            assertThat(filtered.displayedResults[0].malId).isEqualTo(21)
        }
    }

    @Test
    fun `selectSort with RECENTLY_ADDED sorts by watchlist addedAt descending`() = runTest {
        val watchlistFlow = MutableStateFlow(listOf(
            Anime(id = 5L, malId = 21, title = "One Punch Man", status = WatchStatus.WATCHING, addedAt = 1000L),
            Anime(id = 6L, malId = 30, title = "Attack on Titan", status = WatchStatus.WATCHING, addedAt = 3000L)
        ))
        coEvery { searchAnimeUseCase("anime") } returns Result.success(multiResults)
        every { observeWatchlistAnimeByMalIdsUseCase(any()) } returns watchlistFlow

        viewModel.uiState.test {
            awaitItem()

            viewModel.updateQuery("anime")
            awaitItem()
            viewModel.search()
            awaitItem()
            awaitItem()
            awaitItem()

            viewModel.selectSort(SearchSortOption.RECENTLY_ADDED)

            val sorted = awaitItem()
            assertThat(sorted.sortOption).isEqualTo(SearchSortOption.RECENTLY_ADDED)
            assertThat(sorted.displayedResults[0].title).isEqualTo("Attack on Titan")
            assertThat(sorted.displayedResults[1].title).isEqualTo("One Punch Man")
            assertThat(sorted.displayedResults[2].title).isEqualTo("Bleach")
        }
    }

    @Test
    fun `selectSort with RECENTLY_ADDED puts non-watchlist anime at bottom`() = runTest {
        val watchlistFlow = MutableStateFlow(listOf(
            Anime(id = 5L, malId = 21, title = "One Punch Man", status = WatchStatus.WATCHING, addedAt = 0L)
        ))
        coEvery { searchAnimeUseCase("anime") } returns Result.success(multiResults)
        every { observeWatchlistAnimeByMalIdsUseCase(any()) } returns watchlistFlow

        viewModel.uiState.test {
            awaitItem()

            viewModel.updateQuery("anime")
            awaitItem()
            viewModel.search()
            awaitItem()
            awaitItem()
            awaitItem()

            viewModel.selectSort(SearchSortOption.RECENTLY_ADDED)

            val sorted = awaitItem()
            assertThat(sorted.displayedResults[0].title).isEqualTo("One Punch Man")
            assertThat(sorted.displayedResults[1].malId).isNotEqualTo(21)
            assertThat(sorted.displayedResults[2].malId).isNotEqualTo(21)
        }
    }

    @Test
    fun `displayedResults recomputes when watchlist entries change`() = runTest {
        val watchlistFlow = MutableStateFlow<List<Anime>>(emptyList())
        coEvery { searchAnimeUseCase("anime") } returns Result.success(multiResults)
        every { observeWatchlistAnimeByMalIdsUseCase(any()) } returns watchlistFlow

        viewModel.uiState.test {
            awaitItem()

            viewModel.updateQuery("anime")
            awaitItem()
            viewModel.search()
            awaitItem()
            awaitItem()

            viewModel.selectFilter(SearchFilter.ALREADY_ADDED)
            val emptyFilter = awaitItem()
            assertThat(emptyFilter.displayedResults).isEmpty()

            watchlistFlow.value = listOf(
                Anime(id = 5L, malId = 21, title = "One Punch Man", status = WatchStatus.WATCHING)
            )

            val updated = awaitItem()
            assertThat(updated.displayedResults).hasSize(1)
            assertThat(updated.displayedResults[0].malId).isEqualTo(21)
        }
    }
}
