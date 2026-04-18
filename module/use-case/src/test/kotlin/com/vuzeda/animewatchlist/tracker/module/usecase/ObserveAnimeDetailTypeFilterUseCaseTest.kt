package com.vuzeda.animewatchlist.tracker.module.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.repository.UserPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveAnimeDetailTypeFilterUseCaseTest {

    private val repository = mockk<UserPreferencesRepository>()
    private val useCase = ObserveAnimeDetailTypeFilterUseCase(repository)

    @Test
    fun `returns empty set when no filter is stored`() = runTest {
        every { repository.observeAnimeDetailTypeFilter() } returns flowOf(emptySet())

        useCase().test {
            assertThat(awaitItem()).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `returns filter set when types are stored`() = runTest {
        val expected = setOf("TV", "OVA")
        every { repository.observeAnimeDetailTypeFilter() } returns flowOf(expected)

        useCase().test {
            assertThat(awaitItem()).containsExactlyElementsIn(expected)
            awaitComplete()
        }
    }
}
