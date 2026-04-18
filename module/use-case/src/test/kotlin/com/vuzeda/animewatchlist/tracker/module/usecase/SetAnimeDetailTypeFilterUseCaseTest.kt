package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SetAnimeDetailTypeFilterUseCaseTest {

    private val repository = mockk<UserPreferencesRepository>(relaxUnitFun = true)
    private val useCase = SetAnimeDetailTypeFilterUseCase(repository)

    @Test
    fun `delegates to repository with given filter set`() = runTest {
        useCase(setOf("TV", "OVA"))

        coVerify(exactly = 1) { repository.setAnimeDetailTypeFilter(setOf("TV", "OVA")) }
    }

    @Test
    fun `delegates to repository with empty set to clear filter`() = runTest {
        useCase(emptySet())

        coVerify(exactly = 1) { repository.setAnimeDetailTypeFilter(emptySet()) }
    }
}
