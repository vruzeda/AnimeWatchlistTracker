package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeEpisodesResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.EpisodeDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.EpisodesPaginationDto
import org.junit.jupiter.api.Test

class EpisodeDtoMapperTest {

    @Test
    fun `maps EpisodeDto fields to EpisodeInfo`() {
        val dto = EpisodeDto(
            malId = 5,
            title = "The Battle Begins",
            aired = "2024-02-10T00:00:00+00:00",
            filler = true,
            recap = false
        )

        val info = dto.toEpisodeInfo()

        assertThat(info.number).isEqualTo(5)
        assertThat(info.title).isEqualTo("The Battle Begins")
        assertThat(info.aired).isEqualTo("2024-02-10T00:00:00+00:00")
        assertThat(info.isFiller).isTrue()
        assertThat(info.isRecap).isFalse()
    }

    @Test
    fun `maps null title and aired`() {
        val dto = EpisodeDto(malId = 1)

        val info = dto.toEpisodeInfo()

        assertThat(info.title).isNull()
        assertThat(info.aired).isNull()
        assertThat(info.isFiller).isFalse()
        assertThat(info.isRecap).isFalse()
    }

    @Test
    fun `maps response to EpisodePage with next page`() {
        val response = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 3, hasNextPage = true),
            data = listOf(
                EpisodeDto(malId = 1, title = "Ep 1"),
                EpisodeDto(malId = 2, title = "Ep 2")
            )
        )

        val page = response.toEpisodePage(currentPage = 1)

        assertThat(page.episodes).hasSize(2)
        assertThat(page.episodes[0].number).isEqualTo(1)
        assertThat(page.episodes[1].number).isEqualTo(2)
        assertThat(page.hasNextPage).isTrue()
        assertThat(page.nextPage).isEqualTo(2)
    }

    @Test
    fun `maps last page correctly`() {
        val response = AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(lastVisiblePage = 2, hasNextPage = false),
            data = listOf(EpisodeDto(malId = 26, title = "Final"))
        )

        val page = response.toEpisodePage(currentPage = 2)

        assertThat(page.hasNextPage).isFalse()
        assertThat(page.nextPage).isEqualTo(3)
    }
}
