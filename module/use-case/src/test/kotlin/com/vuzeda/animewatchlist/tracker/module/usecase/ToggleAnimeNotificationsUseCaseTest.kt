package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ToggleAnimeNotificationsUseCaseTest {

    private val repository = mockk<AnimeRepository>()
    private val useCase = ToggleAnimeNotificationsUseCase(repository)

    @Test
    fun `sets notification type for given anime id`() = runTest {
        coEvery { repository.updateNotificationType(id = 1L, notificationType = NotificationType.BOTH) } returns Unit

        useCase(id = 1L, notificationType = NotificationType.BOTH)

        coVerify { repository.updateNotificationType(id = 1L, notificationType = NotificationType.BOTH) }
    }

    @Test
    fun `disables notifications by setting NONE`() = runTest {
        coEvery { repository.updateNotificationType(id = 5L, notificationType = NotificationType.NONE) } returns Unit

        useCase(id = 5L, notificationType = NotificationType.NONE)

        coVerify { repository.updateNotificationType(id = 5L, notificationType = NotificationType.NONE) }
    }
}
