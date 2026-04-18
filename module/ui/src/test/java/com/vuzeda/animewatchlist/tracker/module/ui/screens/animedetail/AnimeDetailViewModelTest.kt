package com.vuzeda.animewatchlist.tracker.module.ui.screens.animedetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.ResolvedSeries
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonData
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsTracker
import com.vuzeda.animewatchlist.tracker.module.usecase.AddAnimeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.AddSeasonToWatchlistUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.DeleteAnimeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FindAnimeBySeasonMalIdUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveAnimeByIdUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveSeasonsForAnimeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveIsNotificationDebugInfoEnabledUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.RefreshAnimeSeasonsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.RefreshSeasonDataUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ResolveAnimeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ToggleAnimeNotificationsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.UpdateAnimeUseCase
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
class AnimeDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val observeAnimeByIdUseCase: ObserveAnimeByIdUseCase = mockk()
    private val observeSeasonsForAnimeUseCase: ObserveSeasonsForAnimeUseCase = mockk()
    private val updateAnimeUseCase: UpdateAnimeUseCase = mockk()
    private val updateSeasonStatusUseCase: UpdateSeasonStatusUseCase = mockk()
    private val deleteAnimeUseCase: DeleteAnimeUseCase = mockk()
    private val toggleAnimeNotificationsUseCase: ToggleAnimeNotificationsUseCase = mockk(relaxed = true)
    private val resolveAnimeUseCase: ResolveAnimeUseCase = mockk()
    private val addAnimeUseCase: AddAnimeUseCase = mockk()
    private val addSeasonToWatchlistUseCase: AddSeasonToWatchlistUseCase = mockk(relaxed = true)
    private val findAnimeBySeasonMalIdUseCase: FindAnimeBySeasonMalIdUseCase = mockk()
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase = mockk()
    private val refreshAnimeSeasonsUseCase: RefreshAnimeSeasonsUseCase = mockk(relaxed = true)
    private val refreshSeasonDataUseCase: RefreshSeasonDataUseCase = mockk(relaxed = true)
    private val observeIsNotificationDebugInfoEnabledUseCase: ObserveIsNotificationDebugInfoEnabledUseCase = mockk()
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)

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
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.DEFAULT)
        every { observeIsNotificationDebugInfoEnabledUseCase() } returns flowOf(false)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(animeId: Long = 1L, malId: Int = 0): AnimeDetailViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "animeId" to animeId,
                "malId" to malId
            )
        )
        return AnimeDetailViewModel(
            savedStateHandle = savedStateHandle,
            observeAnimeByIdUseCase = observeAnimeByIdUseCase,
            observeSeasonsForAnimeUseCase = observeSeasonsForAnimeUseCase,
            updateAnimeUseCase = updateAnimeUseCase,
            updateSeasonStatusUseCase = updateSeasonStatusUseCase,
            deleteAnimeUseCase = deleteAnimeUseCase,
            toggleAnimeNotificationsUseCase = toggleAnimeNotificationsUseCase,
            resolveAnimeUseCase = resolveAnimeUseCase,
            addAnimeUseCase = addAnimeUseCase,
            addSeasonToWatchlistUseCase = addSeasonToWatchlistUseCase,
            findAnimeBySeasonMalIdUseCase = findAnimeBySeasonMalIdUseCase,
            observeTitleLanguageUseCase = observeTitleLanguageUseCase,
            refreshAnimeSeasonsUseCase = refreshAnimeSeasonsUseCase,
            refreshSeasonDataUseCase = refreshSeasonDataUseCase,
            observeIsNotificationDebugInfoEnabledUseCase = observeIsNotificationDebugInfoEnabledUseCase,
            analyticsTracker = analyticsTracker
        )
    }

    @Test
    fun `loads anime and seasons on init in watchlist mode`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading.isLoading).isTrue()

            val success = awaitItem()
            assertThat(success.anime?.title).isEqualTo("Attack on Titan")
            assertThat(success.seasons).hasSize(2)
            assertThat(success.isInWatchlist).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `triggers background refresh for each season on load from DB`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            coVerify { refreshSeasonDataUseCase(sampleSeasons[0]) }
            coVerify { refreshSeasonDataUseCase(sampleSeasons[1]) }
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
            assertThat(notFound.isNotFound).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shows not found when both animeId and malId are zero`() = runTest {
        val viewModel = createViewModel(animeId = 0L, malId = 0)

        viewModel.uiState.test {
            val notFound = awaitItem()
            assertThat(notFound.isNotFound).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resolves anime from API when malId is provided`() = runTest {
        coEvery { findAnimeBySeasonMalIdUseCase(50) } returns null
        coEvery { resolveAnimeUseCase(50) } returns Result.success(
            ResolvedSeries(
                title = "Spy x Family",
                imageUrl = "https://example.com/spy.jpg",
                synopsis = "A spy forms a pretend family.",
                genres = listOf("Action", "Comedy"),
                seasons = listOf(
                    SeasonData(malId = 50, title = "Season 1", type = "TV", episodeCount = 12, score = 8.5),
                    SeasonData(malId = 51, title = "Season 2", type = "TV", episodeCount = 12, score = 8.3)
                )
            )
        )

        val viewModel = createViewModel(animeId = 0L, malId = 50)

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading.isLoading).isTrue()

            val success = awaitItem()
            assertThat(success.anime?.title).isEqualTo("Spy x Family")
            assertThat(success.anime?.synopsis).isEqualTo("A spy forms a pretend family.")
            assertThat(success.seasons).hasSize(2)
            assertThat(success.isInWatchlist).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `seasons resolved from API have isInWatchlist false`() = runTest {
        coEvery { findAnimeBySeasonMalIdUseCase(50) } returns null
        coEvery { resolveAnimeUseCase(50) } returns Result.success(
            ResolvedSeries(
                title = "Spy x Family",
                seasons = listOf(
                    SeasonData(malId = 50, title = "Season 1", type = "TV"),
                    SeasonData(malId = 51, title = "Season 2", type = "TV")
                )
            )
        )

        val viewModel = createViewModel(animeId = 0L, malId = 50)

        viewModel.uiState.test {
            awaitItem()

            val success = awaitItem()
            assertThat(success.seasons).hasSize(2)
            assertThat(success.seasons.all { !it.isInWatchlist }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `redirects to watchlist mode when anime already exists`() = runTest {
        coEvery { findAnimeBySeasonMalIdUseCase(16498) } returns 1L

        val viewModel = createViewModel(animeId = 0L, malId = 16498)

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading.isLoading).isTrue()

            val success = awaitItem()
            assertThat(success.anime?.title).isEqualTo("Attack on Titan")
            assertThat(success.isInWatchlist).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shows not found when API resolve fails`() = runTest {
        coEvery { findAnimeBySeasonMalIdUseCase(999) } returns null
        coEvery { resolveAnimeUseCase(999) } returns Result.failure(Exception("Not found"))

        val viewModel = createViewModel(animeId = 0L, malId = 999)

        viewModel.uiState.test {
            awaitItem()

            val notFound = awaitItem()
            assertThat(notFound.isNotFound).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateStatus updates most recent season status via use case`() = runTest {
        coEvery { updateSeasonStatusUseCase(any(), any()) } coAnswers {
            animeFlow.value = animeFlow.value!!.copy(status = secondArg())
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.updateStatus(WatchStatus.COMPLETED)
            testDispatcher.scheduler.advanceUntilIdle()

            val updated = expectMostRecentItem()
            assertThat(updated.anime?.status).isEqualTo(WatchStatus.COMPLETED)

            coVerify { updateSeasonStatusUseCase(match { it.id == 2L }, WatchStatus.COMPLETED) }
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

            val updated = awaitItem()
            assertThat(updated.anime?.userRating).isEqualTo(10)
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
            val shown = awaitItem()
            assertThat(shown.isDeleteConfirmationVisible).isTrue()

            viewModel.dismissDeleteConfirmation()
            val hidden = awaitItem()
            assertThat(hidden.isDeleteConfirmationVisible).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `confirmDelete calls use case and marks anime as not in watchlist`() = runTest {
        coEvery { deleteAnimeUseCase(1L) } returns Unit

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.confirmDelete()
            testDispatcher.scheduler.advanceUntilIdle()

            val deleted = expectMostRecentItem()
            assertThat(deleted.isInWatchlist).isFalse()
            assertThat(deleted.isDeleteConfirmationVisible).isFalse()
            coVerify { deleteAnimeUseCase(1L) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onNotificationIconClick when disabled shows notification type sheet`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.onNotificationIconClick()

            val shown = awaitItem()
            assertThat(shown.isNotificationTypeSheetVisible).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectNotificationType sets type via use case`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.onNotificationIconClick()
            awaitItem()

            viewModel.selectNotificationType(NotificationType.NEW_EPISODES)
            testDispatcher.scheduler.advanceUntilIdle()

            val dismissed = expectMostRecentItem()
            assertThat(dismissed.isNotificationTypeSheetVisible).isFalse()

            coVerify {
                toggleAnimeNotificationsUseCase(
                    id = 1L,
                    notificationType = NotificationType.NEW_EPISODES
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onNotificationIconClick when enabled disables notifications`() = runTest {
        animeFlow.value = sampleAnime.copy(notificationType = NotificationType.BOTH)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.onNotificationIconClick()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify {
                toggleAnimeNotificationsUseCase(
                    id = 1L,
                    notificationType = NotificationType.NONE
                )
            }
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
            val shown = awaitItem()
            assertThat(shown.isStatusSheetVisible).isTrue()

            viewModel.dismissStatusSheet()
            val hidden = awaitItem()
            assertThat(hidden.isStatusSheetVisible).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `add scope flow transitions to watchlist mode with all seasons`() = runTest {
        coEvery { findAnimeBySeasonMalIdUseCase(50) } returns null
        coEvery { resolveAnimeUseCase(50) } returns Result.success(
            ResolvedSeries(
                title = "Spy x Family",
                seasons = listOf(
                    SeasonData(malId = 50, title = "Season 1", type = "TV"),
                    SeasonData(malId = 51, title = "Season 2", type = "TV")
                )
            )
        )
        coEvery { addAnimeUseCase(any(), any(), any()) } returns 10L

        val addedAnimeFlow = MutableStateFlow<Anime?>(
            Anime(id = 10L, title = "Spy x Family", status = WatchStatus.PLAN_TO_WATCH)
        )
        val addedSeasonsFlow = MutableStateFlow(
            listOf(
                Season(id = 5L, animeId = 10L, malId = 50, title = "Season 1"),
                Season(id = 6L, animeId = 10L, malId = 51, title = "Season 2")
            )
        )
        every { observeAnimeByIdUseCase(10L) } returns addedAnimeFlow
        every { observeSeasonsForAnimeUseCase(10L) } returns addedSeasonsFlow

        val viewModel = createViewModel(animeId = 0L, malId = 50)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val resolved = expectMostRecentItem()
            assertThat(resolved.isInWatchlist).isFalse()

            viewModel.showAddScopeSheet(WatchStatus.PLAN_TO_WATCH)
            val scopeSheet = awaitItem()
            assertThat(scopeSheet.isAddScopeSheetVisible).isTrue()
            assertThat(scopeSheet.pendingAddStatus).isEqualTo(WatchStatus.PLAN_TO_WATCH)

            viewModel.confirmAddScope(allSeasons = true)
            testDispatcher.scheduler.advanceUntilIdle()

            val watchlisted = expectMostRecentItem()
            assertThat(watchlisted.isInWatchlist).isTrue()
            assertThat(watchlisted.anime?.id).isEqualTo(10L)

            coVerify { addAnimeUseCase(any(), match { it.size == 2 }, WatchStatus.PLAN_TO_WATCH) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `confirmAddSeason calls AddSeasonToWatchlistUseCase for season already in DB`() = runTest {
        val dbSeason = Season(id = 1L, animeId = 1L, malId = 16498, title = "Season 1", isInWatchlist = false)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.showAddSeasonSheet(dbSeason)
            val withSheet = awaitItem()
            assertThat(withSheet.isAddSeasonSheetVisible).isTrue()
            assertThat(withSheet.pendingAddSeason).isEqualTo(dbSeason)

            viewModel.confirmAddSeason(WatchStatus.WATCHING)
            testDispatcher.scheduler.advanceUntilIdle()

            val afterAdd = expectMostRecentItem()
            assertThat(afterAdd.isAddSeasonSheetVisible).isFalse()
            assertThat(afterAdd.pendingAddSeason).isNull()

            coVerify { addSeasonToWatchlistUseCase(dbSeason, WatchStatus.WATCHING) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `confirmAddSeason uses AddAnimeUseCase for transient season and switches to watchlist mode`() = runTest {
        coEvery { findAnimeBySeasonMalIdUseCase(50) } returns null
        coEvery { resolveAnimeUseCase(50) } returns Result.success(
            ResolvedSeries(
                title = "Spy x Family",
                seasons = listOf(
                    SeasonData(malId = 50, title = "Season 1", type = "TV"),
                    SeasonData(malId = 51, title = "Season 2", type = "TV")
                )
            )
        )
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
            val resolved = expectMostRecentItem()
            assertThat(resolved.isInWatchlist).isFalse()

            val transientSeason = resolved.seasons[0]
            viewModel.showAddSeasonSheet(transientSeason)
            awaitItem()

            viewModel.confirmAddSeason(WatchStatus.PLAN_TO_WATCH)
            testDispatcher.scheduler.advanceUntilIdle()

            val watchlisted = expectMostRecentItem()
            assertThat(watchlisted.isInWatchlist).isTrue()
            assertThat(watchlisted.anime?.id).isEqualTo(10L)

            coVerify { addAnimeUseCase(any(), match { it.size == 1 && it[0].malId == 50 }, WatchStatus.PLAN_TO_WATCH) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismissAddSeasonSheet clears sheet state`() = runTest {
        val season = Season(id = 1L, animeId = 1L, malId = 100, title = "S1", isInWatchlist = false)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.showAddSeasonSheet(season)
            awaitItem()

            viewModel.dismissAddSeasonSheet()
            val hidden = awaitItem()
            assertThat(hidden.isAddSeasonSheetVisible).isFalse()
            assertThat(hidden.pendingAddSeason).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh calls season sync use cases for local anime and clears isRefreshing`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.refresh()
            testDispatcher.scheduler.advanceUntilIdle()

            val refreshed = expectMostRecentItem()
            assertThat(refreshed.isRefreshing).isFalse()
            coVerify { refreshAnimeSeasonsUseCase(1L) }
            coVerify { refreshSeasonDataUseCase(sampleSeasons[0]) }
            coVerify { refreshSeasonDataUseCase(sampleSeasons[1]) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh re-fetches from API for remote anime and updates state`() = runTest {
        coEvery { findAnimeBySeasonMalIdUseCase(50) } returns null
        coEvery { resolveAnimeUseCase(50) } returns Result.success(
            ResolvedSeries(
                title = "Spy x Family",
                seasons = listOf(
                    SeasonData(malId = 50, title = "Season 1", type = "TV")
                )
            )
        )

        val viewModel = createViewModel(animeId = 0L, malId = 50)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertThat(initial.isInWatchlist).isFalse()

            viewModel.refresh()
            testDispatcher.scheduler.advanceUntilIdle()

            val refreshed = expectMostRecentItem()
            assertThat(refreshed.isRefreshing).isFalse()
            assertThat(refreshed.anime?.title).isEqualTo("Spy x Family")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `add scope flow with first only passes single season`() = runTest {
        coEvery { findAnimeBySeasonMalIdUseCase(50) } returns null
        coEvery { resolveAnimeUseCase(50) } returns Result.success(
            ResolvedSeries(
                title = "Spy x Family",
                seasons = listOf(
                    SeasonData(malId = 50, title = "Season 1", type = "TV"),
                    SeasonData(malId = 51, title = "Season 2", type = "TV")
                )
            )
        )
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
            expectMostRecentItem()

            viewModel.showAddScopeSheet(WatchStatus.WATCHING)
            awaitItem()

            viewModel.confirmAddScope(allSeasons = false)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { addAnimeUseCase(any(), match { it.size == 1 }, WatchStatus.WATCHING) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isNotificationDebugInfoEnabled propagates from use case to state`() = runTest {
        every { observeIsNotificationDebugInfoEnabledUseCase() } returns flowOf(true)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.isNotificationDebugInfoEnabled).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `typeFilter defaults to empty set on initial load`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            val success = awaitItem()
            assertThat(success.typeFilter).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleTypeFilter adds type to filter when not present`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.toggleTypeFilter("TV")

            val state = awaitItem()
            assertThat(state.typeFilter).containsExactly("TV")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleTypeFilter removes type from filter when already present`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.toggleTypeFilter("TV")
            awaitItem()
            viewModel.toggleTypeFilter("TV")

            val state = awaitItem()
            assertThat(state.typeFilter).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetTypeFilter clears all selected types`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.toggleTypeFilter("TV")
            awaitItem()
            viewModel.toggleTypeFilter("OVA")
            awaitItem()

            viewModel.resetTypeFilter()

            val state = awaitItem()
            assertThat(state.typeFilter).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
