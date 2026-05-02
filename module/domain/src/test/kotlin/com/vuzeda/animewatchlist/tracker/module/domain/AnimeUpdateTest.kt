package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class AnimeUpdateTest {

    private val anime = Anime(id = 1L, title = "Fullmetal Alchemist")
    private val season = Season(id = 1L, animeId = 1L, malId = 5114, title = "FMA: Brotherhood")

    @Test
    fun `NewEpisodes holds anime, season, and newEpisodeCount`() {
        val update = AnimeUpdate.NewEpisodes(
            anime = anime,
            season = season,
            newEpisodeCount = 3
        )

        assertThat(update.anime).isEqualTo(anime)
        assertThat(update.season).isEqualTo(season)
        assertThat(update.newEpisodeCount).isEqualTo(3)
    }

    @Test
    fun `NewSeason holds anime and sequel info`() {
        val update = AnimeUpdate.NewSeason(
            anime = anime,
            sequelMalId = 9999,
            sequelTitle = "FMA: Season 2",
            sequelTitleEnglish = "FMA: Season 2 English",
            sequelTitleJapanese = "鋼の錬金術師 Season 2"
        )

        assertThat(update.anime).isEqualTo(anime)
        assertThat(update.sequelMalId).isEqualTo(9999)
        assertThat(update.sequelTitle).isEqualTo("FMA: Season 2")
        assertThat(update.sequelTitleEnglish).isEqualTo("FMA: Season 2 English")
        assertThat(update.sequelTitleJapanese).isEqualTo("鋼の錬金術師 Season 2")
    }

    @Test
    fun `NewSeason nullable title fields default to null`() {
        val update = AnimeUpdate.NewSeason(
            anime = anime,
            sequelMalId = 9999,
            sequelTitle = "FMA: Season 2"
        )

        assertThat(update.sequelTitleEnglish).isNull()
        assertThat(update.sequelTitleJapanese).isNull()
    }

    @Test
    fun `two NewEpisodes with same values are equal`() {
        val a = AnimeUpdate.NewEpisodes(anime = anime, season = season, newEpisodeCount = 2)
        val b = AnimeUpdate.NewEpisodes(anime = anime, season = season, newEpisodeCount = 2)

        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `two NewSeason with same values are equal`() {
        val a = AnimeUpdate.NewSeason(anime = anime, sequelMalId = 1, sequelTitle = "S2")
        val b = AnimeUpdate.NewSeason(anime = anime, sequelMalId = 1, sequelTitle = "S2")

        assertThat(a).isEqualTo(b)
    }
}
