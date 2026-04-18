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
    fun `delegates to repository with given status`() = runTest {
        useCase(WatchStatus.COMPLETED)

        coVerify(exactly = 1) { repository.setHomeStatusFilter(WatchStatus.COMPLETED) }
    }

    @Test
    fun `delegates to repository with null to clear filter`() = runTest {
        useCase(null)

        coVerify(exactly = 1) { repository.setHomeStatusFilter(null) }
    }
}
