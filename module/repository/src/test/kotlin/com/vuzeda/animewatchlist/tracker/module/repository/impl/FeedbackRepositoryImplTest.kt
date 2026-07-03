package com.vuzeda.animewatchlist.tracker.module.repository.impl

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.DataError
import com.vuzeda.animewatchlist.tracker.module.domain.Feedback
import com.vuzeda.animewatchlist.tracker.module.domain.FeedbackCategory
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.FeedbackRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class FeedbackRepositoryImplTest {

    private val remoteDataSource: FeedbackRemoteDataSource = mockk()
    private val repository = FeedbackRepositoryImpl(remoteDataSource)

    private val feedback = Feedback(
        category = FeedbackCategory.BUG_REPORT,
        message = "The episode list stops loading after page two",
        appVersion = "1.6.0",
        timestamp = 1_751_500_000_000,
        deviceModel = "Pixel 8",
        androidVersion = 35,
        installationId = "install-123",
        titleLanguage = "ENGLISH",
        homeViewMode = "ANIME",
        contactEmail = "user@example.com"
    )

    @Test
    fun `submitFeedback delegates the feedback to the remote data source`() = runTest {
        coEvery { remoteDataSource.submit(feedback) } returns Result.success(Unit)

        val result = repository.submitFeedback(feedback)

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { remoteDataSource.submit(feedback) }
    }

    @Test
    fun `submitFeedback propagates remote failures unchanged`() = runTest {
        val failure = DataError.Network(throwable = IOException("offline"))
        coEvery { remoteDataSource.submit(feedback) } returns Result.failure(failure)

        val result = repository.submitFeedback(feedback)

        assertThat(result.exceptionOrNull()).isEqualTo(failure)
    }
}
