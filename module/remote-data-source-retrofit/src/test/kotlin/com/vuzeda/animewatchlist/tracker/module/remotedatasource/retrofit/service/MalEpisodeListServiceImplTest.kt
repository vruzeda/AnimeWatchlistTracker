package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class MalEpisodeListServiceImplTest {

    private fun episodeRow(number: Int, title: String, japaneseTitle: String, aired: String): String = """
        <tr class="episode-list-data">
          <td class="episode-number nowrap" data-raw="$number">$number</td>
          <td class="episode-video nowrap"><a href="https://myanimelist.net/anime/52991/Sousou_no_Frieren/episode/$number" class="mal-icon" title="Watch Episode #$number"><i class="malicon malicon-movie-episode"></i></a></td>
          <td class="episode-title fs12"><a href="https://myanimelist.net/anime/52991/Sousou_no_Frieren/episode/$number" class="fl-l fw-b ">$title</a>
            <br><span class="di-ib">$japaneseTitle</span>
          </td>
          <td class="episode-aired nowrap">$aired</td><td class="episode-poll ac nowrap scored" data-raw="4.29"></td>
        </tr>
    """.trimIndent()

    @Test
    fun `parses episode number title and ISO air date from rows`() {
        val html = episodeRow(1, "The Journey&#039;s End", "Noroshi&nbsp;(狼煙)", "Sep 29, 2023") +
            episodeRow(2, "It Didn&#039;t Have to Be Magic…", "Itami&nbsp;(痛み)", "Oct 6, 2023")

        val page = MalEpisodeListServiceImpl.parseEpisodeListHtml(html, currentOffset = 0)

        assertThat(page.episodes).hasSize(2)
        assertThat(page.episodes[0].number).isEqualTo(1)
        assertThat(page.episodes[0].title).isEqualTo("The Journey's End")
        assertThat(page.episodes[0].titleEnglish).isEqualTo("The Journey's End")
        assertThat(page.episodes[0].titleJapanese).isEqualTo("狼煙")
        assertThat(page.episodes[0].airedIsoDate).isEqualTo("2023-09-29")
        assertThat(page.episodes[1].number).isEqualTo(2)
        assertThat(page.episodes[1].titleEnglish).isEqualTo("It Didn't Have to Be Magic…")
        assertThat(page.episodes[1].titleJapanese).isEqualTo("痛み")
        assertThat(page.episodes[1].airedIsoDate).isEqualTo("2023-10-06")
    }

    @Test
    fun `treats unparseable air date as null`() {
        val html = episodeRow(1, "Pilot", "Romaji&nbsp;(日本語)", "N/A")

        val page = MalEpisodeListServiceImpl.parseEpisodeListHtml(html, currentOffset = 0)

        assertThat(page.episodes[0].airedIsoDate).isNull()
    }

    @Test
    fun `treats missing title anchor as null title`() {
        val html = """
            <tr class="episode-list-data">
              <td class="episode-number nowrap" data-raw="7">7</td>
              <td class="episode-aired nowrap">Nov 10, 2023</td>
            </tr>
        """.trimIndent()

        val page = MalEpisodeListServiceImpl.parseEpisodeListHtml(html, currentOffset = 0)

        assertThat(page.episodes).hasSize(1)
        assertThat(page.episodes[0].title).isNull()
        assertThat(page.episodes[0].titleEnglish).isNull()
        assertThat(page.episodes[0].titleJapanese).isNull()
        assertThat(page.episodes[0].airedIsoDate).isEqualTo("2023-11-10")
    }

    @Test
    fun `skips rows without an episode number`() {
        val html = """
            <tr class="episode-list-data">
              <td class="episode-title fs12"><a href="#" class="fl-l fw-b ">Broken Row</a></td>
            </tr>
        """.trimIndent()

        val page = MalEpisodeListServiceImpl.parseEpisodeListHtml(html, currentOffset = 0)

        assertThat(page.episodes).isEmpty()
    }

    @Test
    fun `detects next page from pager link with the following offset`() {
        val html = episodeRow(100, "Century", "Romaji&nbsp;(日本語)", "Jan 1, 2010") +
            """<a href="https://myanimelist.net/anime/21/One_Piece/episode?offset=100">101-200</a>"""

        val page = MalEpisodeListServiceImpl.parseEpisodeListHtml(html, currentOffset = 0)

        assertThat(page.hasNextPage).isTrue()
    }

    @Test
    fun `reports last page when no pager link points past the current offset`() {
        val html = episodeRow(28, "Finale", "Romaji&nbsp;(日本語)", "Mar 22, 2024") +
            """<a href="https://myanimelist.net/anime/21/One_Piece/episode?offset=0">1-100</a>"""

        val page = MalEpisodeListServiceImpl.parseEpisodeListHtml(html, currentOffset = 100)

        assertThat(page.hasNextPage).isFalse()
    }

    @Test
    fun `parses empty page as no episodes and no next page`() {
        val page = MalEpisodeListServiceImpl.parseEpisodeListHtml("<html><body></body></html>", currentOffset = 0)

        assertThat(page.episodes).isEmpty()
        assertThat(page.hasNextPage).isFalse()
    }

    @Test
    fun `extractJapaneseTitle returns Japanese from parentheses`() {
        assertThat(extractJapaneseTitle("Noroshi (狼煙)")).isEqualTo("狼煙")
        assertThat(extractJapaneseTitle("Itami (痛み)")).isEqualTo("痛み")
    }

    @Test
    fun `extractJapaneseTitle returns Japanese-only when no romaji`() {
        assertThat(extractJapaneseTitle("(狼煙)")).isEqualTo("狼煙")
        assertThat(extractJapaneseTitle("(痛み)")).isEqualTo("痛み")
    }

    @Test
    fun `extractJapaneseTitle returns text as-is when no parentheses`() {
        assertThat(extractJapaneseTitle("Noroshi")).isEqualTo("Noroshi")
        assertThat(extractJapaneseTitle("狼煙")).isEqualTo("狼煙")
    }

    @Test
    fun `extractJapaneseTitle handles Japanese without romaji`() {
        // Pattern from anime 61013: just Japanese in parentheses
        assertThat(extractJapaneseTitle("(レプリカは、夢を見ない。)")).isEqualTo("レプリカは、夢を見ない。")
        // Pattern with romaji and Japanese
        assertThat(extractJapaneseTitle("Replica wa, Yume wo Miru. (レプリカは、夢を見る。)")).isEqualTo("レプリカは、夢を見る。")
        // Pattern with only romaji (no Japanese chars in parens)
        assertThat(extractJapaneseTitle("Replica wa, Yureru.")).isEqualTo("Replica wa, Yureru.")
    }

    @Test
    fun `extractJapaneseTitle returns null for null input`() {
        assertThat(extractJapaneseTitle(null)).isNull()
    }

    @Test
    fun `isJapaneseLetterOrDigit detects Japanese characters`() {
        // Kanji
        assertThat('狼'.isJapaneseLetterOrDigit()).isTrue()
        assertThat('煙'.isJapaneseLetterOrDigit()).isTrue()
        assertThat('痛'.isJapaneseLetterOrDigit()).isTrue()
        assertThat('み'.isJapaneseLetterOrDigit()).isTrue() // Hiragana
        assertThat('レ'.isJapaneseLetterOrDigit()).isTrue() // Katakana
        assertThat('プ'.isJapaneseLetterOrDigit()).isTrue()
        // Non-Japanese
        assertThat('N'.isJapaneseLetterOrDigit()).isFalse()
        assertThat('o'.isJapaneseLetterOrDigit()).isFalse()
        assertThat('r'.isJapaneseLetterOrDigit()).isFalse()
        assertThat('o'.isJapaneseLetterOrDigit()).isFalse()
        assertThat('s'.isJapaneseLetterOrDigit()).isFalse()
        assertThat('h'.isJapaneseLetterOrDigit()).isFalse()
        assertThat('i'.isJapaneseLetterOrDigit()).isFalse()
        assertThat(' '.isJapaneseLetterOrDigit()).isFalse()
        assertThat('.'.isJapaneseLetterOrDigit()).isFalse()
    }
}
