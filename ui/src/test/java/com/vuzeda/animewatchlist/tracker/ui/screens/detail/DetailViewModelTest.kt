package com.vuzeda.animewatchlist.tracker.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
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
            toggleAnimeNotificationsUseCase = toggleAnimeNotificationsUseCase
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
        }
    }

    @Test
    fun `transitions to NotFound when anime is deleted`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            animeFlow.value = null

            val notFound = awaitItem()
            assertThat(notFound).isInstanceOf(DetailUiState.NotFound::class.java)
        }
    }

    @Test
    fun `updates reactively when anime changes in database`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            val initial = awaitItem() as DetailUiState.Success
            assertThat(initial.anime.status).isEqualTo(WatchStatus.WATCHING)

            animeFlow.value = sampleAnime.copy(status = WatchStatus.COMPLETED)

            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.anime.status).isEqualTo(WatchStatus.COMPLETED)
        }
    }

    @Test
    fun `updateStatus persists immediately`() = runTest {
        coEvery { updateAnimeUseCase(any()) } coAnswers {
            animeFlow.value = firstArg()
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.updateStatus(WatchStatus.COMPLETED)

            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.anime.status).isEqualTo(WatchStatus.COMPLETED)

            coVerify {
                updateAnimeUseCase(match { it.status == WatchStatus.COMPLETED })
            }
        }
    }

    @Test
    fun `updateCurrentEpisode persists immediately`() = runTest {
        coEvery { updateAnimeUseCase(any()) } coAnswers {
            animeFlow.value = firstArg()
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.updateCurrentEpisode(10)

            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.anime.currentEpisode).isEqualTo(10)

            coVerify {
                updateAnimeUseCase(match { it.currentEpisode == 10 })
            }
        }
    }

    @Test
    fun `updateCurrentEpisode does not go below zero`() = runTest {
        coEvery { updateAnimeUseCase(any()) } coAnswers {
            animeFlow.value = firstArg()
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.updateCurrentEpisode(-5)

            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.anime.currentEpisode).isEqualTo(0)

            coVerify {
                updateAnimeUseCase(match { it.currentEpisode == 0 })
            }
        }
    }

    @Test
    fun `updateUserRating persists immediately`() = runTest {
        coEvery { updateAnimeUseCase(any()) } coAnswers {
            animeFlow.value = firstArg()
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.updateUserRating(10)

            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.anime.userRating).isEqualTo(10)

            coVerify {
                updateAnimeUseCase(match { it.userRating == 10 })
            }
        }
    }

    @Test
    fun `updateUserRating clears rating when zero`() = runTest {
        coEvery { updateAnimeUseCase(any()) } coAnswers {
            animeFlow.value = firstArg()
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.updateUserRating(0)

            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.anime.userRating).isNull()

            coVerify {
                updateAnimeUseCase(match { it.userRating == null })
            }
        }
    }

    @Test
    fun `showStatusSheet sets isStatusSheetVisible to true`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.showStatusSheet()
            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.isStatusSheetVisible).isTrue()
        }
    }

    @Test
    fun `dismissStatusSheet sets isStatusSheetVisible to false`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.showStatusSheet()
            awaitItem()

            viewModel.dismissStatusSheet()
            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.isStatusSheetVisible).isFalse()
        }
    }

    @Test
    fun `deleteAnime calls use case and invokes callback`() = runTest {
        coEvery { deleteAnimeFromWatchlistUseCase(1L) } returns Unit

        val viewModel = createViewModel()
        var callbackInvoked = false

        viewModel.uiState.test {
            skipItems(2)

            viewModel.deleteAnime { callbackInvoked = true }
            testDispatcher.scheduler.advanceUntilIdle()

            assertThat(callbackInvoked).isTrue()
            coVerify { deleteAnimeFromWatchlistUseCase(1L) }
        }
    }

    @Test
    fun `toggleNotifications delegates to use case`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.toggleNotifications()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { toggleAnimeNotificationsUseCase(id = 1L, enabled = true) }
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
            skipItems(2)

            viewModel.toggleNotifications()

            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.isNotificationsEnabled).isTrue()
            assertThat(updated.anime.isNotificationsEnabled).isTrue()
        }
    }

    @Test
    fun `toggleNotifications disables when already enabled`() = runTest {
        animeFlow.value = sampleAnime.copy(isNotificationsEnabled = true)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.toggleNotifications()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { toggleAnimeNotificationsUseCase(id = 1L, enabled = false) }
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
        }
    }

    @Test
    fun `shows not found when both animeId and malId are zero`() = runTest {
        val viewModel = createViewModel(animeId = 0L, malId = 0)

        viewModel.uiState.test {
            val notFound = awaitItem()
            assertThat(notFound).isInstanceOf(DetailUiState.NotFound::class.java)
        }
    }
}
