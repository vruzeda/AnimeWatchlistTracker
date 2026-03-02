package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ToggleSeasonEpisodeNotificationsUseCaseTest {

    private val repository = mockk<SeasonRepository>()
    private val useCase = ToggleSeasonEpisodeNotificationsUseCase(repository)

    @Test
    fun `enables episode notifications for given season id`() = runTest {
        coEvery { repository.toggleSeasonEpisodeNotifications(seasonId = 1L, enabled = true) } returns Unit

        useCase(seasonId = 1L, enabled = true)

        coVerify { repository.toggleSeasonEpisodeNotifications(seasonId = 1L, enabled = true) }
    }

    @Test
    fun `disables episode notifications for given season id`() = runTest {
        coEvery { repository.toggleSeasonEpisodeNotifications(seasonId = 5L, enabled = false) } returns Unit

        useCase(seasonId = 5L, enabled = false)

        coVerify { repository.toggleSeasonEpisodeNotifications(seasonId = 5L, enabled = false) }
    }
}
