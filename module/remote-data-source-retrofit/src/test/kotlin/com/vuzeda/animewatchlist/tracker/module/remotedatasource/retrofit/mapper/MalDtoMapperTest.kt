package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AiringStatus
import com.vuzeda.animewatchlist.tracker.module.domain.BroadcastTime
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalAlternativeTitlesDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalAnimeDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalAnimeListResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalAnimeNodeWrapperDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalBroadcastDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalGenreDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalMainPictureDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalPagingDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalRelatedAnimeDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalRelatedNodeDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalStartSeasonDto
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId

class MalDtoMapperTest {

    private val fullAnime = MalAnimeDto(
        id = 52991,
        title = "Sousou no Frieren",
        alternativeTitles = MalAlternativeTitlesDto(en = "Frieren: Beyond Journey's End", ja = "葬送のフリーレン"),
        mainPicture = MalMainPictureDto(
            medium = "https://cdn.myanimelist.net/images/anime/1015/138006.jpg",
            large = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg"
        ),
        mediaType = "tv",
        status = "finished_airing",
        numEpisodes = 28,
        mean = 9.29,
        synopsis = "During their decade-long quest...",
        genres = listOf(MalGenreDto("Adventure"), MalGenreDto("Fantasy")),
        broadcast = MalBroadcastDto(dayOfTheWeek = "friday", startTime = "23:00"),
        startSeason = MalStartSeasonDto(year = 2023, season = "fall"),
        relatedAnime = listOf(
            MalRelatedAnimeDto(
                node = MalRelatedNodeDto(id = 59978, title = "Sousou no Frieren 2nd Season"),
                relationType = "sequel"
            ),
            MalRelatedAnimeDto(
                node = MalRelatedNodeDto(id = 56805, title = "Yuusha"),
                relationType = "other"
            )
        )
    )

    @Test
    fun `toAnimeFullDetails maps every populated field`() {
        val details = fullAnime.toAnimeFullDetails()

        assertThat(details.malId).isEqualTo(52991)
        assertThat(details.title).isEqualTo("Sousou no Frieren")
        assertThat(details.titleEnglish).isEqualTo("Frieren: Beyond Journey's End")
        assertThat(details.titleJapanese).isEqualTo("葬送のフリーレン")
        assertThat(details.imageUrl).isEqualTo("https://cdn.myanimelist.net/images/anime/1015/138006l.jpg")
        assertThat(details.type).isEqualTo("TV")
        assertThat(details.episodes).isEqualTo(28)
        assertThat(details.score).isEqualTo(9.29)
        assertThat(details.synopsis).isEqualTo("During their decade-long quest...")
        assertThat(details.genres).containsExactly("Adventure", "Fantasy").inOrder()
        assertThat(details.airingStatus).isEqualTo("Finished Airing")
        assertThat(details.airingSeasonName).isEqualTo("fall")
        assertThat(details.airingSeasonYear).isEqualTo(2023)
    }

    @Test
    fun `toAnimeFullDetails airing status round-trips through AiringStatus`() {
        val details = fullAnime.toAnimeFullDetails()

        assertThat(AiringStatus.fromDisplayName(details.airingStatus))
            .isEqualTo(AiringStatus.FINISHED_AIRING)
    }

    @Test
    fun `toAnimeFullDetails composes broadcast fields with JST timezone`() {
        val details = fullAnime.toAnimeFullDetails()

        assertThat(details.broadcastTime).isEqualTo(BroadcastTime(
            dayOfWeek = DayOfWeek.FRIDAY,
            time = LocalTime.of(23, 0),
            zoneId = ZoneId.of("Asia/Tokyo")
        ))
        assertThat(details.broadcastInfo).isEqualTo("Fridays at 23:00 (JST)")
    }

    @Test
    fun `toAnimeFullDetails maps day-only broadcast without time suffix`() {
        val details = fullAnime.copy(
            broadcast = MalBroadcastDto(dayOfTheWeek = "friday", startTime = null)
        ).toAnimeFullDetails()

        assertThat(details.broadcastInfo).isEqualTo("Fridays")
        assertThat(details.broadcastTime).isEqualTo(BroadcastTime(dayOfWeek = DayOfWeek.FRIDAY))
    }

