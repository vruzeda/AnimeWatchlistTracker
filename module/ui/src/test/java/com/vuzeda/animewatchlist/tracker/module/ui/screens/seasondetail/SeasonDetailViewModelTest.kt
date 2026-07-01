package com.vuzeda.animewatchlist.tracker.module.ui.screens.seasondetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsTracker
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.usecase.AddAnimeFromDetailsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.AddSeasonToWatchlistUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.DeleteOrphanedWatchedEpisodesUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.DeleteSeasonUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FetchEpisodesUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.GetCachedEpisodesUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FetchSeasonDetailUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FillEpisodeGapsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.FindSeasonIdByMalIdUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveIsNotificationDebugInfoEnabledUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveSeasonByIdUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveSeasonsForAnimeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveWatchedEpisodesUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.RefreshSeasonDataUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetAllEpisodesWatchedUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetEpisodeWatchedUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ToggleSeasonEpisodeNotificationsUseCase
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
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class SeasonDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val observeSeasonByIdUseCase: ObserveSeasonByIdUseCase = mockk()
    private val observeSeasonsForAnimeUseCase: ObserveSeasonsForAnimeUseCase = mockk()
    private val fetchSeasonDetailUseCase: FetchSeasonDetailUseCase = mockk()
    private val fetchEpisodesUseCase: FetchEpisodesUseCase = mockk()
    private val getCachedEpisodesUseCase: GetCachedEpisodesUseCase = mockk()
    private val fillEpisodeGapsUseCase: FillEpisodeGapsUseCase = FillEpisodeGapsUseCase()
    private val deleteOrphanedWatchedEpisodesUseCase: DeleteOrphanedWatchedEpisodesUseCase = mockk(relaxed = true)
    private val updateSeasonStatusUseCase: UpdateSeasonStatusUseCase = mockk(relaxed = true)
    private val deleteSeasonUseCase: DeleteSeasonUseCase = mockk(relaxed = true)
    private val addSeasonToWatchlistUseCase: AddSeasonToWatchlistUseCase = mockk(relaxed = true)
    private val addAnimeFromDetailsUseCase: AddAnimeFromDetailsUseCase = mockk(relaxed = true)
    private val findSeasonIdByMalIdUseCase: FindSeasonIdByMalIdUseCase = mockk()
    private val toggleSeasonEpisodeNotificationsUseCase: ToggleSeasonEpisodeNotificationsUseCase = mockk(relaxed = true)
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase = mockk()
    private val refreshSeasonDataUseCase: RefreshSeasonDataUseCase = mockk(relaxed = true)
    private val observeIsNotificationDebugInfoEnabledUseCase: ObserveIsNotificationDebugInfoEnabledUseCase = mockk()
    private val observeWatchedEpisodesUseCase: ObserveWatchedEpisodesUseCase = mockk()
    private val setEpisodeWatchedUseCase: SetEpisodeWatchedUseCase = mockk(relaxed = true)
    private val setAllEpisodesWatchedUseCase: SetAllEpisodesWatchedUseCase = mockk(relaxed = true)
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)

    private val sampleSeason = Season(
        id = 1L,
        animeId = 1L,
        malId = 16498,
        title = "Attack on Titan",
        episodeCount = 25,
        score = 8.5,
        type = "TV",
        airingStatus = "Finished Airing"
    )

    private val airingSeasonUnknownCount = Season(
        id = 2L,
        animeId = 2L,
        malId = 55555,
        title = "Ongoing Anime",
        episodeCount = null,
        type = "TV",
        airingStatus = "Currently Airing"
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
        every { observeIsNotificationDebugInfoEnabledUseCase() } returns flowOf(false)
        every { observeWatchedEpisodesUseCase(any()) } returns flowOf(emptySet())
        every { observeSeasonsForAnimeUseCase(any()) } returns flowOf(listOf(sampleSeason))
        coEvery { findSeasonIdByMalIdUseCase(any()) } returns null
        coEvery { fetchEpisodesUseCase(malId = 16498, page = 1) } returns Result.success(
            EpisodePage(episodes = sampleEpisodes, hasNextPage = true, nextPage = 2)
        )
        coEvery { getCachedEpisodesUseCase(any()) } returns emptyList()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        seasonId: Long = 1L,
        malId: Int = 0,
        localZoneId: ZoneId = ZoneId.systemDefault()
    ): SeasonDetailViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "seasonId" to seasonId,
                "malId" to malId
            )
        )
        return object : SeasonDetailViewModel(
            savedStateHandle = savedStateHandle,
            observeSeasonByIdUseCase = observeSeasonByIdUseCase,
            observeSeasonsForAnimeUseCase = observeSeasonsForAnimeUseCase,
            fetchSeasonDetailUseCase = fetchSeasonDetailUseCase,
            fetchEpisodesUseCase = fetchEpisodesUseCase,
            getCachedEpisodesUseCase = getCachedEpisodesUseCase,
            fillEpisodeGapsUseCase = fillEpisodeGapsUseCase,
            deleteOrphanedWatchedEpisodesUseCase = deleteOrphanedWatchedEpisodesUseCase,
            updateSeasonStatusUseCase = updateSeasonStatusUseCase,
            deleteSeasonUseCase = deleteSeasonUseCase,
            addSeasonToWatchlistUseCase = addSeasonToWatchlistUseCase,
            addAnimeFromDetailsUseCase = addAnimeFromDetailsUseCase,
            findSeasonIdByMalIdUseCase = findSeasonIdByMalIdUseCase,
            toggleSeasonEpisodeNotificationsUseCase = toggleSeasonEpisodeNotificationsUseCase,
            observeTitleLanguageUseCase = observeTitleLanguageUseCase,
            refreshSeasonDataUseCase = refreshSeasonDataUseCase,
            observeIsNotificationDebugInfoEnabledUseCase = observeIsNotificationDebugInfoEnabledUseCase,
            observeWatchedEpisodesUseCase = observeWatchedEpisodesUseCase,
            setEpisodeWatchedUseCase = setEpisodeWatchedUseCase,
            setAllEpisodesWatchedUseCase = setAllEpisodesWatchedUseCase,
            analyticsTracker = analyticsTracker
        ) {
            override fun localZoneId(): ZoneId = localZoneId
        }
    }

    @Test
    fun `loads season and episodes on init`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading.isLoading).isTrue()

            testDispatcher.scheduler.advanceUntilIdle()

            val loaded = expectMostRecentItem()
            assertThat(loaded.season?.title).isEqualTo("Attack on Titan")
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
            assertThat(notFound.isNotFound).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMoreEpisodes appends episodes and fills gaps on last page`() = runTest {
        val moreEpisodes = listOf(
            EpisodeInfo(number = 3, title = "Episode 3", aired = "2013-04-21", isFiller = false, isRecap = false)
        )
        coEvery { fetchEpisodesUseCase(malId = 16498, page = 2) } returns Result.success(
            EpisodePage(episodes = moreEpisodes, hasNextPage = false, nextPage = 3)
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertThat(initial.episodes).hasSize(2) // page 1, gap-fill not applied yet
            assertThat(initial.hasMoreEpisodes).isTrue()

            viewModel.loadMoreEpisodes()
            testDispatcher.scheduler.advanceUntilIdle()

            // last page: 3 real episodes + 22 placeholders (4..25) = 25 total (sampleSeason.episodeCount = 25)
            val updated = expectMostRecentItem()
            assertThat(updated.episodes).hasSize(25)
            assertThat(updated.episodes.filter { it.isPlaceholder }).hasSize(22)
            assertThat(updated.hasMoreEpisodes).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `gap-fill adds placeholders when last page has fewer episodes than episodeCount`() = runTest {
        coEvery { fetchEpisodesUseCase(malId = 16498, page = 1) } returns Result.success(
            EpisodePage(episodes = sampleEpisodes, hasNextPage = false, nextPage = 2)
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val state = expectMostRecentItem()
            // sampleSeason.episodeCount = 25; 2 real episodes fetched → 23 placeholders added
            assertThat(state.episodes).hasSize(25)
            assertThat(state.episodes.take(2).none { it.isPlaceholder }).isTrue()
            assertThat(state.episodes.drop(2).all { it.isPlaceholder }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `season updates reactively from database`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertThat(initial.season?.title).isEqualTo("Attack on Titan")

            seasonFlow.value = sampleSeason.copy(title = "Attack on Titan Updated")

            val updated = awaitItem()
            assertThat(updated.season?.title).isEqualTo("Attack on Titan Updated")
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
            assertThat(loading.isLoading).isTrue()

            testDispatcher.scheduler.advanceUntilIdle()

            val success = expectMostRecentItem()
            assertThat(success.season?.title).isEqualTo("Spy x Family")
            assertThat(success.season?.malId).isEqualTo(50265)
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
            assertThat(notFound.isNotFound).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh retries the API fetch and recovers from not-found when malId succeeds on retry`() = runTest {
        coEvery { fetchSeasonDetailUseCase(50265) } returns Result.failure(Exception("Not found"))

        val viewModel = createViewModel(seasonId = 0L, malId = 50265)

        viewModel.uiState.test {
            awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()
            val notFound = expectMostRecentItem()
            assertThat(notFound.isNotFound).isTrue()

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

            viewModel.refresh()
            testDispatcher.scheduler.advanceUntilIdle()

            val recovered = expectMostRecentItem()
            assertThat(recovered.isNotFound).isFalse()
            assertThat(recovered.isRefreshing).isFalse()
            assertThat(recovered.season?.malId).isEqualTo(50265)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh sets isNotFound again when the retried API fetch still fails`() = runTest {
        coEvery { fetchSeasonDetailUseCase(999) } returns Result.failure(Exception("Not found"))

        val viewModel = createViewModel(seasonId = 0L, malId = 999)

        viewModel.uiState.test {
            awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.refresh()
            testDispatcher.scheduler.advanceUntilIdle()

            val stillNotFound = expectMostRecentItem()
            assertThat(stillNotFound.isNotFound).isTrue()
            assertThat(stillNotFound.isRefreshing).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shows not found when both seasonId and malId are zero`() = runTest {
        val viewModel = createViewModel(seasonId = 0L, malId = 0)

        viewModel.uiState.test {
            val notFound = awaitItem()
            assertThat(notFound.isNotFound).isTrue()
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
    fun `confirmDelete calls delete use case and marks season as not in watchlist`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.confirmDelete()
            testDispatcher.scheduler.advanceUntilIdle()

            val deleted = expectMostRecentItem()
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
            val initial = expectMostRecentItem()
            assertThat(initial.isInWatchlist).isFalse()

            viewModel.showAddSheet()
            val shown = awaitItem()
            assertThat(shown.isAddSheetVisible).isTrue()

            viewModel.dismissAddSheet()
            val hidden = awaitItem()
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
            val resolved = expectMostRecentItem()
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
            val state = expectMostRecentItem()
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
            val state = expectMostRecentItem()
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
    fun `loadFromApi with existing non-watchlist season reflects isInWatchlist false`() = runTest {
        val nonWatchlistSeason = sampleSeason.copy(id = 5L, malId = 16498, isInWatchlist = false)
        coEvery { findSeasonIdByMalIdUseCase(16498) } returns 5L
        every { observeSeasonByIdUseCase(5L) } returns MutableStateFlow<Season?>(nonWatchlistSeason)
        every { observeWatchedEpisodesUseCase(5L) } returns flowOf(emptySet())
        every { observeSeasonsForAnimeUseCase(nonWatchlistSeason.animeId) } returns flowOf(listOf(nonWatchlistSeason))

        val viewModel = createViewModel(seasonId = 0L, malId = 16498)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.isInWatchlist).isFalse()
            assertThat(state.season?.id).isEqualTo(5L)
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
    fun `broadcastLocalTime is computed from structured broadcast fields`() = runTest {
        val seasonWithBroadcast = sampleSeason.copy(
            broadcastDay = "Saturdays",
            broadcastTime = "18:00",
            broadcastTimezone = "Asia/Tokyo"
        )
        seasonFlow.value = seasonWithBroadcast

        val viewModel = createViewModel(localZoneId = ZoneId.of("UTC"))

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val state = expectMostRecentItem()
            assertThat(state.broadcastLocalTime).isEqualTo(LocalBroadcastTime(day = "Saturday", time = "09:00", zone = "UTC"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `broadcastLocalTime is null when broadcast fields are missing`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val state = expectMostRecentItem()
            assertThat(state.broadcastLocalTime).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `triggers background refresh of season data from API on load from DB`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            coVerify { refreshSeasonDataUseCase(sampleSeason) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh calls refreshSeasonDataUseCase for local season and clears isRefreshing`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.refresh()
            testDispatcher.scheduler.advanceUntilIdle()

            val refreshed = expectMostRecentItem()
            assertThat(refreshed.isRefreshing).isFalse()
            coVerify(atLeast = 2) { refreshSeasonDataUseCase(sampleSeason) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh reloads episodes for local season`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertThat(initial.episodes).hasSize(2)

            viewModel.refresh()
            testDispatcher.scheduler.advanceUntilIdle()

            val refreshed = expectMostRecentItem()
            assertThat(refreshed.isRefreshing).isFalse()
            assertThat(refreshed.isLoadingEpisodes).isFalse()
            assertThat(refreshed.episodes).hasSize(2)
            coVerify(atLeast = 2) { fetchEpisodesUseCase(malId = 16498, page = 1) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `episode load failure emits EpisodeLoadFailed snackbar event`() = runTest {
        coEvery { fetchEpisodesUseCase(malId = 16498, page = 1) } returns Result.failure(Exception("rate limited"))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.snackbarEvent).isEqualTo(SeasonDetailSnackbarEvent.EpisodeLoadFailed)
            assertThat(state.isLoadingEpisodes).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads cached episodes when initial fetch fails`() = runTest {
        coEvery { fetchEpisodesUseCase(malId = 16498, page = 1) } returns Result.failure(Exception("rate limited"))
        coEvery { getCachedEpisodesUseCase(16498) } returns sampleEpisodes

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            // 2 real cached episodes + 23 placeholders to fill sampleSeason.episodeCount=25
            assertThat(state.episodes).hasSize(25)
            assertThat(state.episodes.filter { !it.isPlaceholder }).hasSize(2)
            assertThat(state.snackbarEvent).isEqualTo(SeasonDetailSnackbarEvent.EpisodeLoadFailed)
            assertThat(state.isLoadingEpisodes).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `episode load failure fills placeholder gaps when cache is empty`() = runTest {
        coEvery { fetchEpisodesUseCase(malId = 16498, page = 1) } returns Result.failure(Exception("rate limited"))
        coEvery { getCachedEpisodesUseCase(16498) } returns emptyList()

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            // cache is empty; gap-fill should still produce 25 placeholders from episodeCount
            assertThat(state.episodes).hasSize(25)
            assertThat(state.episodes.all { it.isPlaceholder }).isTrue()
            assertThat(state.snackbarEvent).isEqualTo(SeasonDetailSnackbarEvent.EpisodeLoadFailed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh keeps cached episodes and fills gaps when episode reload fails`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertThat(initial.episodes).hasSize(2)

            coEvery { fetchEpisodesUseCase(malId = 16498, page = 1) } returns Result.failure(Exception("rate limited"))
            viewModel.refresh()
            testDispatcher.scheduler.advanceUntilIdle()

            val refreshed = expectMostRecentItem()
            assertThat(refreshed.isRefreshing).isFalse()
            // 2 real episodes retained + 23 placeholders = 25 (gap-fill applied on failure path)
            assertThat(refreshed.episodes).hasSize(25)
            assertThat(refreshed.snackbarEvent).isEqualTo(SeasonDetailSnackbarEvent.EpisodeLoadFailed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh re-fetches from API for remote season and updates state`() = runTest {
        val details = AnimeFullDetails(
            malId = 16498,
            title = "Attack on Titan",
            type = "TV",
            episodes = 25,
            score = 8.7,
            airingStatus = "Finished Airing",
            sequels = emptyList()
        )
        coEvery { findSeasonIdByMalIdUseCase(16498) } returns null
        coEvery { fetchSeasonDetailUseCase(16498) } returns Result.success(details)
        coEvery { fetchEpisodesUseCase(malId = 16498, page = 1) } returns Result.success(
            EpisodePage(episodes = sampleEpisodes, hasNextPage = false, nextPage = 1)
        )

        val viewModel = createViewModel(seasonId = 0L, malId = 16498)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertThat(initial.isInWatchlist).isFalse()

            viewModel.refresh()
            testDispatcher.scheduler.advanceUntilIdle()

            val refreshed = expectMostRecentItem()
            assertThat(refreshed.isRefreshing).isFalse()
            assertThat(refreshed.season?.title).isEqualTo("Attack on Titan")
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

            val updated = awaitItem()
            assertThat(updated.pendingNavigationMalId).isEqualTo(16498)
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
    fun `watchedEpisodes defaults to empty set on initial load`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.watchedEpisodes).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setEpisodeWatched delegates to use case`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.setEpisodeWatched(3, true)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { setEpisodeWatchedUseCase(sampleSeason.id, 3, true) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setEpisodeWatched does nothing when not in watchlist`() = runTest {
        seasonFlow.value = sampleSeason.copy(isInWatchlist = false)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.setEpisodeWatched(1, true)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 0) { setEpisodeWatchedUseCase(any(), any(), any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `markAllEpisodesWatched delegates loaded episodes to use case`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.markAllEpisodesWatched()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { setAllEpisodesWatchedUseCase(sampleSeason.id, listOf(1, 2)) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `markAllEpisodesWatched does nothing when not in watchlist`() = runTest {
        seasonFlow.value = sampleSeason.copy(isInWatchlist = false)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.markAllEpisodesWatched()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 0) { setAllEpisodesWatchedUseCase(any(), any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `watchedEpisodes are present on first Success emission when they emit before season`() = runTest {
        val delayedSeasonFlow = MutableStateFlow<Season?>(null)
        every { observeSeasonByIdUseCase(1L) } returns delayedSeasonFlow
        every { observeWatchedEpisodesUseCase(1L) } returns flowOf(setOf(1, 2, 3))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading

            delayedSeasonFlow.value = sampleSeason
            testDispatcher.scheduler.advanceUntilIdle()

            val success = expectMostRecentItem()
            assertThat(success.watchedEpisodes).containsExactly(1, 2, 3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- Trailing airing placeholder tests ---

    private fun setupAiringSeasonFlow(): MutableStateFlow<Season?> {
        val flow = MutableStateFlow<Season?>(airingSeasonUnknownCount)
        every { observeSeasonByIdUseCase(2L) } returns flow
        every { observeWatchedEpisodesUseCase(2L) } returns flowOf(emptySet())
        every { observeSeasonsForAnimeUseCase(2L) } returns flowOf(listOf(airingSeasonUnknownCount))
        return flow
    }

    @Test
    fun `trailing placeholder appended on last page for currently airing season with unknown episode count`() = runTest {
        setupAiringSeasonFlow()
        coEvery { fetchEpisodesUseCase(malId = 55555, page = 1) } returns Result.success(
            EpisodePage(episodes = sampleEpisodes, hasNextPage = false, nextPage = 2)
        )

        val viewModel = createViewModel(seasonId = 2L)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.episodes).hasSize(3) // 2 real + 1 trailing placeholder
            assertThat(state.episodes.filter { !it.isPlaceholder }).hasSize(2)
            assertThat(state.episodes.last().isPlaceholder).isTrue()
            assertThat(state.episodes.last().number).isEqualTo(3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `no trailing placeholder appended when more pages remain`() = runTest {
        setupAiringSeasonFlow()
        coEvery { fetchEpisodesUseCase(malId = 55555, page = 1) } returns Result.success(
            EpisodePage(episodes = sampleEpisodes, hasNextPage = true, nextPage = 2)
        )

        val viewModel = createViewModel(seasonId = 2L)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.episodes).hasSize(2)
            assertThat(state.episodes.none { it.isPlaceholder }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `no trailing placeholder for finished airing season with unknown episode count`() = runTest {
        val finishedSeason = airingSeasonUnknownCount.copy(airingStatus = "Finished Airing")
        val flow = MutableStateFlow<Season?>(finishedSeason)
        every { observeSeasonByIdUseCase(2L) } returns flow
        every { observeWatchedEpisodesUseCase(2L) } returns flowOf(emptySet())
        every { observeSeasonsForAnimeUseCase(2L) } returns flowOf(listOf(finishedSeason))
        coEvery { fetchEpisodesUseCase(malId = 55555, page = 1) } returns Result.success(
            EpisodePage(episodes = sampleEpisodes, hasNextPage = false, nextPage = 2)
        )

        val viewModel = createViewModel(seasonId = 2L)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.episodes).hasSize(2)
            assertThat(state.episodes.none { it.isPlaceholder }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `no trailing placeholder for currently airing season with known episode count`() = runTest {
        val knownCountSeason = airingSeasonUnknownCount.copy(episodeCount = 24)
        val flow = MutableStateFlow<Season?>(knownCountSeason)
        every { observeSeasonByIdUseCase(2L) } returns flow
        every { observeWatchedEpisodesUseCase(2L) } returns flowOf(emptySet())
        every { observeSeasonsForAnimeUseCase(2L) } returns flowOf(listOf(knownCountSeason))
        coEvery { fetchEpisodesUseCase(malId = 55555, page = 1) } returns Result.success(
            EpisodePage(episodes = sampleEpisodes, hasNextPage = false, nextPage = 2)
        )

        val viewModel = createViewModel(seasonId = 2L)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            // gap-fill only: 2 real + 22 placeholders = 24, not trailing-at-3
            assertThat(state.episodes).hasSize(24)
            assertThat(state.episodes.first { it.isPlaceholder }.number).isEqualTo(3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `trailing placeholder starts at episode 1 when no real episodes loaded yet`() = runTest {
        setupAiringSeasonFlow()
        coEvery { fetchEpisodesUseCase(malId = 55555, page = 1) } returns Result.success(
            EpisodePage(episodes = emptyList(), hasNextPage = false, nextPage = 2)
        )

        val viewModel = createViewModel(seasonId = 2L)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.episodes).hasSize(1)
            assertThat(state.episodes.single().number).isEqualTo(1)
            assertThat(state.episodes.single().isPlaceholder).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `trailing placeholder advances when current placeholder is marked watched`() = runTest {
        val watchedFlow = MutableStateFlow<Set<Int>>(emptySet())
        every { observeSeasonByIdUseCase(2L) } returns MutableStateFlow<Season?>(airingSeasonUnknownCount)
        every { observeWatchedEpisodesUseCase(2L) } returns watchedFlow
        every { observeSeasonsForAnimeUseCase(2L) } returns flowOf(listOf(airingSeasonUnknownCount))
        coEvery { fetchEpisodesUseCase(malId = 55555, page = 1) } returns Result.success(
            EpisodePage(episodes = sampleEpisodes, hasNextPage = false, nextPage = 2)
        )

        val viewModel = createViewModel(seasonId = 2L)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val initial = expectMostRecentItem()
            // 2 real + placeholder at 3
            assertThat(initial.episodes.last().number).isEqualTo(3)

            watchedFlow.value = setOf(3)
            testDispatcher.scheduler.advanceUntilIdle()

            val advanced = expectMostRecentItem()
            // 2 real + watched placeholder 3 + new trailing placeholder 4
            assertThat(advanced.episodes).hasSize(4)
            assertThat(advanced.episodes[2].number).isEqualTo(3)
            assertThat(advanced.episodes[2].isPlaceholder).isTrue()
            assertThat(advanced.episodes[3].number).isEqualTo(4)
            assertThat(advanced.episodes[3].isPlaceholder).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `trailing placeholder does not advance while episodes are loading`() = runTest {
        val watchedFlow = MutableStateFlow<Set<Int>>(emptySet())
        every { observeSeasonByIdUseCase(2L) } returns MutableStateFlow<Season?>(airingSeasonUnknownCount)
        every { observeWatchedEpisodesUseCase(2L) } returns watchedFlow
        every { observeSeasonsForAnimeUseCase(2L) } returns flowOf(listOf(airingSeasonUnknownCount))
        coEvery { fetchEpisodesUseCase(malId = 55555, page = 1) } returns Result.success(
            EpisodePage(episodes = sampleEpisodes, hasNextPage = true, nextPage = 2)
        )

        val viewModel = createViewModel(seasonId = 2L)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertThat(initial.hasMoreEpisodes).isTrue()
            assertThat(initial.episodes.none { it.isPlaceholder }).isTrue()

            watchedFlow.value = setOf(1, 2)
            testDispatcher.scheduler.advanceUntilIdle()

            val updated = expectMostRecentItem()
            // still no trailing placeholder — hasMoreEpisodes guard active
            assertThat(updated.episodes.none { it.isPlaceholder }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `trailing placeholder accounts for max watched episode exceeding max real episode`() = runTest {
        val watchedFlow = MutableStateFlow<Set<Int>>(setOf(1, 2, 3, 4, 5))
        every { observeSeasonByIdUseCase(2L) } returns MutableStateFlow<Season?>(airingSeasonUnknownCount)
        every { observeWatchedEpisodesUseCase(2L) } returns watchedFlow
        every { observeSeasonsForAnimeUseCase(2L) } returns flowOf(listOf(airingSeasonUnknownCount))
        coEvery { fetchEpisodesUseCase(malId = 55555, page = 1) } returns Result.success(
            EpisodePage(episodes = sampleEpisodes, hasNextPage = false, nextPage = 2)
        )

        val viewModel = createViewModel(seasonId = 2L)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            // real: 1, 2 — watched placeholders: 3, 4, 5 — trailing: 6
            assertThat(state.episodes.last().number).isEqualTo(6)
            assertThat(state.episodes.last().isPlaceholder).isTrue()
            assertThat(state.episodes.filter { it.isPlaceholder }).hasSize(4) // 3, 4, 5, 6
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh re-derives trailing placeholder correctly for airing season`() = runTest {
        setupAiringSeasonFlow()
        coEvery { fetchEpisodesUseCase(malId = 55555, page = 1) } returns Result.success(
            EpisodePage(episodes = sampleEpisodes, hasNextPage = false, nextPage = 2)
        )

        val viewModel = createViewModel(seasonId = 2L)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertThat(initial.episodes.last().number).isEqualTo(3)
            assertThat(initial.episodes.last().isPlaceholder).isTrue()

            viewModel.refresh()
            testDispatcher.scheduler.advanceUntilIdle()

            val refreshed = expectMostRecentItem()
            assertThat(refreshed.isRefreshing).isFalse()
            assertThat(refreshed.episodes).hasSize(3)
            assertThat(refreshed.episodes.last().number).isEqualTo(3)
            assertThat(refreshed.episodes.last().isPlaceholder).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- Orphaned watched episode cleanup tests ---

    @Test
    fun `orphaned watched episodes are cleaned up on first load when episode count is known`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            coVerify { deleteOrphanedWatchedEpisodesUseCase(sampleSeason.id, sampleSeason.episodeCount!!) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `orphaned watched episodes are cleaned up when episode count transitions from null to non-null`() = runTest {
        val nullCountSeason = airingSeasonUnknownCount
        val confirmedCountSeason = airingSeasonUnknownCount.copy(episodeCount = 13)
        val flow = MutableStateFlow<Season?>(nullCountSeason)
        every { observeSeasonByIdUseCase(2L) } returns flow
        every { observeWatchedEpisodesUseCase(2L) } returns flowOf(emptySet())
        every { observeSeasonsForAnimeUseCase(2L) } returns flowOf(listOf(nullCountSeason))
        coEvery { fetchEpisodesUseCase(malId = 55555, page = 1) } returns Result.success(
            EpisodePage(episodes = emptyList(), hasNextPage = false, nextPage = 2)
        )

        val viewModel = createViewModel(seasonId = 2L)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            coVerify(exactly = 0) { deleteOrphanedWatchedEpisodesUseCase(any(), any()) }

            flow.value = confirmedCountSeason
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { deleteOrphanedWatchedEpisodesUseCase(confirmedCountSeason.id, 13) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `orphaned watched episodes are not cleaned up when episode count remains null`() = runTest {
        setupAiringSeasonFlow()
        coEvery { fetchEpisodesUseCase(malId = 55555, page = 1) } returns Result.success(
            EpisodePage(episodes = emptyList(), hasNextPage = false, nextPage = 2)
        )

        val viewModel = createViewModel(seasonId = 2L)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            coVerify(exactly = 0) { deleteOrphanedWatchedEpisodesUseCase(any(), any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
