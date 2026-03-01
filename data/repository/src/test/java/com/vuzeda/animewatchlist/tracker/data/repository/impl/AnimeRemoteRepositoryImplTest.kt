package com.vuzeda.animewatchlist.tracker.data.repository.impl

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeDataDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeSearchResponseDto
import com.vuzeda.animewatchlist.tracker.data.api.service.ChiakiService
import com.vuzeda.animewatchlist.tracker.data.api.service.JikanApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AnimeRemoteRepositoryImplTest {

    private val jikanApiService: JikanApiService = mockk()
    private val chiakiService: ChiakiService = mockk()
    private val repository = AnimeRemoteRepositoryImpl(jikanApiService, chiakiService)

    @Test
    fun `searchAnime deduplicates results by malId`() = runTest {
        val duplicatedData = listOf(
            AnimeDataDto(malId = 1, title = "Naruto"),
            AnimeDataDto(malId = 2, title = "Bleach"),
            AnimeDataDto(malId = 1, title = "Naruto")
        )
        coEvery { jikanApiService.searchAnime(query = "naruto") } returns
            AnimeSearchResponseDto(data = duplicatedData)

        val result = repository.searchAnime("naruto").getOrThrow()

        assertThat(result).hasSize(2)
        assertThat(result[0].malId).isEqualTo(1)
        assertThat(result[1].malId).isEqualTo(2)
    }

    @Test
    fun `searchAnime returns all results when no duplicates`() = runTest {
        val uniqueData = listOf(
            AnimeDataDto(malId = 1, title = "Naruto"),
            AnimeDataDto(malId = 2, title = "Bleach"),
            AnimeDataDto(malId = 3, title = "One Piece")
        )
        coEvery { jikanApiService.searchAnime(query = "anime") } returns
            AnimeSearchResponseDto(data = uniqueData)

        val result = repository.searchAnime("anime").getOrThrow()

        assertThat(result).hasSize(3)
    }

    @Test
    fun `searchAnime keeps first occurrence when duplicates exist`() = runTest {
        val duplicatedData = listOf(
            AnimeDataDto(malId = 1, title = "Naruto Original"),
            AnimeDataDto(malId = 1, title = "Naruto Duplicate")
        )
        coEvery { jikanApiService.searchAnime(query = "naruto") } returns
            AnimeSearchResponseDto(data = duplicatedData)

        val result = repository.searchAnime("naruto").getOrThrow()

        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("Naruto Original")
    }
}
