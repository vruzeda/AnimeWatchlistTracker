package com.vuzeda.animewatchlist.tracker.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.domain.model.EpisodePage
import com.vuzeda.animewatchlist.tracker.domain.model.RelationType
import com.vuzeda.animewatchlist.tracker.domain.model.SequelInfo
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import com.vuzeda.animewatchlist.tracker.domain.usecase.AddAnimeToWatchlistUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.DeleteAnimeFromWatchlistUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.FetchAnimeByMalIdUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ObserveAnimeByIdUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.ToggleAnimeNotificationsUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.UpdateAnimeUseCase
import com.vuzeda.animewatchlist.tracker.ui.navigation.Route
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
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
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val observeAnimeByIdUseCase: ObserveAnimeByIdUseCase = mockk()
    private val fetchAnimeByMalIdUseCase: FetchAnimeByMalIdUseCase = mockk()
    private val updateAnimeUseCase: UpdateAnimeUseCase = mockk()
    private val deleteAnimeFromWatchlistUseCase: DeleteAnimeFromWatchlistUseCase = mockk()
    private val toggleAnimeNotificationsUseCase: ToggleAnimeNotificationsUseCase = mockk(relaxed = true)
    private val addAnimeToWatchlistUseCase: AddAnimeToWatchlistUseCase = mockk()
    private val animeRepository: AnimeRepository = mockk(relaxed = true)

    private val sampleAnime = Anime(
        id = 1L,
        malId = 21,
        title = "One Punch Man",
        status = WatchStatus.WATCHING,
        currentEpisode = 6,
        episodeCount = 12,
        userRating = 8,
        score = 8.7
    )

    private lateinit var animeFlow: MutableStateFlow<Anime?>

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        animeFlow = MutableStateFlow(sampleAnime)
        every { observeAnimeByIdUseCase(1L) } returns animeFlow
        coEvery { animeRepository.fetchAnimeEpisodes(any(), any()) } returns Result.success(
            EpisodePage(episodes = emptyList(), hasNextPage = false, nextPage = 2)
        )
        coEvery { animeRepository.fetchAnimeFullDetails(any()) } returns Result.success(
            AnimeFullDetails(malId = 21, episodes = 12, sequels = emptyList())
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(animeId: Long = 1L, malId: Int = 0): DetailViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                Route.Detail.ARG_ANIME_ID to animeId,
                Route.Detail.ARG_MAL_ID to malId
            )
        )
        return DetailViewModel(
            savedStateHandle = savedStateHandle,
            observeAnimeByIdUseCase = observeAnimeByIdUseCase,
            fetchAnimeByMalIdUseCase = fetchAnimeByMalIdUseCase,
            updateAnimeUseCase = updateAnimeUseCase,
            deleteAnimeFromWatchlistUseCase = deleteAnimeFromWatchlistUseCase,
            toggleAnimeNotificationsUseCase = toggleAnimeNotificationsUseCase,
            addAnimeToWatchlistUseCase = addAnimeToWatchlistUseCase,
            animeRepository = animeRepository
        )
    }

    @Test
    fun `loads anime on init`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(DetailUiState.Loading::class.java)

            val success = awaitItem()
            assertThat(success).isInstanceOf(DetailUiState.Success::class.java)
            assertThat((success as DetailUiState.Success).anime.title).isEqualTo("One Punch Man")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shows not found when anime does not exist`() = runTest {
        val emptyFlow = MutableStateFlow<Anime?>(null)
        every { observeAnimeByIdUseCase(999L) } returns emptyFlow

        val viewModel = createViewModel(animeId = 999L)

        viewModel.uiState.test {
            awaitItem()

            val notFound = awaitItem()
            assertThat(notFound).isInstanceOf(DetailUiState.NotFound::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `transitions to NotFound when anime is deleted`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            animeFlow.value = null

            val notFound = awaitItem()
            assertThat(notFound).isInstanceOf(DetailUiState.NotFound::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updates reactively when anime changes in database`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val initial = expectMostRecentItem() as DetailUiState.Success
            assertThat(initial.anime.status).isEqualTo(WatchStatus.WATCHING)

            animeFlow.value = sampleAnime.copy(status = WatchStatus.COMPLETED)

            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.anime.status).isEqualTo(WatchStatus.COMPLETED)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateStatus persists immediately`() = runTest {
        coEvery { updateAnimeUseCase(any()) } coAnswers {
            animeFlow.value = firstArg()
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.updateStatus(WatchStatus.COMPLETED)

            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.anime.status).isEqualTo(WatchStatus.COMPLETED)

            coVerify {
                updateAnimeUseCase(match { it.status == WatchStatus.COMPLETED })
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateCurrentEpisode persists immediately`() = runTest {
        coEvery { updateAnimeUseCase(any()) } coAnswers {
            animeFlow.value = firstArg()
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.updateCurrentEpisode(10)

            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.anime.currentEpisode).isEqualTo(10)

            coVerify {
                updateAnimeUseCase(match { it.currentEpisode == 10 })
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateCurrentEpisode does not go below zero`() = runTest {
        coEvery { updateAnimeUseCase(any()) } coAnswers {
            animeFlow.value = firstArg()
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.updateCurrentEpisode(-5)

            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.anime.currentEpisode).isEqualTo(0)

            coVerify {
                updateAnimeUseCase(match { it.currentEpisode == 0 })
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateUserRating persists immediately`() = runTest {
        coEvery { updateAnimeUseCase(any()) } coAnswers {
            animeFlow.value = firstArg()
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.updateUserRating(10)

            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.anime.userRating).isEqualTo(10)

            coVerify {
                updateAnimeUseCase(match { it.userRating == 10 })
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateUserRating clears rating when zero`() = runTest {
        coEvery { updateAnimeUseCase(any()) } coAnswers {
            animeFlow.value = firstArg()
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.updateUserRating(0)

            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.anime.userRating).isNull()

            coVerify {
                updateAnimeUseCase(match { it.userRating == null })
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showStatusSheet sets isStatusSheetVisible to true`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.showStatusSheet()
            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.isStatusSheetVisible).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismissStatusSheet sets isStatusSheetVisible to false`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.showStatusSheet()
            awaitItem()

            viewModel.dismissStatusSheet()
            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.isStatusSheetVisible).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteAnime calls use case and invokes callback`() = runTest {
        coEvery { deleteAnimeFromWatchlistUseCase(1L) } returns Unit

        val viewModel = createViewModel()
        var callbackInvoked = false

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.deleteAnime { callbackInvoked = true }
            testDispatcher.scheduler.advanceUntilIdle()

            assertThat(callbackInvoked).isTrue()
            coVerify { deleteAnimeFromWatchlistUseCase(1L) }
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
    fun `toggleNotifications state updates reactively`() = runTest {
        coEvery { toggleAnimeNotificationsUseCase(any(), any()) } coAnswers {
            val enabled = secondArg<Boolean>()
            animeFlow.value = animeFlow.value?.copy(isNotificationsEnabled = enabled)
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.toggleNotifications()

            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.isNotificationsEnabled).isTrue()
            assertThat(updated.anime.isNotificationsEnabled).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleNotifications disables when already enabled`() = runTest {
        animeFlow.value = sampleAnime.copy(isNotificationsEnabled = true)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.toggleNotifications()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { toggleAnimeNotificationsUseCase(id = 1L, enabled = false) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads anime from API when malId is provided`() = runTest {
        val apiAnime = Anime(
            malId = 50,
            title = "Spy x Family",
            score = 8.5,
            synopsis = "A spy forms a pretend family."
        )
        coEvery { fetchAnimeByMalIdUseCase(50) } returns Result.success(apiAnime)

        val viewModel = createViewModel(animeId = 0L, malId = 50)

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(DetailUiState.Loading::class.java)

            val success = awaitItem() as DetailUiState.Success
            assertThat(success.anime.title).isEqualTo("Spy x Family")
            assertThat(success.isInWatchlist).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shows not found when API fetch fails`() = runTest {
        coEvery { fetchAnimeByMalIdUseCase(999) } returns Result.failure(Exception("Not found"))

        val viewModel = createViewModel(animeId = 0L, malId = 999)

        viewModel.uiState.test {
            awaitItem()

            val notFound = awaitItem()
            assertThat(notFound).isInstanceOf(DetailUiState.NotFound::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shows not found when both animeId and malId are zero`() = runTest {
        val viewModel = createViewModel(animeId = 0L, malId = 0)

        viewModel.uiState.test {
            val notFound = awaitItem()
            assertThat(notFound).isInstanceOf(DetailUiState.NotFound::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showAddSheet sets isAddSheetVisible to true`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.showAddSheet()
            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.isAddSheetVisible).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismissAddSheet sets isAddSheetVisible to false`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.showAddSheet()
            awaitItem()

            viewModel.dismissAddSheet()
            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.isAddSheetVisible).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addToWatchlist calls use case and transitions to watchlist mode`() = runTest {
        val apiAnime = Anime(
            malId = 50,
            title = "Spy x Family",
            score = 8.5
        )
        coEvery { fetchAnimeByMalIdUseCase(50) } returns Result.success(apiAnime)
        coEvery { addAnimeToWatchlistUseCase(any()) } returns 99L

        val addedAnimeFlow = MutableStateFlow<Anime?>(apiAnime.copy(id = 99L, status = WatchStatus.WATCHING))
        every { observeAnimeByIdUseCase(99L) } returns addedAnimeFlow

        val viewModel = createViewModel(animeId = 0L, malId = 50)

        viewModel.uiState.test {
            awaitItem()
            val success = awaitItem() as DetailUiState.Success
            assertThat(success.isInWatchlist).isFalse()

            viewModel.addToWatchlist(WatchStatus.WATCHING)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { addAnimeToWatchlistUseCase(match { it.status == WatchStatus.WATCHING }) }

            val watchlistState = expectMostRecentItem() as DetailUiState.Success
            assertThat(watchlistState.isInWatchlist).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetches episodes on init for watchlist anime`() = runTest {
        val episodes = listOf(
            EpisodeInfo(number = 1, title = "Ep 1", aired = "2024-01-01", isFiller = false, isRecap = false)
        )
        coEvery { animeRepository.fetchAnimeEpisodes(malId = 21, page = 1) } returns Result.success(
            EpisodePage(episodes = episodes, hasNextPage = true, nextPage = 2)
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem() as DetailUiState.Success
            assertThat(state.episodes).hasSize(1)
            assertThat(state.episodes[0].number).isEqualTo(1)
            assertThat(state.hasMoreEpisodes).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMoreEpisodes appends to existing list`() = runTest {
        val page1 = listOf(
            EpisodeInfo(number = 1, title = "Ep 1", aired = null, isFiller = false, isRecap = false)
        )
        val page2 = listOf(
            EpisodeInfo(number = 2, title = "Ep 2", aired = null, isFiller = false, isRecap = false)
        )
        coEvery { animeRepository.fetchAnimeEpisodes(malId = 21, page = 1) } returns Result.success(
            EpisodePage(episodes = page1, hasNextPage = true, nextPage = 2)
        )
        coEvery { animeRepository.fetchAnimeEpisodes(malId = 21, page = 2) } returns Result.success(
            EpisodePage(episodes = page2, hasNextPage = false, nextPage = 3)
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)
            testDispatcher.scheduler.advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.loadMoreEpisodes()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as DetailUiState.Success
        assertThat(state.episodes).hasSize(2)
        assertThat(state.hasMoreEpisodes).isFalse()
    }

    @Test
    fun `fetches related anime on init`() = runTest {
        coEvery { animeRepository.fetchAnimeFullDetails(21) } returns Result.success(
            AnimeFullDetails(
                malId = 21,
                episodes = 12,
                sequels = listOf(SequelInfo(malId = 200, title = "Sequel")),
                prequels = listOf(SequelInfo(malId = 50, title = "Prequel"))
            )
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem() as DetailUiState.Success
            assertThat(state.relatedAnime).hasSize(2)
            assertThat(state.relatedAnime[0].relationType).isEqualTo(RelationType.PREQUEL)
            assertThat(state.relatedAnime[1].relationType).isEqualTo(RelationType.SEQUEL)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
