package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ToggleAnimeNotificationsUseCaseTest {

    private val repository = mockk<AnimeRepository>()
    private val useCase = ToggleAnimeNotificationsUseCase(repository)

    @Test
    fun `enables notifications for given anime id`() = runTest {
        coEvery { repository.toggleNotifications(id = 1L, enabled = true) } returns Unit

        useCase(id = 1L, enabled = true)

        coVerify { repository.toggleNotifications(id = 1L, enabled = true) }
    }

    @Test
    fun `disables notifications for given anime id`() = runTest {
        coEvery { repository.toggleNotifications(id = 5L, enabled = false) } returns Unit

        useCase(id = 5L, enabled = false)

        coVerify { repository.toggleNotifications(id = 5L, enabled = false) }
    }
}
