package com.vuzeda.animewatchlist.tracker.ui.screens.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.NotificationType
import com.vuzeda.animewatchlist.tracker.domain.model.TitleLanguage
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveAnimeListUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveTitleLanguageUseCase
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

    private val sampleAnimeList = listOf(
        Anime(id = 1L, title = "Attack on Titan", status = WatchStatus.WATCHING, userRating = 8, addedAt = 1000L, notificationType = NotificationType.BOTH),
        Anime(id = 2L, title = "One Punch Man", status = WatchStatus.COMPLETED, userRating = 9, addedAt = 3000L, notificationType = NotificationType.NONE),
        Anime(id = 3L, title = "Bleach", status = WatchStatus.WATCHING, userRating = 7, addedAt = 2000L, notificationType = NotificationType.NEW_EPISODES)
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.DEFAULT)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        every { observeAnimeListUseCase() } returns flowOf(sampleAnimeList)
        return HomeViewModel(observeAnimeListUseCase, observeTitleLanguageUseCase)
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

        val viewModel = HomeViewModel(observeAnimeListUseCase, observeTitleLanguageUseCase)

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
}
