package com.vuzeda.animewatchlist.tracker.ui.screens.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveAnimeByNotificationUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveAnimeListUseCase
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
    private val observeAnimeByNotificationUseCase: ObserveAnimeByNotificationUseCase = mockk()

    private val sampleAnimeList = listOf(
        Anime(id = 1L, title = "Attack on Titan", status = WatchStatus.WATCHING, userRating = 8, addedAt = 1000L, isNotificationsEnabled = true),
        Anime(id = 2L, title = "One Punch Man", status = WatchStatus.COMPLETED, userRating = 9, addedAt = 3000L, isNotificationsEnabled = false),
        Anime(id = 3L, title = "Bleach", status = WatchStatus.WATCHING, userRating = 7, addedAt = 2000L, isNotificationsEnabled = true)
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        every { observeAnimeListUseCase() } returns flowOf(sampleAnimeList)
        return HomeViewModel(observeAnimeListUseCase, observeAnimeByNotificationUseCase)
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
            assertThat(loaded.selectedFilter).isEqualTo(HomeFilter.All)
            assertThat(loaded.sortOption).isEqualTo(HomeSortOption.ALPHABETICAL)
        }
    }

    @Test
    fun `selectFilter filters by status`() = runTest {
        every { observeAnimeListUseCase(WatchStatus.WATCHING) } returns flowOf(
            listOf(sampleAnimeList[0])
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectFilter(HomeFilter.ByStatus(WatchStatus.WATCHING))

            val tabChanged = awaitItem()
            assertThat(tabChanged.selectedFilter).isEqualTo(HomeFilter.ByStatus(WatchStatus.WATCHING))
            assertThat(tabChanged.isLoading).isTrue()

            val filtered = awaitItem()
            assertThat(filtered.animeList).hasSize(1)
            assertThat(filtered.animeList[0].title).isEqualTo("Attack on Titan")
            assertThat(filtered.isLoading).isFalse()
        }
    }

    @Test
    fun `selectFilter with All shows all anime`() = runTest {
        every { observeAnimeListUseCase(WatchStatus.WATCHING) } returns flowOf(
            listOf(sampleAnimeList[0])
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectFilter(HomeFilter.ByStatus(WatchStatus.WATCHING))
            skipItems(2)

            viewModel.selectFilter(HomeFilter.All)

            val tabChanged = awaitItem()
            assertThat(tabChanged.selectedFilter).isEqualTo(HomeFilter.All)

            val allAnime = awaitItem()
            assertThat(allAnime.animeList).hasSize(3)
        }
    }

    @Test
    fun `selectFilter with NotificationsOn shows only notification enabled anime`() = runTest {
        val notifiedAnime = sampleAnimeList.filter { it.isNotificationsEnabled }
        every { observeAnimeByNotificationUseCase(enabled = true) } returns flowOf(notifiedAnime)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectFilter(HomeFilter.NotificationsOn)

            val filterChanged = awaitItem()
            assertThat(filterChanged.selectedFilter).isEqualTo(HomeFilter.NotificationsOn)
            assertThat(filterChanged.isLoading).isTrue()

            val filtered = awaitItem()
            assertThat(filtered.animeList).hasSize(2)
            assertThat(filtered.animeList.all { it.isNotificationsEnabled }).isTrue()
            assertThat(filtered.isLoading).isFalse()
        }
    }

    @Test
    fun `selectFilter with NotificationsOff shows only notification disabled anime`() = runTest {
        val nonNotifiedAnime = sampleAnimeList.filter { !it.isNotificationsEnabled }
        every { observeAnimeByNotificationUseCase(enabled = false) } returns flowOf(nonNotifiedAnime)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.selectFilter(HomeFilter.NotificationsOff)

            val filterChanged = awaitItem()
            assertThat(filterChanged.selectedFilter).isEqualTo(HomeFilter.NotificationsOff)

            val filtered = awaitItem()
            assertThat(filtered.animeList).hasSize(1)
            assertThat(filtered.animeList[0].title).isEqualTo("One Punch Man")
            assertThat(filtered.animeList.none { it.isNotificationsEnabled }).isTrue()
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

        val viewModel = HomeViewModel(observeAnimeListUseCase, observeAnimeByNotificationUseCase)

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
