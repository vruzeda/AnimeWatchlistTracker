package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SeasonTest {

    @Test
    fun `Season holds malId and title`() {
        val season = Season(malId = 5114, title = "FMA Brotherhood")

        assertThat(season.malId).isEqualTo(5114)
        assertThat(season.title).isEqualTo("FMA Brotherhood")
    }

    @Test
    fun `Season default values are correct`() {
        val season = Season(malId = 1, title = "Test")

        assertThat(season.id).isEqualTo(0L)
        assertThat(season.animeId).isEqualTo(0L)
        assertThat(season.type).isEqualTo("TV")
        assertThat(season.episodeCount).isNull()
        assertThat(season.watchedEpisodeCount).isEqualTo(0)
        assertThat(season.status).isEqualTo(WatchStatus.PLAN_TO_WATCH)
        assertThat(season.score).isNull()
        assertThat(season.orderIndex).isEqualTo(0)
        assertThat(season.airingStatus).isNull()
        assertThat(season.streamingLinks).isEmpty()
        assertThat(season.isEpisodeNotificationsEnabled).isFalse()
        assertThat(season.isInWatchlist).isTrue()
        assertThat(season.addedAt).isEqualTo(0L)
    }

    @Test
    fun `Season with all fields populated`() {
        val checkDate = LocalDate.of(2024, 1, 1)
        val season = Season(
            id = 1L,
            animeId = 2L,
            malId = 5114,
            title = "FMA Brotherhood",
            titleEnglish = "FMA Brotherhood",
            titleJapanese = "鋼の錬金術師",
            imageUrl = "https://example.com/fma.jpg",
            type = "TV",
            episodeCount = 64,
            watchedEpisodeCount = 32,
            status = WatchStatus.WATCHING,
            score = 9.1,
            orderIndex = 1,
            airingStatus = "Finished Airing",
            broadcastInfo = "Sundays",
            broadcastDay = "Sunday",
            broadcastTime = "17:00",
            broadcastTimezone = "Asia/Tokyo",
            streamingLinks = listOf(StreamingInfo("Crunchyroll", "https://crunchyroll.com")),
            lastCheckedAiredEpisodeCount = 64,
            latestKnownEpisodeAirDate = checkDate,
            isEpisodeNotificationsEnabled = true,
            isInWatchlist = true,
            airingSeasonName = "spring",
            airingSeasonYear = 2009,
            addedAt = 1700000000L
        )

        assertThat(season.watchedEpisodeCount).isEqualTo(32)
        assertThat(season.status).isEqualTo(WatchStatus.WATCHING)
        assertThat(season.isEpisodeNotificationsEnabled).isTrue()
        assertThat(season.latestKnownEpisodeAirDate).isEqualTo(checkDate)
        assertThat(season.airingSeasonYear).isEqualTo(2009)
    }

    @Test
    fun `Season isInWatchlist can be false`() {
        val season = Season(malId = 1, title = "Test", isInWatchlist = false)

        assertThat(season.isInWatchlist).isFalse()
    }

    @Test
    fun `two Season with same values are equal`() {
        val a = Season(malId = 1, title = "Test")
        val b = Season(malId = 1, title = "Test")

        assertThat(a).isEqualTo(b)
    }
}
