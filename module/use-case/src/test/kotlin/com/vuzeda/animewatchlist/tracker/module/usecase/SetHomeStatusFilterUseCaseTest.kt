package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SetHomeStatusFilterUseCaseTest {

    private val repository = mockk<UserPreferencesRepository>(relaxUnitFun = true)
    private val useCase = SetHomeStatusFilterUseCase(repository)

    @Test
    fun `delegates to repository with single status set`() = runTest {
        useCase(setOf(WatchStatus.COMPLETED))

        coVerify(exactly = 1) { repository.setHomeStatusFilter(setOf(WatchStatus.COMPLETED)) }
    }

    @Test
    fun `delegates to repository with multiple statuses set`() = runTest {
        useCase(setOf(WatchStatus.WATCHING, WatchStatus.ON_HOLD))

        coVerify(exactly = 1) { repository.setHomeStatusFilter(setOf(WatchStatus.WATCHING, WatchStatus.ON_HOLD)) }
    }

    @Test
    fun `delegates to repository with empty set to clear filter`() = runTest {
        useCase(emptySet())

        coVerify(exactly = 1) { repository.setHomeStatusFilter(emptySet()) }
    }
}
