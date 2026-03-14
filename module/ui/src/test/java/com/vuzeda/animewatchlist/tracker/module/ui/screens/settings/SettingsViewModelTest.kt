package com.vuzeda.animewatchlist.tracker.module.ui.screens.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.usecase.DeleteAllDataUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveHomeViewModeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetHomeViewModeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SetTitleLanguageUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val deleteAllDataUseCase: DeleteAllDataUseCase = mockk(relaxUnitFun = true)
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase = mockk()
    private val setTitleLanguageUseCase: SetTitleLanguageUseCase = mockk(relaxUnitFun = true)
    private val observeHomeViewModeUseCase: ObserveHomeViewModeUseCase = mockk()
    private val setHomeViewModeUseCase: SetHomeViewModeUseCase = mockk(relaxUnitFun = true)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.DEFAULT)
        every { observeHomeViewModeUseCase() } returns flowOf(HomeViewMode.ANIME)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SettingsViewModel(
        deleteAllDataUseCase,
        observeTitleLanguageUseCase,
        setTitleLanguageUseCase,
        observeHomeViewModeUseCase,
        setHomeViewModeUseCase
    )

    @Test
    fun `initial state has dialog hidden and data not deleted`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.isDeleteConfirmationVisible).isFalse()
            assertThat(initial.isDataDeleted).isFalse()
        }
    }

    @Test
    fun `requestDeleteAllData shows confirmation dialog`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.requestDeleteAllData()

            val updated = awaitItem()
            assertThat(updated.isDeleteConfirmationVisible).isTrue()
        }
    }

    @Test
    fun `dismissDeleteConfirmation hides dialog`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.requestDeleteAllData()
            awaitItem()

            viewModel.dismissDeleteConfirmation()

            val dismissed = awaitItem()
            assertThat(dismissed.isDeleteConfirmationVisible).isFalse()
        }
    }

    @Test
    fun `confirmDeleteAllData calls use case and sets data deleted flag`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.requestDeleteAllData()
            awaitItem()

            viewModel.confirmDeleteAllData()

            val dialogHidden = awaitItem()
            assertThat(dialogHidden.isDeleteConfirmationVisible).isFalse()

            val deleted = awaitItem()
            assertThat(deleted.isDataDeleted).isTrue()

            coVerify(exactly = 1) { deleteAllDataUseCase() }
        }
    }

    @Test
    fun `clearDataDeletedFlag resets the flag`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.requestDeleteAllData()
            awaitItem()

            viewModel.confirmDeleteAllData()
            awaitItem()
            awaitItem()

            viewModel.clearDataDeletedFlag()

            val cleared = awaitItem()
            assertThat(cleared.isDataDeleted).isFalse()
        }
    }

    @Test
    fun `setHomeViewMode delegates to use case`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.setHomeViewMode(HomeViewMode.SEASON)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { setHomeViewModeUseCase(HomeViewMode.SEASON) }
        }
    }

    @Test
    fun `observes home view mode from use case`() = runTest {
        every { observeHomeViewModeUseCase() } returns flowOf(HomeViewMode.SEASON)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()

            val updated = awaitItem()
            assertThat(updated.homeViewMode).isEqualTo(HomeViewMode.SEASON)
        }
    }
}
