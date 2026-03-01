package com.vuzeda.animewatchlist.tracker.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.DeleteAnimeUseCase
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
    private val updateAnimeUseCase: UpdateAnimeUseCase = mockk()
    private val deleteAnimeUseCase: DeleteAnimeUseCase = mockk()
    private val toggleAnimeNotificationsUseCase: ToggleAnimeNotificationsUseCase = mockk(relaxed = true)

    private val sampleAnime = Anime(
        id = 1L,
        title = "One Punch Man",
        status = WatchStatus.WATCHING,
        userRating = 8
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

    private fun createViewModel(animeId: Long = 1L): DetailViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                Route.Detail.ARG_ANIME_ID to animeId,
                Route.Detail.ARG_MAL_ID to 0
            )
        )
        return DetailViewModel(
            savedStateHandle = savedStateHandle,
            observeAnimeByIdUseCase = observeAnimeByIdUseCase,
            updateAnimeUseCase = updateAnimeUseCase,
            deleteAnimeUseCase = deleteAnimeUseCase,
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
    fun `shows not found when animeId is zero`() = runTest {
        val viewModel = createViewModel(animeId = 0L)

        viewModel.uiState.test {
            val notFound = awaitItem()
            assertThat(notFound).isInstanceOf(DetailUiState.NotFound::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
