package com.vuzeda.animewatchlist.tracker.ui.screens.animedetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.ProgressiveResolveResult
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.SeasonData
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.DeleteAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.FindAnimeBySeasonMalIdUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveAnimeByIdUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveSeasonsForAnimeUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ResolveAnimeProgressivelyUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ToggleAnimeNotificationsUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.UpdateAnimeUseCase
import com.vuzeda.animewatchlist.tracker.ui.navigation.Route
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
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
class AnimeDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val observeAnimeByIdUseCase: ObserveAnimeByIdUseCase = mockk()
    private val observeSeasonsForAnimeUseCase: ObserveSeasonsForAnimeUseCase = mockk()
    private val updateAnimeUseCase: UpdateAnimeUseCase = mockk()
    private val deleteAnimeUseCase: DeleteAnimeUseCase = mockk()
    private val toggleAnimeNotificationsUseCase: ToggleAnimeNotificationsUseCase = mockk(relaxed = true)
    private val resolveAnimeProgressivelyUseCase: ResolveAnimeProgressivelyUseCase = mockk()
    private val addAnimeUseCase: AddAnimeUseCase = mockk()
    private val findAnimeBySeasonMalIdUseCase: FindAnimeBySeasonMalIdUseCase = mockk()

    private val sampleAnime = Anime(
        id = 1L,
        title = "Attack on Titan",
        status = WatchStatus.WATCHING,
        userRating = 9
    )

    private val sampleSeasons = listOf(
        Season(id = 1L, animeId = 1L, malId = 16498, title = "Season 1", orderIndex = 0),
        Season(id = 2L, animeId = 1L, malId = 25777, title = "Season 2", orderIndex = 1)
    )

    private lateinit var animeFlow: MutableStateFlow<Anime?>
    private lateinit var seasonsFlow: MutableStateFlow<List<Season>>

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        animeFlow = MutableStateFlow(sampleAnime)
        seasonsFlow = MutableStateFlow(sampleSeasons)
        every { observeAnimeByIdUseCase(1L) } returns animeFlow
        every { observeSeasonsForAnimeUseCase(1L) } returns seasonsFlow
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(animeId: Long = 1L, malId: Int = 0): AnimeDetailViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                Route.AnimeDetail.ARG_ANIME_ID to animeId,
                Route.AnimeDetail.ARG_MAL_ID to malId
            )
        )
        return AnimeDetailViewModel(
            savedStateHandle = savedStateHandle,
            observeAnimeByIdUseCase = observeAnimeByIdUseCase,
            observeSeasonsForAnimeUseCase = observeSeasonsForAnimeUseCase,
            updateAnimeUseCase = updateAnimeUseCase,
            deleteAnimeUseCase = deleteAnimeUseCase,
            toggleAnimeNotificationsUseCase = toggleAnimeNotificationsUseCase,
            resolveAnimeProgressivelyUseCase = resolveAnimeProgressivelyUseCase,
            addAnimeUseCase = addAnimeUseCase,
            findAnimeBySeasonMalIdUseCase = findAnimeBySeasonMalIdUseCase
        )
    }

    @Test
    fun `loads anime and seasons on init in watchlist mode`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(AnimeDetailUiState.Loading::class.java)

            val success = awaitItem() as AnimeDetailUiState.Success
            assertThat(success.anime.title).isEqualTo("Attack on Titan")
            assertThat(success.seasons).hasSize(2)
            assertThat(success.isInWatchlist).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shows not found when anime does not exist`() = runTest {
        val emptyAnimeFlow = MutableStateFlow<Anime?>(null)
        val emptySeasonsFlow = MutableStateFlow<List<Season>>(emptyList())
        every { observeAnimeByIdUseCase(999L) } returns emptyAnimeFlow
        every { observeSeasonsForAnimeUseCase(999L) } returns emptySeasonsFlow

        val viewModel = createViewModel(animeId = 999L)

        viewModel.uiState.test {
            awaitItem()

            val notFound = awaitItem()
            assertThat(notFound).isInstanceOf(AnimeDetailUiState.NotFound::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shows not found when both animeId and malId are zero`() = runTest {
        val viewModel = createViewModel(animeId = 0L, malId = 0)

        viewModel.uiState.test {
            val notFound = awaitItem()
            assertThat(notFound).isInstanceOf(AnimeDetailUiState.NotFound::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resolves anime from API progressively when malId is provided`() = runTest {
        coEvery { findAnimeBySeasonMalIdUseCase(50) } returns null
        every { resolveAnimeProgressivelyUseCase(50) } returns flow {
            emit(
                ProgressiveResolveResult(
                    title = "Spy x Family",
                    imageUrl = "https://example.com/spy.jpg",
                    synopsis = "A spy forms a pretend family.",
                    genres = listOf("Action", "Comedy"),
                    seasons = listOf(
                        SeasonData(malId = 50, title = "Season 1", type = "TV", episodeCount = 12, score = 8.5)
                    ),
                    isResolvingPrequels = false,
                    isResolvingSequels = true
                )
            )
            emit(
                ProgressiveResolveResult(
                    title = "Spy x Family",
                    imageUrl = "https://example.com/spy.jpg",
                    synopsis = "A spy forms a pretend family.",
                    genres = listOf("Action", "Comedy"),
                    seasons = listOf(
                        SeasonData(malId = 50, title = "Season 1", type = "TV", episodeCount = 12, score = 8.5),
                        SeasonData(malId = 51, title = "Season 2", type = "TV", episodeCount = 12, score = 8.3)
                    ),
                    isResolvingPrequels = false,
                    isResolvingSequels = false
                )
            )
        }

        val viewModel = createViewModel(animeId = 0L, malId = 50)

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(AnimeDetailUiState.Loading::class.java)

            val partial = awaitItem() as AnimeDetailUiState.Success
            assertThat(partial.anime.title).isEqualTo("Spy x Family")
            assertThat(partial.seasons).hasSize(1)
            assertThat(partial.isResolvingSequels).isTrue()
            assertThat(partial.isInWatchlist).isFalse()

            val complete = awaitItem() as AnimeDetailUiState.Success
            assertThat(complete.seasons).hasSize(2)
            assertThat(complete.isResolvingSequels).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `redirects to watchlist mode when anime already exists`() = runTest {
        coEvery { findAnimeBySeasonMalIdUseCase(16498) } returns 1L

        val viewModel = createViewModel(animeId = 0L, malId = 16498)

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(AnimeDetailUiState.Loading::class.java)

            val success = awaitItem() as AnimeDetailUiState.Success
            assertThat(success.anime.title).isEqualTo("Attack on Titan")
            assertThat(success.isInWatchlist).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shows not found when API resolve fails`() = runTest {
        coEvery { findAnimeBySeasonMalIdUseCase(999) } returns null
        every { resolveAnimeProgressivelyUseCase(999) } returns flow {
            throw Exception("Not found")
        }

        val viewModel = createViewModel(animeId = 0L, malId = 999)

        viewModel.uiState.test {
            awaitItem()

            val notFound = awaitItem()
            assertThat(notFound).isInstanceOf(AnimeDetailUiState.NotFound::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateStatus persists via use case`() = runTest {
        coEvery { updateAnimeUseCase(any()) } coAnswers {
            animeFlow.value = firstArg()
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.updateStatus(WatchStatus.COMPLETED)

            val updated = awaitItem() as AnimeDetailUiState.Success
            assertThat(updated.anime.status).isEqualTo(WatchStatus.COMPLETED)

            coVerify { updateAnimeUseCase(match { it.status == WatchStatus.COMPLETED }) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateUserRating persists via use case`() = runTest {
        coEvery { updateAnimeUseCase(any()) } coAnswers {
            animeFlow.value = firstArg()
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.updateUserRating(10)

            val updated = awaitItem() as AnimeDetailUiState.Success
            assertThat(updated.anime.userRating).isEqualTo(10)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteAnime calls use case and invokes callback`() = runTest {
        coEvery { deleteAnimeUseCase(1L) } returns Unit

        val viewModel = createViewModel()
        var callbackInvoked = false

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.deleteAnime { callbackInvoked = true }
            testDispatcher.scheduler.advanceUntilIdle()

            assertThat(callbackInvoked).isTrue()
            coVerify { deleteAnimeUseCase(1L) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleNotifications delegates to use case`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.toggleNotifications()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { toggleAnimeNotificationsUseCase(id = 1L, enabled = true) }
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
            val shown = awaitItem() as AnimeDetailUiState.Success
            assertThat(shown.isStatusSheetVisible).isTrue()

            viewModel.dismissStatusSheet()
            val hidden = awaitItem() as AnimeDetailUiState.Success
            assertThat(hidden.isStatusSheetVisible).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addToWatchlist flow transitions to watchlist mode`() = runTest {
        coEvery { findAnimeBySeasonMalIdUseCase(50) } returns null
        every { resolveAnimeProgressivelyUseCase(50) } returns flow {
            emit(
                ProgressiveResolveResult(
                    title = "Spy x Family",
                    seasons = listOf(
                        SeasonData(malId = 50, title = "Season 1", type = "TV")
                    ),
                    isResolvingPrequels = false,
                    isResolvingSequels = false
                )
            )
        }
        coEvery { addAnimeUseCase(any(), any(), any()) } returns 10L

        val addedAnimeFlow = MutableStateFlow<Anime?>(
            Anime(id = 10L, title = "Spy x Family", status = WatchStatus.PLAN_TO_WATCH)
        )
        val addedSeasonsFlow = MutableStateFlow(
            listOf(Season(id = 5L, animeId = 10L, malId = 50, title = "Season 1"))
        )
        every { observeAnimeByIdUseCase(10L) } returns addedAnimeFlow
        every { observeSeasonsForAnimeUseCase(10L) } returns addedSeasonsFlow

        val viewModel = createViewModel(animeId = 0L, malId = 50)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val resolved = expectMostRecentItem() as AnimeDetailUiState.Success
            assertThat(resolved.isInWatchlist).isFalse()

            viewModel.addToWatchlist(WatchStatus.PLAN_TO_WATCH)
            testDispatcher.scheduler.advanceUntilIdle()

            val watchlisted = expectMostRecentItem() as AnimeDetailUiState.Success
            assertThat(watchlisted.isInWatchlist).isTrue()
            assertThat(watchlisted.anime.id).isEqualTo(10L)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
