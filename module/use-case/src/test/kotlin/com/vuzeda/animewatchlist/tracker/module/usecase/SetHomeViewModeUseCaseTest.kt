package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SetHomeViewModeUseCaseTest {

    private val repository = mockk<UserPreferencesRepository>(relaxUnitFun = true)
    private val useCase = SetHomeViewModeUseCase(repository)

    @Test
    fun `delegates to repository with SEASON mode`() = runTest {
        useCase(HomeViewMode.SEASON)

        coVerify(exactly = 1) { repository.setHomeViewMode(HomeViewMode.SEASON) }
    }

    @Test
    fun `delegates to repository with ANIME mode`() = runTest {
        useCase(HomeViewMode.ANIME)

        coVerify(exactly = 1) { repository.setHomeViewMode(HomeViewMode.ANIME) }
    }
}
