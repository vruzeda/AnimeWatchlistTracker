package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.HomeSortOption
import com.vuzeda.animewatchlist.tracker.module.domain.HomeSortState
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SetHomeSortStateUseCaseTest {

    private val repository = mockk<UserPreferencesRepository>(relaxUnitFun = true)
    private val useCase = SetHomeSortStateUseCase(repository)

    @Test
    fun `delegates to repository with given sort state`() = runTest {
        val state = HomeSortState(HomeSortOption.RECENTLY_ADDED, true)

        useCase(state)

        coVerify(exactly = 1) { repository.setHomeSortState(state) }
    }

    @Test
    fun `delegates to repository with default sort state`() = runTest {
        val state = HomeSortState()

        useCase(state)

        coVerify(exactly = 1) { repository.setHomeSortState(state) }
    }
}
