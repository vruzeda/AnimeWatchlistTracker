package com.vuzeda.animewatchlist.tracker.ui.screens.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.usecase.DeleteAllDataUseCase
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
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val deleteAllDataUseCase: DeleteAllDataUseCase = mockk(relaxUnitFun = true)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has dialog hidden and data not deleted`() = runTest {
        val viewModel = SettingsViewModel(deleteAllDataUseCase)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.isDeleteConfirmationVisible).isFalse()
            assertThat(initial.isDataDeleted).isFalse()
        }
    }

    @Test
    fun `requestDeleteAllData shows confirmation dialog`() = runTest {
        val viewModel = SettingsViewModel(deleteAllDataUseCase)

        viewModel.uiState.test {
            awaitItem()

            viewModel.requestDeleteAllData()

            val updated = awaitItem()
            assertThat(updated.isDeleteConfirmationVisible).isTrue()
        }
    }

    @Test
    fun `dismissDeleteConfirmation hides dialog`() = runTest {
        val viewModel = SettingsViewModel(deleteAllDataUseCase)

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
        val viewModel = SettingsViewModel(deleteAllDataUseCase)

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
        val viewModel = SettingsViewModel(deleteAllDataUseCase)

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
}
