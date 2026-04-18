package com.vuzeda.animewatchlist.tracker.module.repository.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.HomeSortOption
import com.vuzeda.animewatchlist.tracker.module.domain.HomeSortState
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
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

    @Test
    fun `observeIsDeveloperOptionsEnabled returns true when data source emits true`() = runTest {
        every { dataSource.observeIsDeveloperOptionsEnabled() } returns flowOf(true)

        repository.observeIsDeveloperOptionsEnabled().test {
            assertThat(awaitItem()).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `observeIsDeveloperOptionsEnabled returns false when data source emits false`() = runTest {
        every { dataSource.observeIsDeveloperOptionsEnabled() } returns flowOf(false)

        repository.observeIsDeveloperOptionsEnabled().test {
            assertThat(awaitItem()).isFalse()
            awaitComplete()
        }
    }

    @Test
    fun `setIsDeveloperOptionsEnabled delegates to data source`() = runTest {
        coEvery { dataSource.setIsDeveloperOptionsEnabled(any()) } returns Unit

        repository.setIsDeveloperOptionsEnabled(true)

        coVerify { dataSource.setIsDeveloperOptionsEnabled(true) }
    }

    @Test
    fun `observeIsNotificationDebugInfoEnabled returns true when data source emits true`() = runTest {
        every { dataSource.observeIsNotificationDebugInfoEnabled() } returns flowOf(true)

        repository.observeIsNotificationDebugInfoEnabled().test {
            assertThat(awaitItem()).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `observeIsNotificationDebugInfoEnabled returns false when data source emits false`() = runTest {
        every { dataSource.observeIsNotificationDebugInfoEnabled() } returns flowOf(false)

        repository.observeIsNotificationDebugInfoEnabled().test {
            assertThat(awaitItem()).isFalse()
            awaitComplete()
        }
    }

    @Test
    fun `setIsNotificationDebugInfoEnabled delegates to data source`() = runTest {
        coEvery { dataSource.setIsNotificationDebugInfoEnabled(any()) } returns Unit

        repository.setIsNotificationDebugInfoEnabled(true)

        coVerify { dataSource.setIsNotificationDebugInfoEnabled(true) }
    }

    @Test
    fun `observeHomeSortState returns ALPHABETICAL ascending for default stored value`() = runTest {
        every { dataSource.observeHomeSortState() } returns flowOf("ALPHABETICAL:true")

        repository.observeHomeSortState().test {
            assertThat(awaitItem()).isEqualTo(HomeSortState(HomeSortOption.ALPHABETICAL, true))
            awaitComplete()
        }
    }

    @Test
    fun `observeHomeSortState returns correct state for RECENTLY_ADDED descending`() = runTest {
        every { dataSource.observeHomeSortState() } returns flowOf("RECENTLY_ADDED:false")

        repository.observeHomeSortState().test {
            assertThat(awaitItem()).isEqualTo(HomeSortState(HomeSortOption.RECENTLY_ADDED, false))
            awaitComplete()
        }
    }

    @Test
    fun `observeHomeSortState falls back to ALPHABETICAL for unknown option`() = runTest {
        every { dataSource.observeHomeSortState() } returns flowOf("UNKNOWN:true")

        repository.observeHomeSortState().test {
            val result = awaitItem()
            assertThat(result.option).isEqualTo(HomeSortOption.ALPHABETICAL)
            awaitComplete()
        }
    }

    @Test
    fun `setHomeSortState serialises option and ascending to data source`() = runTest {
        coEvery { dataSource.setHomeSortState(any()) } returns Unit

        repository.setHomeSortState(HomeSortState(HomeSortOption.USER_RATING, false))

        coVerify { dataSource.setHomeSortState("USER_RATING:false") }
    }

    @Test
    fun `observeHomeStatusFilter returns null for empty stored value`() = runTest {
        every { dataSource.observeHomeStatusFilter() } returns flowOf("")

        repository.observeHomeStatusFilter().test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `observeHomeStatusFilter returns matching WatchStatus for valid stored value`() = runTest {
        every { dataSource.observeHomeStatusFilter() } returns flowOf("WATCHING")

        repository.observeHomeStatusFilter().test {
            assertThat(awaitItem()).isEqualTo(WatchStatus.WATCHING)
            awaitComplete()
        }
    }

    @Test
    fun `observeHomeStatusFilter returns all WatchStatus variants correctly`() = runTest {
        WatchStatus.entries.forEach { status ->
            every { dataSource.observeHomeStatusFilter() } returns flowOf(status.name)

            repository.observeHomeStatusFilter().test {
                assertThat(awaitItem()).isEqualTo(status)
                awaitComplete()
            }
        }
    }

    @Test
    fun `setHomeStatusFilter delegates enum name to data source`() = runTest {
        coEvery { dataSource.setHomeStatusFilter(any()) } returns Unit

        repository.setHomeStatusFilter(WatchStatus.COMPLETED)

        coVerify { dataSource.setHomeStatusFilter("COMPLETED") }
    }

    @Test
    fun `setHomeStatusFilter delegates empty string to data source when null`() = runTest {
        coEvery { dataSource.setHomeStatusFilter(any()) } returns Unit

        repository.setHomeStatusFilter(null)

        coVerify { dataSource.setHomeStatusFilter("") }
    }

    @Test
    fun `observeHomeNotificationFilter returns null for empty stored value`() = runTest {
        every { dataSource.observeHomeNotificationFilter() } returns flowOf("")

        repository.observeHomeNotificationFilter().test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `observeHomeNotificationFilter returns true for stored true`() = runTest {
        every { dataSource.observeHomeNotificationFilter() } returns flowOf("true")

        repository.observeHomeNotificationFilter().test {
            assertThat(awaitItem()).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `observeHomeNotificationFilter returns false for stored false`() = runTest {
        every { dataSource.observeHomeNotificationFilter() } returns flowOf("false")

        repository.observeHomeNotificationFilter().test {
            assertThat(awaitItem()).isFalse()
            awaitComplete()
        }
    }

    @Test
    fun `setHomeNotificationFilter delegates true string to data source`() = runTest {
        coEvery { dataSource.setHomeNotificationFilter(any()) } returns Unit

        repository.setHomeNotificationFilter(true)

        coVerify { dataSource.setHomeNotificationFilter("true") }
    }

    @Test
    fun `setHomeNotificationFilter delegates empty string to data source when null`() = runTest {
        coEvery { dataSource.setHomeNotificationFilter(any()) } returns Unit

        repository.setHomeNotificationFilter(null)

        coVerify { dataSource.setHomeNotificationFilter("") }
    }
}
