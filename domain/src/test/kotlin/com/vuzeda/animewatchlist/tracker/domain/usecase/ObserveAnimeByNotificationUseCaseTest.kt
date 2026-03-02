package com.vuzeda.animewatchlist.tracker.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.NotificationType
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveAnimeByNotificationUseCaseTest {

    private val repository = mockk<AnimeRepository>()
    private val useCase = ObserveAnimeByNotificationUseCase(repository)

    @Test
    fun `delegates to repository with enabled true`() = runTest {
        val anime = listOf(
            Anime(id = 1L, title = "Test", status = WatchStatus.WATCHING, notificationType = NotificationType.BOTH)
        )
        every { repository.observeByNotificationEnabled(true) } returns flowOf(anime)

        useCase(enabled = true).test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            assertThat(result[0].isNotificationsEnabled).isTrue()
            awaitComplete()
        }

        verify { repository.observeByNotificationEnabled(true) }
    }

    @Test
    fun `delegates to repository with enabled false`() = runTest {
        val anime = listOf(
            Anime(id = 2L, title = "Test 2", status = WatchStatus.WATCHING, notificationType = NotificationType.NONE)
        )
        every { repository.observeByNotificationEnabled(false) } returns flowOf(anime)

        useCase(enabled = false).test {
            val result = awaitItem()

            assertThat(result).hasSize(1)
            assertThat(result[0].isNotificationsEnabled).isFalse()
            awaitComplete()
        }

        verify { repository.observeByNotificationEnabled(false) }
    }
}
