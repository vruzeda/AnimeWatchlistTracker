package com.vuzeda.animewatchlist.tracker.ui.screens.seasons

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeSeason
import com.vuzeda.animewatchlist.tracker.domain.model.ResolvedSeries
import com.vuzeda.animewatchlist.tracker.domain.model.SearchResult
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.SeasonData
import com.vuzeda.animewatchlist.tracker.domain.model.SeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddSeasonsToAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.FetchSeasonDetailUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.FindAnimeBySeasonMalIdUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.GetSeasonAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.GetSeasonsForAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ResolveAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.UpdateAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.UpdateSeasonUseCase
import io.mockk.coEvery
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
class SeasonsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getSeasonAnimeUseCase: GetSeasonAnimeUseCase = mockk()
    private val fetchSeasonDetailUseCase: FetchSeasonDetailUseCase = mockk()
    private val resolveAnimeUseCase: ResolveAnimeUseCase = mockk()
    private val addAnimeUseCase: AddAnimeUseCase = mockk()
    private val updateAnimeUseCase: UpdateAnimeUseCase = mockk(relaxed = true)
    private val updateSeasonUseCase: UpdateSeasonUseCase = mockk(relaxed = true)
    private val getSeasonsForAnimeUseCase: GetSeasonsForAnimeUseCase = mockk()
    private val addSeasonsToAnimeUseCase: AddSeasonsToAnimeUseCase = mockk(relaxed = true)
    private val findAnimeBySeasonMalIdUseCase: FindAnimeBySeasonMalIdUseCase = mockk()

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
        coEvery { findAnimeBySeasonMalIdUseCase(any()) } returns null
        coEvery { getSeasonAnimeUseCase(any(), any(), any()) } returns Result.success(samplePage)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SeasonsViewModel(
        getSeasonAnimeUseCase = getSeasonAnimeUseCase,
        fetchSeasonDetailUseCase = fetchSeasonDetailUseCase,
        resolveAnimeUseCase = resolveAnimeUseCase,
        addAnimeUseCase = addAnimeUseCase,
        updateAnimeUseCase = updateAnimeUseCase,
        updateSeasonUseCase = updateSeasonUseCase,
        getSeasonsForAnimeUseCase = getSeasonsForAnimeUseCase,
        addSeasonsToAnimeUseCase = addSeasonsToAnimeUseCase,
        findAnimeBySeasonMalIdUseCase = findAnimeBySeasonMalIdUseCase
    )

    @Test
    fun `initial load fetches current season anime`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(1)

            val loading = awaitItem()
            assertThat(loading.isLoading).isTrue()

            val loaded = awaitItem()
            assertThat(loaded.isLoading).isFalse()
            assertThat(loaded.animeList).hasSize(2)
            assertThat(loaded.displayedAnimeList).hasSize(2)
            assertThat(loaded.displayedAnimeList[0].title).isEqualTo("Frieren")
            assertThat(loaded.hasNextPage).isTrue()
        }
    }

    @Test
    fun `selectNextSeason clears list and loads new season`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(3)

            viewModel.selectNextSeason()

            val cleared = awaitItem()
            assertThat(cleared.animeList).isEmpty()

            val loading = awaitItem()
            assertThat(loading.isLoading).isTrue()

            val loaded = awaitItem()
            assertThat(loaded.isLoading).isFalse()
            assertThat(loaded.animeList).hasSize(2)
        }
    }

    @Test
    fun `selectPreviousSeason clears list and loads new season`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(3)

            viewModel.selectPreviousSeason()

            val cleared = awaitItem()
            assertThat(cleared.animeList).isEmpty()

            val loading = awaitItem()
            assertThat(loading.isLoading).isTrue()

            val loaded = awaitItem()
            assertThat(loaded.isLoading).isFalse()
            assertThat(loaded.animeList).hasSize(2)
        }
    }

    @Test
    fun `loadMore appends results to existing list`() = runTest {
        coEvery {
            getSeasonAnimeUseCase(any(), any(), page = 2)
        } returns Result.success(samplePageTwo)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(3)

            viewModel.loadMore()

            val loadingMore = awaitItem()
            assertThat(loadingMore.isLoadingMore).isTrue()

            val loaded = awaitItem()
            assertThat(loaded.isLoadingMore).isFalse()
            assertThat(loaded.animeList).hasSize(3)
            assertThat(loaded.hasNextPage).isFalse()
            assertThat(loaded.currentPage).isEqualTo(2)
        }
    }

    @Test
    fun `loadMore does nothing when no next page`() = runTest {
        coEvery {
            getSeasonAnimeUseCase(any(), any(), any())
        } returns Result.success(samplePage.copy(hasNextPage = false))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(3)

            viewModel.loadMore()

            expectNoEvents()
        }
    }

    @Test
    fun `error state is set on fetch failure`() = runTest {
        coEvery {
            getSeasonAnimeUseCase(any(), any(), any())
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
            skipItems(3)

            viewModel.onResultClick(samplePage.results[0])

            val navigating = awaitItem()
            assertThat(navigating.pendingNavigationMalId).isEqualTo(1)
        }
    }

    @Test
    fun `onNavigated clears pending navigation`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(3)

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
            skipItems(3)

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
        coEvery { addAnimeUseCase(any(), any(), any()) } returns 10L
        coEvery { resolveAnimeUseCase(any()) } returns Result.success(
            ResolvedSeries(
                title = "Frieren",
                seasons = listOf(SeasonData(malId = 1, title = "Season 1", type = "TV"))
            )
        )
        coEvery { getSeasonsForAnimeUseCase(any()) } returns listOf(
            Season(id = 1L, animeId = 10L, malId = 1, title = "Season 1", orderIndex = 0)
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(3)

            viewModel.onAddClick(samplePage.results[0])
            awaitItem()
            awaitItem()

            viewModel.addToWatchlist(WatchStatus.WATCHING)

            val added = awaitItem()
            assertThat(added.selectedResultForAdd).isNull()
            assertThat(added.addedMalIds).contains(1)
            assertThat(added.snackbarMessage).isEqualTo("Frieren")
        }
    }

    @Test
    fun `dismissBottomSheet clears selection`() = runTest {
        coEvery { fetchSeasonDetailUseCase(1) } returns Result.success(sampleDetails)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(3)

            viewModel.onAddClick(samplePage.results[0])
            awaitItem()
            awaitItem()

            viewModel.dismissBottomSheet()

            val dismissed = awaitItem()
            assertThat(dismissed.selectedResultForAdd).isNull()
        }
    }

    @Test
    fun `selectSort by alphabetical sorts displayedAnimeList`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(3)

            viewModel.selectSort(SeasonsSortOption.ALPHABETICAL)

            val sorted = awaitItem()
            assertThat(sorted.sortOption).isEqualTo(SeasonsSortOption.ALPHABETICAL)
            assertThat(sorted.isSortAscending).isTrue()
            assertThat(sorted.displayedAnimeList[0].title).isEqualTo("Frieren")
            assertThat(sorted.displayedAnimeList[1].title).isEqualTo("Jujutsu Kaisen")
        }
    }

    @Test
    fun `selectSort by score sorts displayedAnimeList descending`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(3)

            viewModel.selectSort(SeasonsSortOption.SCORE)

            val sorted = awaitItem()
            assertThat(sorted.sortOption).isEqualTo(SeasonsSortOption.SCORE)
            assertThat(sorted.isSortAscending).isFalse()
            assertThat(sorted.displayedAnimeList[0].title).isEqualTo("Frieren")
            assertThat(sorted.displayedAnimeList[1].title).isEqualTo("Jujutsu Kaisen")
        }
    }

    @Test
    fun `selectSort toggles ascending when same option selected twice`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(3)

            viewModel.selectSort(SeasonsSortOption.ALPHABETICAL)
            val first = awaitItem()
            assertThat(first.isSortAscending).isTrue()

            viewModel.selectSort(SeasonsSortOption.ALPHABETICAL)
            val toggled = awaitItem()
            assertThat(toggled.isSortAscending).isFalse()
            assertThat(toggled.displayedAnimeList[0].title).isEqualTo("Jujutsu Kaisen")
            assertThat(toggled.displayedAnimeList[1].title).isEqualTo("Frieren")
        }
    }

    @Test
    fun `selectNextSeason resets sort to default`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(3)

            viewModel.selectSort(SeasonsSortOption.SCORE)
            awaitItem()

            viewModel.selectNextSeason()

            val cleared = awaitItem()
            assertThat(cleared.sortOption).isEqualTo(SeasonsSortOption.DEFAULT)
            assertThat(cleared.isSortAscending).isTrue()
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
