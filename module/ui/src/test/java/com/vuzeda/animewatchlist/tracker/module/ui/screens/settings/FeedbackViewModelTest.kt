package com.vuzeda.animewatchlist.tracker.module.ui.screens.settings

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsTracker
import com.vuzeda.animewatchlist.tracker.module.domain.Feedback
import com.vuzeda.animewatchlist.tracker.module.domain.FeedbackCategory
import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveHomeViewModeUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.ObserveTitleLanguageUseCase
import com.vuzeda.animewatchlist.tracker.module.usecase.SubmitFeedbackUseCase
import io.mockk.coEvery
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
class FeedbackViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val context: Context = mockk()
    private val packageManager: PackageManager = mockk()
    private val submitFeedbackUseCase: SubmitFeedbackUseCase = mockk()
    private val observeTitleLanguageUseCase: ObserveTitleLanguageUseCase = mockk()
    private val observeHomeViewModeUseCase: ObserveHomeViewModeUseCase = mockk()
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { observeTitleLanguageUseCase() } returns flowOf(TitleLanguage.DEFAULT)
        every { observeHomeViewModeUseCase() } returns flowOf(HomeViewMode.ANIME)
        every { context.packageName } returns "com.test"
        every { context.packageManager } returns packageManager
        @Suppress("DEPRECATION")
        every { packageManager.getPackageInfo("com.test", 0) } returns PackageInfo().apply {
            versionName = "1.0"
            versionCode = 1
        }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = FeedbackViewModel(
        context = context,
        submitFeedbackUseCase = submitFeedbackUseCase,
        observeTitleLanguageUseCase = observeTitleLanguageUseCase,
        observeHomeViewModeUseCase = observeHomeViewModeUseCase,
        analyticsTracker = analyticsTracker
    )

    @Test
    fun `initial state has no category and empty message`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.category).isNull()
            assertThat(state.message).isEmpty()
            assertThat(state.isSubmitting).isFalse()
            assertThat(state.snackbarEvent).isNull()
            assertThat(state.isValid).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectCategory updates category in state`() = runTest {
        val viewModel = createViewModel()

        viewModel.selectCategory(FeedbackCategory.BUG_REPORT.name)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.category).isEqualTo(FeedbackCategory.BUG_REPORT.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateMessage updates message in state`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateMessage("This is my feedback message")

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.message).isEqualTo("This is my feedback message")
            assertThat(state.charCount).isEqualTo(27)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isValid is true when category set and message is 10 to 500 chars`() = runTest {
        val viewModel = createViewModel()

        viewModel.selectCategory(FeedbackCategory.GENERAL.name)
        viewModel.updateMessage("A".repeat(10))

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isValid).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isValid is false when message is too short`() = runTest {
        val viewModel = createViewModel()

        viewModel.selectCategory(FeedbackCategory.GENERAL.name)
        viewModel.updateMessage("short")

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isValid).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `submitFeedback emits success event on successful submission`() = runTest {
        coEvery { submitFeedbackUseCase(any<Feedback>()) } returns Result.success(Unit)
        val viewModel = createViewModel()
        viewModel.selectCategory(FeedbackCategory.BUG_REPORT.name)
        viewModel.updateMessage("A".repeat(20))

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.submitFeedback()
            testDispatcher.scheduler.advanceUntilIdle()

            val submitting = awaitItem()
            assertThat(submitting.isSubmitting).isTrue()

            val done = awaitItem()
            assertThat(done.isSubmitting).isFalse()
            assertThat(done.snackbarEvent).isEqualTo(FeedbackSnackbarEvent.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `submitFeedback emits error event on failed submission`() = runTest {
        coEvery { submitFeedbackUseCase(any<Feedback>()) } returns Result.failure(Exception("network error"))
        val viewModel = createViewModel()
        viewModel.selectCategory(FeedbackCategory.FEATURE_REQUEST.name)
        viewModel.updateMessage("A".repeat(20))

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.submitFeedback()
            testDispatcher.scheduler.advanceUntilIdle()

            val submitting = awaitItem()
            assertThat(submitting.isSubmitting).isTrue()

            val done = awaitItem()
            assertThat(done.isSubmitting).isFalse()
            assertThat(done.snackbarEvent).isEqualTo(FeedbackSnackbarEvent.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `submitFeedback does nothing when category is null`() = runTest {
        val viewModel = createViewModel()
        viewModel.updateMessage("A".repeat(20))

        viewModel.uiState.test {
            awaitItem()

            viewModel.submitFeedback()
            testDispatcher.scheduler.advanceUntilIdle()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearSnackbarEvent clears the event`() = runTest {
        coEvery { submitFeedbackUseCase(any<Feedback>()) } returns Result.success(Unit)
        val viewModel = createViewModel()
        viewModel.selectCategory(FeedbackCategory.GENERAL.name)
        viewModel.updateMessage("A".repeat(20))

        viewModel.uiState.test {
            awaitItem()
            viewModel.submitFeedback()
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // isSubmitting = true
            awaitItem() // success event

            viewModel.clearSnackbarEvent()
            val cleared = awaitItem()
            assertThat(cleared.snackbarEvent).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reset restores initial state`() = runTest {
        val viewModel = createViewModel()
        viewModel.selectCategory(FeedbackCategory.BUG_REPORT.name)
        viewModel.updateMessage("Some message here")

        viewModel.uiState.test {
            awaitItem()

            viewModel.reset()
            val state = awaitItem()
            assertThat(state.category).isNull()
            assertThat(state.message).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
