package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class MalEpisodeListServiceImplTest {

    private fun episodeRow(number: Int, titleEnglish: String, titleRomaji: String?, titleJapanese: String?, aired: String): String = """
        <tr class="episode-list-data">
          <td class="episode-number nowrap" data-raw="$number">$number</td>
          <td class="episode-video nowrap"><a href="https://myanimelist.net/anime/52991/Sousou_no_Frieren/episode/$number" class="mal-icon" title="Watch Episode #$number"><i class="malicon malicon-movie-episode"></i></a></td>
          <td class="episode-title fs12"><a href="https://myanimelist.net/anime/52991/Sousou_no_Frieren/episode/$number" class="fl-l fw-b ">$titleEnglish</a>
            <br><span class="di-ib">${titleRomaji?.let { "$it&nbsp;" } ?: ""}${titleJapanese?.let { "($it)" } ?: ""}</span>
          </td>
          <td class="episode-aired nowrap">$aired</td><td class="episode-poll ac nowrap scored" data-raw="4.29"></td>
        </tr>
    """.trimIndent()

    @Test
    fun `parses episode number titleEnglish romajiJapanese and ISO air date from rows`() {
        val html = episodeRow(1, "The Journey's End", "Tabi no Owari", "旅の終わり", "Sep 29, 2023") +
            episodeRow(2, "It Didn't Have to Be Magic…", "Mahou ja Nakatta", "魔法じゃなかった", "Oct 6, 2023")

        val page = MalEpisodeListServiceImpl.parseEpisodeListHtml(html, currentOffset = 0)

        assertThat(page.episodes).hasSize(2)
        assertThat(page.episodes[0].number).isEqualTo(1)
        assertThat(page.episodes[0].titleEnglish).isEqualTo("The Journey's End")
        assertThat(page.episodes[0].titleRomaji).isEqualTo("Tabi no Owari")
        assertThat(page.episodes[0].titleJapanese).isEqualTo("旅の終わり")
        assertThat(page.episodes[0].airedIsoDate).isEqualTo("2023-09-29")
        assertThat(page.episodes[1].number).isEqualTo(2)
        assertThat(page.episodes[1].titleEnglish).isEqualTo("It Didn't Have to Be Magic…")
        assertThat(page.episodes[1].titleRomaji).isEqualTo("Mahou ja Nakatta")
        assertThat(page.episodes[1].titleJapanese).isEqualTo("魔法じゃなかった")
        assertThat(page.episodes[1].airedIsoDate).isEqualTo("2023-10-06")
    }

    @Test
    fun `treats unparseable air date as null`() {
        val html = episodeRow(1, "Pilot", "Pilot", "パイロット", "N/A")

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
        assertThat(page.episodes[0].titleEnglish).isNull()
        assertThat(page.episodes[0].titleRomaji).isNull()
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
        val html = episodeRow(100, "Century", "Century", "世紀末", "Jan 1, 2010") +
            """<a href="https://myanimelist.net/anime/21/One_Piece/episode?offset=100">101-200</a>"""

        val page = MalEpisodeListServiceImpl.parseEpisodeListHtml(html, currentOffset = 0)

        assertThat(page.hasNextPage).isTrue()
    }

    @Test
    fun `reports last page when no pager link points past the current offset`() {
        val html = episodeRow(28, "Finale", "Finale", "フィナーレ", "Mar 22, 2024") +
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
    fun `parses romajiJapanese without Japanese part`() {
        val html = episodeRow(1, "Pilot", "Pilot", null, "Jan 1, 2024")

        val page = MalEpisodeListServiceImpl.parseEpisodeListHtml(html, currentOffset = 0)

        assertThat(page.episodes[0].titleRomaji).isEqualTo("Pilot")
        assertThat(page.episodes[0].titleJapanese).isNull()
    }

    @Test
    fun `parses romajiJapanese without Romaji part`() {
        val html = episodeRow(1, "Pilot", null, "Pilot", "Jan 1, 2024")

        val page = MalEpisodeListServiceImpl.parseEpisodeListHtml(html, currentOffset = 0)

        assertThat(page.episodes[0].titleRomaji).isNull()
        assertThat(page.episodes[0].titleJapanese).isEqualTo("Pilot")
    }
}
