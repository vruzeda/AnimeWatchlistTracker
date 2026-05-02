package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class AnimeFullDetailsTest {

    @Test
    fun `AnimeFullDetails holds all required fields`() {
        val sequel = SequelInfo(malId = 200, title = "Season 2")
        val details = AnimeFullDetails(
            malId = 1,
            title = "Cowboy Bebop",
            type = "TV",
            episodes = 26,
            sequels = listOf(sequel)
        )

        assertThat(details.malId).isEqualTo(1)
        assertThat(details.title).isEqualTo("Cowboy Bebop")
        assertThat(details.type).isEqualTo("TV")
        assertThat(details.episodes).isEqualTo(26)
        assertThat(details.sequels).containsExactly(sequel)
    }

    @Test
    fun `AnimeFullDetails optional fields default to null or empty`() {
        val details = AnimeFullDetails(
            malId = 1,
            title = "Cowboy Bebop",
            type = "TV",
            episodes = null,
            sequels = emptyList()
        )

        assertThat(details.titleEnglish).isNull()
        assertThat(details.titleJapanese).isNull()
        assertThat(details.imageUrl).isNull()
        assertThat(details.score).isNull()
        assertThat(details.synopsis).isNull()
        assertThat(details.genres).isEmpty()
        assertThat(details.airingStatus).isNull()
        assertThat(details.broadcastInfo).isNull()
        assertThat(details.broadcastDay).isNull()
        assertThat(details.broadcastTime).isNull()
        assertThat(details.broadcastTimezone).isNull()
        assertThat(details.streamingLinks).isEmpty()
        assertThat(details.prequels).isEmpty()
        assertThat(details.airingSeasonName).isNull()
        assertThat(details.airingSeasonYear).isNull()
    }

    @Test
    fun `AnimeFullDetails with all optional fields populated`() {
        val details = AnimeFullDetails(
            malId = 5114,
            title = "Fullmetal Alchemist: Brotherhood",
            titleEnglish = "FMA Brotherhood",
            titleJapanese = "鋼の錬金術師",
            imageUrl = "https://example.com/fma.jpg",
            type = "TV",
            episodes = 64,
            score = 9.1,
            synopsis = "Two brothers seek the Philosopher's Stone.",
            genres = listOf("Action", "Adventure"),
            airingStatus = "Finished Airing",
            broadcastInfo = "Sundays at 17:00",
            broadcastDay = "Sunday",
            broadcastTime = "17:00",
            broadcastTimezone = "Asia/Tokyo",
            streamingLinks = listOf(StreamingInfo("Crunchyroll", "https://crunchyroll.com")),
            sequels = listOf(SequelInfo(malId = 9999, title = "FMA S2")),
            prequels = listOf(SequelInfo(malId = 121, title = "FMA 2003")),
            airingSeasonName = "spring",
            airingSeasonYear = 2009
        )

        assertThat(details.titleEnglish).isEqualTo("FMA Brotherhood")
        assertThat(details.score).isEqualTo(9.1)
        assertThat(details.genres).containsExactly("Action", "Adventure")
        assertThat(details.streamingLinks).hasSize(1)
        assertThat(details.prequels).hasSize(1)
        assertThat(details.airingSeasonYear).isEqualTo(2009)
    }

    @Test
    fun `two AnimeFullDetails with same values are equal`() {
        val a = AnimeFullDetails(malId = 1, title = "Test", type = "TV", episodes = 12, sequels = emptyList())
        val b = AnimeFullDetails(malId = 1, title = "Test", type = "TV", episodes = 12, sequels = emptyList())

        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `SequelInfo holds malId and title`() {
        val sequel = SequelInfo(malId = 42, title = "The Sequel")

        assertThat(sequel.malId).isEqualTo(42)
        assertThat(sequel.title).isEqualTo("The Sequel")
    }

    @Test
    fun `two SequelInfo with same values are equal`() {
        val a = SequelInfo(malId = 1, title = "S2")
        val b = SequelInfo(malId = 1, title = "S2")

        assertThat(a).isEqualTo(b)
    }
}
