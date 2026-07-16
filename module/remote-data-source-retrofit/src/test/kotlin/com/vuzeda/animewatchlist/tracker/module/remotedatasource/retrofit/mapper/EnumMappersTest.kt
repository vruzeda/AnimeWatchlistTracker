package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchOrderBy
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchStatus
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSearchType
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import org.junit.jupiter.api.Test

class AnimeSearchTypeMapperTest {
    @Test
    fun `ALL maps to null`() {
        assertThat(AnimeSearchType.ALL.toApiValue()).isNull()
    }

    @Test
    fun `TV maps to tv`() {
        assertThat(AnimeSearchType.TV.toApiValue()).isEqualTo("tv")
    }

    @Test
    fun `MOVIE maps to movie`() {
        assertThat(AnimeSearchType.MOVIE.toApiValue()).isEqualTo("movie")
    }
}

class AnimeSearchStatusMapperTest {
    @Test
    fun `ALL maps to null`() {
        assertThat(AnimeSearchStatus.ALL.toApiValue()).isNull()
    }

    @Test
    fun `AIRING maps to airing`() {
        assertThat(AnimeSearchStatus.AIRING.toApiValue()).isEqualTo("airing")
    }

    @Test
    fun `COMPLETE maps to complete`() {
        assertThat(AnimeSearchStatus.COMPLETE.toApiValue()).isEqualTo("complete")
    }
}

class AnimeSearchOrderByMapperTest {
    @Test
    fun `DEFAULT maps to null`() {
        assertThat(AnimeSearchOrderBy.DEFAULT.toApiValue()).isNull()
    }

    @Test
    fun `SCORE maps to score`() {
        assertThat(AnimeSearchOrderBy.SCORE.toApiValue()).isEqualTo("score")
    }

    @Test
    fun `RANK maps to rank`() {
        assertThat(AnimeSearchOrderBy.RANK.toApiValue()).isEqualTo("rank")
    }
}

class AnimeSeasonMapperTest {
    @Test
    fun `WINTER maps to winter`() {
        assertThat(AnimeSeason.WINTER.toApiValue()).isEqualTo("winter")
    }

    @Test
    fun `SPRING maps to spring`() {
        assertThat(AnimeSeason.SPRING.toApiValue()).isEqualTo("spring")
    }

    @Test
    fun `toAnimeSeason parses winter`() {
        assertThat("winter".toAnimeSeason()).isEqualTo(AnimeSeason.WINTER)
    }

    @Test
    fun `toAnimeSeason handles case insensitive matching`() {
        assertThat("WINTER".toAnimeSeason()).isEqualTo(AnimeSeason.WINTER)
    }

    @Test
    fun `toAnimeSeason returns null for unknown value`() {
        assertThat("unknown".toAnimeSeason()).isNull()
    }
}
