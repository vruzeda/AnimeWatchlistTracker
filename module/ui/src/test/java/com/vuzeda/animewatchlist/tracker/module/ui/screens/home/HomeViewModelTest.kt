package com.vuzeda.animewatchlist.tracker.module.ui.screens.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.HomeSortOption
import com.vuzeda.animewatchlist.tracker.module.domain.HomeSortState
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsTracker
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveAllSeasonsUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveAnimeListUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveHomeNotificationFilterUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveHomeSortStateUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveHomeStatusFilterUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveHomeViewModeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetHomeNotificationFilterUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetHomeSortStateUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetHomeStatusFilterUseCase
import io.mockk.coEvery
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val observeAnimeListUseCase: ObserveAnimeListUseCase = mockk()
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase = mockk()
    private val observeHomeViewModeUseCase: ObserveHomeViewModeUseCase = mockk()
    private val observeAllSeasonsUseCase: ObserveAllSeasonsUseCase = mockk()
    private val observeHomeSortStateUseCase: ObserveHomeSortStateUseCase = mockk()
    private val setHomeSortStateUseCase: SetHomeSortStateUseCase = mockk()
    private val observeHomeStatusFilterUseCase: ObserveHomeStatusFilterUseCase = mockk()
    private val setHomeStatusFilterUseCase: SetHomeStatusFilterUseCase = mockk()
    private val observeHomeNotificationFilterUseCase: ObserveHomeNotificationFilterUseCase = mockk()
    private val setHomeNotificationFilterUseCase: SetHomeNotificationFilterUseCase = mockk()
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)

    private val sortStateFlow = MutableStateFlow(HomeSortState())
    private val statusFilterFlow = MutableStateFlow<WatchStatus?>(null)
    private val notificationFilterFlow = MutableStateFlow<Boolean?>(null)

    private val sampleAnimeList = listOf(
        Anime(id = 1L, title = "Attack on Titan", status = WatchStatus.WATCHING, userRating = 8, addedAt = 1000L, notificationType = NotificationType.BOTH),
        Anime(id = 2L, title = "One Punch Man", status = WatchStatus.COMPLETED, userRating = 9, addedAt = 3000L, notificationType = NotificationType.NONE),
        Anime(id = 3L, title = "Bleach", status = WatchStatus.WATCHING, userRating = 7, addedAt = 2000L, notificationType = NotificationType.NEW_EPISODES)
    )

    private val sampleSeasonList = listOf(
        Season(id = 10L, animeId = 1L, malId = 100, title = "Attack on Titan S1", score = 9.0, episodeCount = 25, watchedEpisodeCount = 25, status = WatchStatus.COMPLETED, addedAt = 500L, isEpisodeNotificationsEnabled = false),
        Season(id = 11L, animeId = 1L, malId = 101, title = "Attack on Titan S2", score = 8.5, episodeCount = 12, watchedEpisodeCount = 6, status = WatchStatus.WATCHING, addedAt = 1500L, isEpisodeNotificationsEnabled = true),
        Season(id = 20L, animeId = 2L, malId = 200, title = "One Punch Man S1", score = 8.8, episodeCount = 12, watchedEpisodeCount = 12, status = WatchStatus.COMPLETED, addedAt = 3000L, isEpisodeNotificationsEnabled = false),
        Season(id = 30L, animeId = 3L, malId = 300, title = "Bleach S1", score = 7.5, episodeCount = 50, watchedEpisodeCount = 10, status = WatchStatus.WATCHING, addedAt = 2000L, isEpisodeNotificationsEnabled = true)
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.DEFAULT)
        every { observeHomeViewModeUseCase() } returns flowOf(HomeViewMode.ANIME)
        every { observeAllSeasonsUseCase() } returns flowOf(emptyList())
        every { observeHomeSortStateUseCase() } returns sortStateFlow
        every { observeHomeStatusFilterUseCase() } returns statusFilterFlow
        every { observeHomeNotificationFilterUseCase() } returns notificationFilterFlow
        coEvery { setHomeSortStateUseCase(any()) } answers { sortStateFlow.value = firstArg() }
        coEvery { setHomeStatusFilterUseCase(any()) } answers { statusFilterFlow.value = firstArg() }
        coEvery { setHomeNotificationFilterUseCase(any()) } answers { notificationFilterFlow.value = firstArg() }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        every { observeAnimeListUseCase() } returns flowOf(sampleAnimeList)
        return HomeViewModel(
            observeAnimeListUseCase,
            observeTitleLanguageUseCase,
            observeHomeViewModeUseCase,
            observeAllSeasonsUseCase,
            observeHomeSortStateUseCase,
            setHomeSortStateUseCase,
            observeHomeStatusFilterUseCase,
            setHomeStatusFilterUseCase,
            observeHomeNotificationFilterUseCase,
            setHomeNotificationFilterUseCase,
            analyticsTracker
        )
    }

    @Test
    fun `initial state loads all anime sorted alphabetically`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading.isLoading).isTrue()

            val loaded = awaitItem()
            assertThat(loaded.isLoading).isFalse()
            assertThat(loaded.animeList).hasSize(3)
            assertThat(loaded.animeList[0].title).isEqualTo("Attack on Titan")
            assertThat(loaded.animeList[1].title).isEqualTo("Bleach")
            assertThat(loaded.animeList[2].title).isEqualTo("One Punch Man")
            assertThat(loaded.filterState).isEqualTo(HomeFilterState())
            assertThat(loaded.sortOption).isEqualTo(HomeSortOption.ALPHABETICAL)
        }
    }

    @Test
    fun `selectStatusFilter filters by status`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectStatusFilter(WatchStatus.WATCHING)

            val filtered = awaitItem()
            assertThat(filtered.filterState.statusFilter).isEqualTo(WatchStatus.WATCHING)
            assertThat(filtered.animeList).hasSize(2)
            assertThat(filtered.animeList.all { it.status == WatchStatus.WATCHING }).isTrue()
        }
    }

    @Test
    fun `selectStatusFilter with null shows all anime`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectStatusFilter(WatchStatus.WATCHING)
            awaitItem()

            viewModel.selectStatusFilter(null)

            val allAnime = awaitItem()
            assertThat(allAnime.filterState.statusFilter).isNull()
            assertThat(allAnime.animeList).hasSize(3)
        }
    }

    @Test
    fun `selectNotificationFilter with true shows only notification enabled anime`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectNotificationFilter(true)

            val filtered = awaitItem()
            assertThat(filtered.filterState.notificationFilter).isTrue()
            assertThat(filtered.animeList).hasSize(2)
            assertThat(filtered.animeList.all { it.isNotificationsEnabled }).isTrue()
        }
    }

    @Test
    fun `selectNotificationFilter with false shows only notification disabled anime`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectNotificationFilter(false)

            val filtered = awaitItem()
            assertThat(filtered.filterState.notificationFilter).isFalse()
            assertThat(filtered.animeList).hasSize(1)
            assertThat(filtered.animeList[0].title).isEqualTo("One Punch Man")
        }
    }

    @Test
    fun `combined status and notification filters apply together`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectStatusFilter(WatchStatus.WATCHING)
            awaitItem()

            viewModel.selectNotificationFilter(true)

            val combined = awaitItem()
            assertThat(combined.filterState.statusFilter).isEqualTo(WatchStatus.WATCHING)
            assertThat(combined.filterState.notificationFilter).isTrue()
            assertThat(combined.animeList).hasSize(2)
            assertThat(combined.animeList.all { it.status == WatchStatus.WATCHING && it.isNotificationsEnabled }).isTrue()
        }
    }

    @Test
    fun `resetFilters clears both filters`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectStatusFilter(WatchStatus.WATCHING)
            awaitItem()
            viewModel.selectNotificationFilter(true)
            awaitItem()

            viewModel.resetFilters()

            val reset = awaitItem()
            assertThat(reset.filterState).isEqualTo(HomeFilterState())
            assertThat(reset.animeList).hasSize(3)
        }
    }

    @Test
    fun `selectSort with USER_RATING sorts by user rating descending`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectSort(HomeSortOption.USER_RATING)

            val sorted = awaitItem()
            assertThat(sorted.animeList[0].title).isEqualTo("One Punch Man")
            assertThat(sorted.animeList[1].title).isEqualTo("Attack on Titan")
            assertThat(sorted.animeList[2].title).isEqualTo("Bleach")
        }
    }

    @Test
    fun `selectSort with WATCH_STATUS sorts by status ordinal ascending`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectSort(HomeSortOption.WATCH_STATUS)

            val sorted = awaitItem()
            assertThat(sorted.sortOption).isEqualTo(HomeSortOption.WATCH_STATUS)
            assertThat(sorted.animeList[0].title).isEqualTo("Attack on Titan")
            assertThat(sorted.animeList[1].title).isEqualTo("Bleach")
            assertThat(sorted.animeList[2].title).isEqualTo("One Punch Man")
        }
    }

    @Test
    fun `selectSort with RECENTLY_ADDED sorts by addedAt descending`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectSort(HomeSortOption.RECENTLY_ADDED)

            val sorted = awaitItem()
            assertThat(sorted.sortOption).isEqualTo(HomeSortOption.RECENTLY_ADDED)
            assertThat(sorted.animeList[0].title).isEqualTo("One Punch Man")
            assertThat(sorted.animeList[1].title).isEqualTo("Bleach")
            assertThat(sorted.animeList[2].title).isEqualTo("Attack on Titan")
        }
    }

    @Test
    fun `selectSort toggles direction when same option is selected again`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectSort(HomeSortOption.ALPHABETICAL)
            val toggled = awaitItem()
            assertThat(toggled.isSortAscending).isFalse()
            assertThat(toggled.animeList[0].title).isEqualTo("One Punch Man")

            viewModel.selectSort(HomeSortOption.ALPHABETICAL)
            val back = awaitItem()
            assertThat(back.isSortAscending).isTrue()
            assertThat(back.animeList[0].title).isEqualTo("Attack on Titan")
        }
    }

    @Test
    fun `selectSort resets direction to default when switching to different option`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectSort(HomeSortOption.RECENTLY_ADDED)
            awaitItem()
            viewModel.selectSort(HomeSortOption.RECENTLY_ADDED)
            val reversed = awaitItem()
            assertThat(reversed.isSortAscending).isTrue()

            viewModel.selectSort(HomeSortOption.ALPHABETICAL)
            val alpha = awaitItem()
            assertThat(alpha.isSortAscending).isTrue()
            assertThat(alpha.animeList[0].title).isEqualTo("Attack on Titan")
        }
    }

    @Test
    fun `sort persists when new data arrives from Flow`() = runTest {
        val watchlistFlow = MutableStateFlow(sampleAnimeList)
        every { observeAnimeListUseCase() } returns watchlistFlow

        val viewModel = HomeViewModel(
            observeAnimeListUseCase,
            observeTitleLanguageUseCase,
            observeHomeViewModeUseCase,
            observeAllSeasonsUseCase,
            observeHomeSortStateUseCase,
            setHomeSortStateUseCase,
            observeHomeStatusFilterUseCase,
            setHomeStatusFilterUseCase,
            observeHomeNotificationFilterUseCase,
            setHomeNotificationFilterUseCase,
            analyticsTracker
        )

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectSort(HomeSortOption.USER_RATING)
            val sorted = awaitItem()
            assertThat(sorted.animeList[0].userRating).isEqualTo(9)

            val updatedList = sampleAnimeList + Anime(
                id = 4L, title = "Demon Slayer", status = WatchStatus.WATCHING, userRating = 10
            )
            watchlistFlow.value = updatedList

            val updated = awaitItem()
            assertThat(updated.sortOption).isEqualTo(HomeSortOption.USER_RATING)
            assertThat(updated.animeList[0].userRating).isEqualTo(10)
            assertThat(updated.animeList[1].userRating).isEqualTo(9)
        }
    }

    @Test
    fun `season mode shows season items sorted alphabetically`() = runTest {
        every { observeAnimeListUseCase() } returns flowOf(sampleAnimeList)
        every { observeAllSeasonsUseCase() } returns flowOf(sampleSeasonList)
        every { observeHomeViewModeUseCase() } returns flowOf(HomeViewMode.SEASON)

        val viewModel = HomeViewModel(
            observeAnimeListUseCase,
            observeTitleLanguageUseCase,
            observeHomeViewModeUseCase,
            observeAllSeasonsUseCase,
            observeHomeSortStateUseCase,
            setHomeSortStateUseCase,
            observeHomeStatusFilterUseCase,
            setHomeStatusFilterUseCase,
            observeHomeNotificationFilterUseCase,
            setHomeNotificationFilterUseCase,
            analyticsTracker
        )

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading.isLoading).isTrue()

            val loaded = awaitItem()
            assertThat(loaded.homeViewMode).isEqualTo(HomeViewMode.SEASON)
            assertThat(loaded.seasonItems).hasSize(4)
            assertThat(loaded.seasonItems[0].season.title).isEqualTo("Attack on Titan S1")
            assertThat(loaded.seasonItems[1].season.title).isEqualTo("Attack on Titan S2")
            assertThat(loaded.seasonItems[2].season.title).isEqualTo("Bleach S1")
            assertThat(loaded.seasonItems[3].season.title).isEqualTo("One Punch Man S1")
        }
    }

    @Test
    fun `season mode filters by season status`() = runTest {
        every { observeAnimeListUseCase() } returns flowOf(sampleAnimeList)
        every { observeAllSeasonsUseCase() } returns flowOf(sampleSeasonList)
        every { observeHomeViewModeUseCase() } returns flowOf(HomeViewMode.SEASON)

        val viewModel = HomeViewModel(
            observeAnimeListUseCase,
            observeTitleLanguageUseCase,
            observeHomeViewModeUseCase,
            observeAllSeasonsUseCase,
            observeHomeSortStateUseCase,
            setHomeSortStateUseCase,
            observeHomeStatusFilterUseCase,
            setHomeStatusFilterUseCase,
            observeHomeNotificationFilterUseCase,
            setHomeNotificationFilterUseCase,
            analyticsTracker
        )

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectStatusFilter(WatchStatus.COMPLETED)

            val filtered = awaitItem()
            assertThat(filtered.seasonItems).hasSize(2)
            assertThat(filtered.seasonItems.all { it.season.status == WatchStatus.COMPLETED }).isTrue()
            assertThat(filtered.seasonItems.map { it.season.title }).containsExactly("Attack on Titan S1", "One Punch Man S1")
        }
    }

    @Test
    fun `season mode excludes seasons not in watchlist`() = runTest {
        val seasonListWithNonWatchlist = sampleSeasonList + Season(
            id = 99L, animeId = 1L, malId = 999, title = "Attack on Titan OVA", isInWatchlist = false
        )
        every { observeAnimeListUseCase() } returns flowOf(sampleAnimeList)
        every { observeAllSeasonsUseCase() } returns flowOf(seasonListWithNonWatchlist)
        every { observeHomeViewModeUseCase() } returns flowOf(HomeViewMode.SEASON)

        val viewModel = HomeViewModel(
            observeAnimeListUseCase,
            observeTitleLanguageUseCase,
            observeHomeViewModeUseCase,
            observeAllSeasonsUseCase,
            observeHomeSortStateUseCase,
            setHomeSortStateUseCase,
            observeHomeStatusFilterUseCase,
            setHomeStatusFilterUseCase,
            observeHomeNotificationFilterUseCase,
            setHomeNotificationFilterUseCase,
            analyticsTracker
        )

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading.isLoading).isTrue()

            val loaded = awaitItem()
            assertThat(loaded.seasonItems).hasSize(4)
            assertThat(loaded.seasonItems.none { it.season.title == "Attack on Titan OVA" }).isTrue()
        }
    }

    @Test
    fun `season mode sorts by score when USER_RATING selected`() = runTest {
        every { observeAnimeListUseCase() } returns flowOf(sampleAnimeList)
        every { observeAllSeasonsUseCase() } returns flowOf(sampleSeasonList)
        every { observeHomeViewModeUseCase() } returns flowOf(HomeViewMode.SEASON)

        val viewModel = HomeViewModel(
            observeAnimeListUseCase,
            observeTitleLanguageUseCase,
            observeHomeViewModeUseCase,
            observeAllSeasonsUseCase,
            observeHomeSortStateUseCase,
            setHomeSortStateUseCase,
            observeHomeStatusFilterUseCase,
            setHomeStatusFilterUseCase,
            observeHomeNotificationFilterUseCase,
            setHomeNotificationFilterUseCase,
            analyticsTracker
        )

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectSort(HomeSortOption.USER_RATING)

            val sorted = awaitItem()
            assertThat(sorted.seasonItems[0].season.score).isEqualTo(9.0)
            assertThat(sorted.seasonItems[1].season.score).isEqualTo(8.8)
            assertThat(sorted.seasonItems[2].season.score).isEqualTo(8.5)
            assertThat(sorted.seasonItems[3].season.score).isEqualTo(7.5)
        }
    }

    @Test
    fun `season items carry each season's own status independently of parent anime status`() = runTest {
        val anime = Anime(id = 1L, title = "Attack on Titan", status = WatchStatus.WATCHING, addedAt = 1000L)
        val completedSeason = Season(
            id = 10L, animeId = 1L, malId = 100, title = "Attack on Titan S1",
            status = WatchStatus.COMPLETED, orderIndex = 0
        )
        val watchingSeason = Season(
            id = 11L, animeId = 1L, malId = 101, title = "Attack on Titan S2",
            status = WatchStatus.WATCHING, orderIndex = 1
        )

        val items = buildSeasonItems(
            animeList = listOf(anime),
            seasonList = listOf(completedSeason, watchingSeason),
            filterState = HomeFilterState(),
            sortState = HomeSortState(HomeSortOption.ALPHABETICAL, true)
        )

        assertThat(items).hasSize(2)
        val s1 = items.first { it.season.id == 10L }
        val s2 = items.first { it.season.id == 11L }
        assertThat(s1.season.status).isEqualTo(WatchStatus.COMPLETED)
        assertThat(s2.season.status).isEqualTo(WatchStatus.WATCHING)
    }

    @Test
    fun `season mode sorts by season status when WATCH_STATUS selected`() = runTest {
        every { observeAnimeListUseCase() } returns flowOf(sampleAnimeList)
        every { observeAllSeasonsUseCase() } returns flowOf(sampleSeasonList)
        every { observeHomeViewModeUseCase() } returns flowOf(HomeViewMode.SEASON)

        val viewModel = HomeViewModel(
            observeAnimeListUseCase,
            observeTitleLanguageUseCase,
            observeHomeViewModeUseCase,
            observeAllSeasonsUseCase,
            observeHomeSortStateUseCase,
            setHomeSortStateUseCase,
            observeHomeStatusFilterUseCase,
            setHomeStatusFilterUseCase,
            observeHomeNotificationFilterUseCase,
            setHomeNotificationFilterUseCase,
            analyticsTracker
        )

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectSort(HomeSortOption.WATCH_STATUS)

            val sorted = awaitItem()
            assertThat(sorted.seasonItems[0].season.status).isEqualTo(WatchStatus.WATCHING)
            assertThat(sorted.seasonItems[1].season.status).isEqualTo(WatchStatus.WATCHING)
            assertThat(sorted.seasonItems[2].season.status).isEqualTo(WatchStatus.COMPLETED)
            assertThat(sorted.seasonItems[3].season.status).isEqualTo(WatchStatus.COMPLETED)
        }
    }

    @Test
    fun `season mode sorts by season addedAt when RECENTLY_ADDED selected`() = runTest {
        every { observeAnimeListUseCase() } returns flowOf(sampleAnimeList)
        every { observeAllSeasonsUseCase() } returns flowOf(sampleSeasonList)
        every { observeHomeViewModeUseCase() } returns flowOf(HomeViewMode.SEASON)

        val viewModel = HomeViewModel(
            observeAnimeListUseCase,
            observeTitleLanguageUseCase,
            observeHomeViewModeUseCase,
            observeAllSeasonsUseCase,
            observeHomeSortStateUseCase,
            setHomeSortStateUseCase,
            observeHomeStatusFilterUseCase,
            setHomeStatusFilterUseCase,
            observeHomeNotificationFilterUseCase,
            setHomeNotificationFilterUseCase,
            analyticsTracker
        )

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectSort(HomeSortOption.RECENTLY_ADDED)

            val sorted = awaitItem()
            assertThat(sorted.seasonItems[0].season.addedAt).isEqualTo(3000L)
            assertThat(sorted.seasonItems[1].season.addedAt).isEqualTo(2000L)
            assertThat(sorted.seasonItems[2].season.addedAt).isEqualTo(1500L)
            assertThat(sorted.seasonItems[3].season.addedAt).isEqualTo(500L)
        }
    }

    @Test
    fun `season mode notification filter uses season episode notification setting`() = runTest {
        every { observeAnimeListUseCase() } returns flowOf(sampleAnimeList)
        every { observeAllSeasonsUseCase() } returns flowOf(sampleSeasonList)
        every { observeHomeViewModeUseCase() } returns flowOf(HomeViewMode.SEASON)

        val viewModel = HomeViewModel(
            observeAnimeListUseCase,
            observeTitleLanguageUseCase,
            observeHomeViewModeUseCase,
            observeAllSeasonsUseCase,
            observeHomeSortStateUseCase,
            setHomeSortStateUseCase,
            observeHomeStatusFilterUseCase,
            setHomeStatusFilterUseCase,
            observeHomeNotificationFilterUseCase,
            setHomeNotificationFilterUseCase,
            analyticsTracker
        )

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectNotificationFilter(true)

            val filtered = awaitItem()
            assertThat(filtered.seasonItems).hasSize(2)
            assertThat(filtered.seasonItems.all { it.season.isEpisodeNotificationsEnabled }).isTrue()

            viewModel.selectNotificationFilter(false)

            val filteredOff = awaitItem()
            assertThat(filteredOff.seasonItems).hasSize(2)
            assertThat(filteredOff.seasonItems.none { it.season.isEpisodeNotificationsEnabled }).isTrue()
        }
    }
}
