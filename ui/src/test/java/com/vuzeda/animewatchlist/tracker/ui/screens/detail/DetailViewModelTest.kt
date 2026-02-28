package com.vuzeda.animewatchlist.tracker.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.usecase.DeleteAnimeFromWatchlistUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.GetAnimeByIdUseCase
import com.vuzeda.animewatchlist.tracker.domain.usecase.UpdateAnimeUseCase
import com.vuzeda.animewatchlist.tracker.ui.navigation.Route
import io.mockk.coEvery
import io.mockk.coVerify
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

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getAnimeByIdUseCase: GetAnimeByIdUseCase = mockk()
    private val updateAnimeUseCase: UpdateAnimeUseCase = mockk()
    private val deleteAnimeFromWatchlistUseCase: DeleteAnimeFromWatchlistUseCase = mockk()

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

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(animeId: Long = 1L): DetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf(Route.Detail.ARG_ANIME_ID to animeId))
        return DetailViewModel(
            savedStateHandle = savedStateHandle,
            getAnimeByIdUseCase = getAnimeByIdUseCase,
            updateAnimeUseCase = updateAnimeUseCase,
            deleteAnimeFromWatchlistUseCase = deleteAnimeFromWatchlistUseCase
        )
    }

    @Test
    fun `loads anime on init`() = runTest {
        coEvery { getAnimeByIdUseCase(1L) } returns sampleAnime

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
        coEvery { getAnimeByIdUseCase(999L) } returns null

        val viewModel = createViewModel(animeId = 999L)

        viewModel.uiState.test {
            awaitItem()

            val notFound = awaitItem()
            assertThat(notFound).isInstanceOf(DetailUiState.NotFound::class.java)
        }
    }

    @Test
    fun `toggleEditing switches editing state`() = runTest {
        coEvery { getAnimeByIdUseCase(1L) } returns sampleAnime

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.toggleEditing()
            val editing = awaitItem() as DetailUiState.Success
            assertThat(editing.isEditing).isTrue()

            viewModel.toggleEditing()
            val notEditing = awaitItem() as DetailUiState.Success
            assertThat(notEditing.isEditing).isFalse()
        }
    }

    @Test
    fun `updateStatus changes edit status`() = runTest {
        coEvery { getAnimeByIdUseCase(1L) } returns sampleAnime

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.updateStatus(WatchStatus.COMPLETED)
            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.editStatus).isEqualTo(WatchStatus.COMPLETED)
        }
    }

    @Test
    fun `updateCurrentEpisode changes edit episode`() = runTest {
        coEvery { getAnimeByIdUseCase(1L) } returns sampleAnime

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.updateCurrentEpisode(10)
            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.editCurrentEpisode).isEqualTo(10)
        }
    }

    @Test
    fun `updateCurrentEpisode does not go below zero`() = runTest {
        coEvery { getAnimeByIdUseCase(1L) } returns sampleAnime

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.updateCurrentEpisode(-5)
            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.editCurrentEpisode).isEqualTo(0)
        }
    }

    @Test
    fun `updateUserRating changes edit rating`() = runTest {
        coEvery { getAnimeByIdUseCase(1L) } returns sampleAnime

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.updateUserRating(10)
            val updated = awaitItem() as DetailUiState.Success
            assertThat(updated.editUserRating).isEqualTo(10)
        }
    }

    @Test
    fun `saveChanges persists and exits edit mode`() = runTest {
        coEvery { getAnimeByIdUseCase(1L) } returns sampleAnime
        coEvery { updateAnimeUseCase(any()) } returns Unit

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)

            viewModel.updateStatus(WatchStatus.COMPLETED)
            awaitItem()
            viewModel.updateCurrentEpisode(12)
            awaitItem()
            viewModel.updateUserRating(9)
            awaitItem()

            viewModel.saveChanges()

            val saved = awaitItem() as DetailUiState.Success
            assertThat(saved.isEditing).isFalse()
            assertThat(saved.anime.status).isEqualTo(WatchStatus.COMPLETED)
            assertThat(saved.anime.currentEpisode).isEqualTo(12)
            assertThat(saved.anime.userRating).isEqualTo(9)

            coVerify { updateAnimeUseCase(any()) }
        }
    }

    @Test
    fun `deleteAnime calls use case and invokes callback`() = runTest {
        coEvery { getAnimeByIdUseCase(1L) } returns sampleAnime
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
}
