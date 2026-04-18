package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SetHomeNotificationFilterUseCaseTest {

    private val repository = mockk<UserPreferencesRepository>(relaxUnitFun = true)
    private val useCase = SetHomeNotificationFilterUseCase(repository)

    @Test
    fun `delegates to repository with true`() = runTest {
        useCase(true)

        coVerify(exactly = 1) { repository.setHomeNotificationFilter(true) }
    }

    @Test
    fun `delegates to repository with false`() = runTest {
        useCase(false)

        coVerify(exactly = 1) { repository.setHomeNotificationFilter(false) }
    }

    @Test
    fun `delegates to repository with null to clear filter`() = runTest {
        useCase(null)

        coVerify(exactly = 1) { repository.setHomeNotificationFilter(null) }
    }
}
