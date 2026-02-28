package com.vuzeda.animewatchlist.tracker.ui.screens.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddAnimeToWatchlistUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.GetWatchlistAnimeByMalIdsUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.SearchAnimeUseCase
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
    private val addAnimeToWatchlistUseCase: AddAnimeToWatchlistUseCase = mockk()
    private val getWatchlistAnimeByMalIdsUseCase: GetWatchlistAnimeByMalIdsUseCase = mockk()

    private lateinit var viewModel: SearchViewModel

    private val sampleAnime = Anime(malId = 21, title = "One Punch Man", status = WatchStatus.PLAN_TO_WATCH)

    private val sampleResults = listOf(sampleAnime)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { getWatchlistAnimeByMalIdsUseCase(any()) } returns emptyList()
        viewModel = SearchViewModel(searchAnimeUseCase, addAnimeToWatchlistUseCase, getWatchlistAnimeByMalIdsUseCase)
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
        coEvery { getWatchlistAnimeByMalIdsUseCase(listOf(21)) } returns watchlistAnime

        viewModel.uiState.test {
            awaitItem()

            viewModel.updateQuery("one punch")
            awaitItem()

            viewModel.search()
            awaitItem()

            val loaded = awaitItem()
            assertThat(loaded.watchlistEntries).containsKey(21)
            assertThat(loaded.watchlistEntries[21]?.localId).isEqualTo(5L)
            assertThat(loaded.watchlistEntries[21]?.status).isEqualTo(WatchStatus.WATCHING)
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
    fun `onAddClick shows bottom sheet without navigate flag`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onAddClick(sampleAnime)

            val updated = awaitItem()
            assertThat(updated.selectedAnimeForAdd).isEqualTo(sampleAnime)
            assertThat(updated.isNavigateAfterAdd).isFalse()
        }
    }

    @Test
    fun `onAnimeClick shows bottom sheet with navigate flag when not in watchlist`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onAnimeClick(sampleAnime)

            val updated = awaitItem()
            assertThat(updated.selectedAnimeForAdd).isEqualTo(sampleAnime)
            assertThat(updated.isNavigateAfterAdd).isTrue()
        }
    }

    @Test
    fun `onAnimeClick navigates directly when already in watchlist`() = runTest {
        coEvery { addAnimeToWatchlistUseCase(any()) } returns 10L

        viewModel.uiState.test {
            awaitItem()

            viewModel.onAddClick(sampleAnime)
            awaitItem()
            viewModel.onStatusSelected(WatchStatus.WATCHING)
            awaitItem()
            val afterAdd = awaitItem()
            assertThat(afterAdd.watchlistEntries[21]?.localId).isEqualTo(10L)

            viewModel.onAnimeClick(sampleAnime)

            val navigating = awaitItem()
            assertThat(navigating.pendingNavigationId).isEqualTo(10L)
        }
    }

    @Test
    fun `onStatusSelected adds anime and updates watchlistEntries with status`() = runTest {
        coEvery { addAnimeToWatchlistUseCase(any()) } returns 5L

        viewModel.uiState.test {
            awaitItem()

            viewModel.onAddClick(sampleAnime)
            awaitItem()

            viewModel.onStatusSelected(WatchStatus.WATCHING)

            val dismissed = awaitItem()
            assertThat(dismissed.selectedAnimeForAdd).isNull()

            val afterAdd = awaitItem()
            assertThat(afterAdd.watchlistEntries[21]?.localId).isEqualTo(5L)
            assertThat(afterAdd.watchlistEntries[21]?.status).isEqualTo(WatchStatus.WATCHING)
            assertThat(afterAdd.snackbarMessage).isEqualTo("One Punch Man added to watchlist")
            assertThat(afterAdd.pendingNavigationId).isNull()

            coVerify {
                addAnimeToWatchlistUseCase(match { it.status == WatchStatus.WATCHING })
            }
        }
    }

    @Test
    fun `onStatusSelected navigates when isNavigateAfterAdd is true`() = runTest {
        coEvery { addAnimeToWatchlistUseCase(any()) } returns 7L

        viewModel.uiState.test {
            awaitItem()

            viewModel.onAnimeClick(sampleAnime)
            awaitItem()

            viewModel.onStatusSelected(WatchStatus.PLAN_TO_WATCH)

            awaitItem()

            val afterAdd = awaitItem()
            assertThat(afterAdd.pendingNavigationId).isEqualTo(7L)
            assertThat(afterAdd.snackbarMessage).isEqualTo("One Punch Man added to watchlist")
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
            assertThat(dismissed.isNavigateAfterAdd).isFalse()
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
    fun `onNavigated clears pending navigation id`() = runTest {
        coEvery { addAnimeToWatchlistUseCase(any()) } returns 7L

        viewModel.uiState.test {
            awaitItem()

            viewModel.onAnimeClick(sampleAnime)
            awaitItem()
            viewModel.onStatusSelected(WatchStatus.WATCHING)
            awaitItem()
            val withNav = awaitItem()
            assertThat(withNav.pendingNavigationId).isEqualTo(7L)

            viewModel.onNavigated()

            val cleared = awaitItem()
            assertThat(cleared.pendingNavigationId).isNull()
        }
    }
}