    @Test
    fun `toAnimeFullDetails leaves broadcast fields null when absent`() {
        val details = fullAnime.copy(broadcast = null).toAnimeFullDetails()

        assertThat(details.broadcastInfo).isNull()
        assertThat(details.broadcastTime).isNull()
    }

    @Test
    fun `toAnimeFullDetails extracts sequels and prequels by relation type`() {
        val details = fullAnime.copy(
            relatedAnime = fullAnime.relatedAnime.orEmpty() + MalRelatedAnimeDto(
                node = MalRelatedNodeDto(id = 40, title = "Season 0"),
                relationType = "prequel"
            )
        ).toAnimeFullDetails()

        assertThat(details.sequels).hasSize(1)
        assertThat(details.sequels[0].malId).isEqualTo(59978)
        assertThat(details.sequels[0].title).isEqualTo("Sousou no Frieren 2nd Season")
        assertThat(details.prequels).hasSize(1)
        assertThat(details.prequels[0].malId).isEqualTo(40)
    }

    @Test
    fun `toAnimeFullDetails has no streaming links`() {
        assertThat(fullAnime.toAnimeFullDetails().streamingLinks).isEmpty()
    }

    @Test
    fun `toAnimeFullDetails maps minimal anime with defaults`() {
        val details = MalAnimeDto(id = 1, title = "Unknown Show").toAnimeFullDetails()

        assertThat(details.malId).isEqualTo(1)
        assertThat(details.title).isEqualTo("Unknown Show")
        assertThat(details.titleEnglish).isNull()
        assertThat(details.titleJapanese).isNull()
        assertThat(details.imageUrl).isNull()
        assertThat(details.type).isEqualTo("Unknown")
        assertThat(details.episodes).isNull()
        assertThat(details.airingStatus).isNull()
        assertThat(details.genres).isEmpty()
        assertThat(details.sequels).isEmpty()
        assertThat(details.prequels).isEmpty()
        assertThat(details.airingSeasonName).isNull()
        assertThat(details.airingSeasonYear).isNull()
    }

    @Test
    fun `toSearchResult treats zero episode count as unknown`() {
        val result = fullAnime.copy(numEpisodes = 0).toSearchResult()

        assertThat(result.episodeCount).isNull()
    }

    @Test
    fun `toSearchResult treats blank alternative titles as missing`() {
        val result = fullAnime.copy(
            alternativeTitles = MalAlternativeTitlesDto(en = "", ja = " ")
        ).toSearchResult()

        assertThat(result.titleEnglish).isNull()
        assertThat(result.titleJapanese).isNull()
    }

    @Test
    fun `toSearchResult falls back to medium picture when large is missing`() {
        val result = fullAnime.copy(
            mainPicture = MalMainPictureDto(medium = "medium.jpg", large = null)
        ).toSearchResult()

        assertThat(result.imageUrl).isEqualTo("medium.jpg")
    }

    @Test
    fun `toSearchResultPage deduplicates by malId and derives hasNextPage from paging`() {
        val response = MalAnimeListResponseDto(
            data = listOf(
                MalAnimeNodeWrapperDto(MalAnimeDto(id = 1, title = "First")),
                MalAnimeNodeWrapperDto(MalAnimeDto(id = 2, title = "Second")),
                MalAnimeNodeWrapperDto(MalAnimeDto(id = 1, title = "Duplicate"))
            ),
            paging = MalPagingDto(next = "https://api.myanimelist.net/v2/anime?offset=20")
        )

        val page = response.toSearchResultPage(currentPage = 1)

        assertThat(page.results).hasSize(2)
        assertThat(page.results[0].title).isEqualTo("First")
        assertThat(page.hasNextPage).isTrue()
        assertThat(page.currentPage).isEqualTo(1)
    }

    @Test
    fun `toSeasonalAnimePage reports last page when paging has no next link`() {
        val response = MalAnimeListResponseDto(
            data = listOf(MalAnimeNodeWrapperDto(MalAnimeDto(id = 1, title = "Only"))),
            paging = MalPagingDto(next = null)
        )

        val page = response.toSeasonalAnimePage(currentPage = 3)

        assertThat(page.results).hasSize(1)
        assertThat(page.hasNextPage).isFalse()
        assertThat(page.currentPage).isEqualTo(3)
    }
}
