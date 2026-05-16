package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.repository.CoverCacheRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetCoverCacheSizeUseCaseTest {

    private val coverCacheRepository: CoverCacheRepository = mockk()
    private val useCase = GetCoverCacheSizeUseCase(coverCacheRepository)

    @Test
    fun `returns cache size from repository`() = runTest {
        coEvery { coverCacheRepository.getCoverCacheSize() } returns 1_048_576L

        assertThat(useCase()).isEqualTo(1_048_576L)
    }

    @Test
    fun `returns zero when cache is empty`() = runTest {
        coEvery { coverCacheRepository.getCoverCacheSize() } returns 0L

        assertThat(useCase()).isEqualTo(0L)
    }
}
