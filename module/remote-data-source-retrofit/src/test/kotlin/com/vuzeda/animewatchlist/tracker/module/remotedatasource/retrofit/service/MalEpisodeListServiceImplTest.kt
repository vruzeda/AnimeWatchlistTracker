package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalEpisodeListPageDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalEpisodeRowDto
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
    fun `parses Shingeki no Kyojin Season 3 episodes from real MAL HTML fixture`() {
        val html = loadFixture("fixtures/mal_episode_list_35760_shingeki_no_kyojin_season_3.html")

        val page = MalEpisodeListServiceImpl.parseEpisodeListHtml(html, currentOffset = 0)

        assertThat(page).isEqualTo(MalEpisodeListPageDtoFixture.episodeListPage)
    }

    private fun loadFixture(name: String): String {
        val resource = javaClass.getResource("/$name") ?: throw IllegalStateException("Fixture not found: $name")
        return resource.readText()
    }

    private object MalEpisodeListPageDtoFixture {
        val episodeListPage = MalEpisodeListPageDto(
            episodes = listOf(
                MalEpisodeRowDtoFixture.episodeRow1,
                MalEpisodeRowDtoFixture.episodeRow2,
                MalEpisodeRowDtoFixture.episodeRow3,
                MalEpisodeRowDtoFixture.episodeRow4,
                MalEpisodeRowDtoFixture.episodeRow5,
                MalEpisodeRowDtoFixture.episodeRow6,
                MalEpisodeRowDtoFixture.episodeRow7,
                MalEpisodeRowDtoFixture.episodeRow8,
                MalEpisodeRowDtoFixture.episodeRow9,
                MalEpisodeRowDtoFixture.episodeRow10,
                MalEpisodeRowDtoFixture.episodeRow11,
                MalEpisodeRowDtoFixture.episodeRow12,
                MalEpisodeRowDtoFixture.episodeRow13,
                MalEpisodeRowDtoFixture.episodeRow14,
                MalEpisodeRowDtoFixture.episodeRow15,
                MalEpisodeRowDtoFixture.episodeRow16,
                MalEpisodeRowDtoFixture.episodeRow17,
                MalEpisodeRowDtoFixture.episodeRow18,
                MalEpisodeRowDtoFixture.episodeRow19,
                MalEpisodeRowDtoFixture.episodeRow20,
                MalEpisodeRowDtoFixture.episodeRow21,
                MalEpisodeRowDtoFixture.episodeRow22,
            ),
            hasNextPage = false,
        )
    }

    private object MalEpisodeRowDtoFixture {
        val episodeRow1 = MalEpisodeRowDto(
            number = 1,
            title = "Smoke Signal",
            titleEnglish = "Smoke Signal",
            titleJapanese = "狼煙",
            airedIsoDate = "2018-07-23",
        )

        val episodeRow2 = MalEpisodeRowDto(
            number = 2,
            title = "Pain",
            titleEnglish = "Pain",
            titleJapanese = "痛み",
            airedIsoDate = "2018-07-30",
        )

        val episodeRow3 = MalEpisodeRowDto(
            number = 3,
            title = "Old Story",
            titleEnglish = "Old Story",
            titleJapanese = "オールドストーリー",
            airedIsoDate = "2018-08-06",
        )

        val episodeRow4 = MalEpisodeRowDto(
            number = 4,
            title = "Trust",
            titleEnglish = "Trust",
            titleJapanese = "信頼",
            airedIsoDate = "2018-08-13",
        )

        val episodeRow5 = MalEpisodeRowDto(
            number = 5,
            title = "Reply",
            titleEnglish = "Reply",
            titleJapanese = "回答",
            airedIsoDate = "2018-08-20",
        )

        val episodeRow6 = MalEpisodeRowDto(
            number = 6,
            title = "Sin",
            titleEnglish = "Sin",
            titleJapanese = "罪",
            airedIsoDate = "2018-08-27",
        )

        val episodeRow7 = MalEpisodeRowDto(
            number = 7,
            title = "Wish",
            titleEnglish = "Wish",
            titleJapanese = "願い",
            airedIsoDate = "2018-09-03",
        )

        val episodeRow8 = MalEpisodeRowDto(
            number = 8,
            title = "Outside the Walls of Orvud District",
            titleEnglish = "Outside the Walls of Orvud District",
            titleJapanese = "オルブド区外壁",
            airedIsoDate = "2018-09-10",
        )

        val episodeRow9 = MalEpisodeRowDto(
            number = 9,
            title = "Ruler of the Walls",
            titleEnglish = "Ruler of the Walls",
            titleJapanese = "壁の王",
            airedIsoDate = "2018-09-17",
        )

        val episodeRow10 = MalEpisodeRowDto(
            number = 10,
            title = "Friends",
            titleEnglish = "Friends",
            titleJapanese = "友人",
            airedIsoDate = "2018-09-24",
        )

        val episodeRow11 = MalEpisodeRowDto(
            number = 11,
            title = "Bystander",
            titleEnglish = "Bystander",
            titleJapanese = "傍観者",
            airedIsoDate = "2018-10-08",
        )

        val episodeRow12 = MalEpisodeRowDto(
            number = 12,
            title = "Night of the Battle to Retake the Wall",
            titleEnglish = "Night of the Battle to Retake the Wall",
            titleJapanese = "奪還作戦の夜",
            airedIsoDate = "2018-10-15",
        )

        val episodeRow13 = MalEpisodeRowDto(
            number = 13,
            title = "The Town Where Everything Began",
            titleEnglish = "The Town Where Everything Began",
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow14 = MalEpisodeRowDto(
            number = 14,
            title = "Thunder Spears",
            titleEnglish = "Thunder Spears",
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow15 = MalEpisodeRowDto(
            number = 15,
            title = "Descent",
            titleEnglish = "Descent",
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow16 = MalEpisodeRowDto(
            number = 16,
            title = "Perfect Game",
            titleEnglish = "Perfect Game",
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow17 = MalEpisodeRowDto(
            number = 17,
            title = "Hero",
            titleEnglish = "Hero",
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow18 = MalEpisodeRowDto(
            number = 18,
            title = "Midnight Sun",
            titleEnglish = "Midnight Sun",
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow19 = MalEpisodeRowDto(
            number = 19,
            title = "The Basement",
            titleEnglish = "The Basement",
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow20 = MalEpisodeRowDto(
            number = 20,
            title = "That Day",
            titleEnglish = "That Day",
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow21 = MalEpisodeRowDto(
            number = 21,
            title = "Attack Titan",
            titleEnglish = "Attack Titan",
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow22 = MalEpisodeRowDto(
            number = 22,
            title = "The Other Side of the Wall",
            titleEnglish = "The Other Side of the Wall",
            titleJapanese = null,
            airedIsoDate = null,
        )
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
