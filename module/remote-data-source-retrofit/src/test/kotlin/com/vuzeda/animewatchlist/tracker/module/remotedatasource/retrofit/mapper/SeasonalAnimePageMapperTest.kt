package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeDataDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeSearchResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.SearchPaginationDto
import org.junit.jupiter.api.Test

class SeasonalAnimePageMapperTest {

    @Test
    fun `toSeasonalAnimePage maps results and pagination correctly`() {
        val dto = AnimeSearchResponseDto(
            pagination = SearchPaginationDto(hasNextPage = true, lastVisiblePage = 3),
            data = listOf(
                AnimeDataDto(malId = 1, title = "Frieren"),
                AnimeDataDto(malId = 2, title = "Jujutsu Kaisen")
            )
        )

        val page = dto.toSeasonalAnimePage(currentPage = 1)

        assertThat(page.currentPage).isEqualTo(1)
        assertThat(page.hasNextPage).isTrue()
        assertThat(page.results).hasSize(2)
        assertThat(page.results[0].malId).isEqualTo(1)
        assertThat(page.results[0].title).isEqualTo("Frieren")
        assertThat(page.results[1].malId).isEqualTo(2)
    }

    @Test
    fun `toSeasonalAnimePage handles null pagination`() {
        val dto = AnimeSearchResponseDto(
            pagination = null,
            data = listOf(AnimeDataDto(malId = 1, title = "Test"))
        )

        val page = dto.toSeasonalAnimePage(currentPage = 2)

        assertThat(page.currentPage).isEqualTo(2)
        assertThat(page.hasNextPage).isFalse()
        assertThat(page.results).hasSize(1)
    }

    @Test
    fun `toSeasonalAnimePage handles empty data`() {
        val dto = AnimeSearchResponseDto(
            pagination = SearchPaginationDto(hasNextPage = false, lastVisiblePage = 1),
            data = emptyList()
        )

        val page = dto.toSeasonalAnimePage(currentPage = 1)

        assertThat(page.results).isEmpty()
        assertThat(page.hasNextPage).isFalse()
    }
}
