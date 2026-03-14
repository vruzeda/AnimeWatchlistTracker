package com.vuzeda.animewatchlist.tracker.module.repository.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.localdatasource.UserPreferencesLocalDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UserPreferencesRepositoryImplTest {

    private val dataSource: UserPreferencesLocalDataSource = mockk()
    private val repository = UserPreferencesRepositoryImpl(dataSource)

    @Test
    fun `observeTitleLanguage returns matching TitleLanguage for valid stored value`() = runTest {
        every { dataSource.observeTitleLanguage() } returns flowOf("ENGLISH")

        repository.observeTitleLanguage().test {
            assertThat(awaitItem()).isEqualTo(TitleLanguage.ENGLISH)
            awaitComplete()
        }
    }

    @Test
    fun `observeTitleLanguage returns DEFAULT for unknown stored value`() = runTest {
        every { dataSource.observeTitleLanguage() } returns flowOf("UNKNOWN_VALUE")

        repository.observeTitleLanguage().test {
            assertThat(awaitItem()).isEqualTo(TitleLanguage.DEFAULT)
            awaitComplete()
        }
    }

    @Test
    fun `observeTitleLanguage returns all TitleLanguage variants correctly`() = runTest {
        TitleLanguage.entries.forEach { language ->
            every { dataSource.observeTitleLanguage() } returns flowOf(language.name)

            repository.observeTitleLanguage().test {
                assertThat(awaitItem()).isEqualTo(language)
                awaitComplete()
            }
        }
    }

    @Test
    fun `setTitleLanguage delegates to data source with enum name`() = runTest {
        coEvery { dataSource.setTitleLanguage(any()) } returns Unit

        repository.setTitleLanguage(TitleLanguage.JAPANESE)

        coVerify { dataSource.setTitleLanguage("JAPANESE") }
    }

    @Test
    fun `observeHomeViewMode returns matching HomeViewMode for valid stored value`() = runTest {
        every { dataSource.observeHomeViewMode() } returns flowOf("SEASON")

        repository.observeHomeViewMode().test {
            assertThat(awaitItem()).isEqualTo(HomeViewMode.SEASON)
            awaitComplete()
        }
    }

    @Test
    fun `observeHomeViewMode returns ANIME for unknown stored value`() = runTest {
        every { dataSource.observeHomeViewMode() } returns flowOf("UNKNOWN_VALUE")

        repository.observeHomeViewMode().test {
            assertThat(awaitItem()).isEqualTo(HomeViewMode.ANIME)
            awaitComplete()
        }
    }

    @Test
    fun `observeHomeViewMode returns all HomeViewMode variants correctly`() = runTest {
        HomeViewMode.entries.forEach { mode ->
            every { dataSource.observeHomeViewMode() } returns flowOf(mode.name)

            repository.observeHomeViewMode().test {
                assertThat(awaitItem()).isEqualTo(mode)
                awaitComplete()
            }
        }
    }

    @Test
    fun `setHomeViewMode delegates to data source with enum name`() = runTest {
        coEvery { dataSource.setHomeViewMode(any()) } returns Unit

        repository.setHomeViewMode(HomeViewMode.SEASON)

        coVerify { dataSource.setHomeViewMode("SEASON") }
    }
}
