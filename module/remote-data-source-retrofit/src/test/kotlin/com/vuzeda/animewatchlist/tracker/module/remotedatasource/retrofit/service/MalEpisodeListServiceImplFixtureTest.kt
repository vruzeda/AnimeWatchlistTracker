package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalEpisodeListPageDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalEpisodeRowDto
import org.junit.jupiter.api.Test

class MalEpisodeListServiceImplFixtureTest {

    @Test
    fun `parses Shingeki no Kyojin Season 3 episodes from real MAL HTML fixture`() {
        val html = loadFixture("fixtures/mal_episode_list_35760_shingeki_no_kyojin_season_3.html")

        val page = MalEpisodeListServiceImpl.parseEpisodeListHtml(html, currentOffset = 0)

        assertThat(page).isEqualTo(MalEpisodeListPageDtoFixture.shingekiNoKyojinEpisodeListPage)
    }

    @Test
    fun `parses Seihantai na Kimi to Boku episodes from real MAL HTML fixture`() {
        val html = loadFixture("fixtures/mal_episode_list_60371_seihantai_na_kimi_to_boku.html")

        val page = MalEpisodeListServiceImpl.parseEpisodeListHtml(html, currentOffset = 0)

        assertThat(page).isEqualTo(MalEpisodeListPageDtoFixture.seihantaiEpisodeListPage)
    }

    @Test
    fun `parses Replica datte Koi wo Suru episodes from real MAL HTML fixture`() {
        val html = loadFixture("fixtures/mal_episode_list_61013_replica_datte_koi_wo_suru.html")

        val page = MalEpisodeListServiceImpl.parseEpisodeListHtml(html, currentOffset = 0)

        assertThat(page).isEqualTo(MalEpisodeListPageDtoFixture.replicaEpisodeListPage)
    }

    private fun loadFixture(name: String): String {
        val resource = javaClass.getResource("/$name")
            ?: throw IllegalStateException("Fixture not found: $name")
        return resource.readText()
    }

    private object MalEpisodeListPageDtoFixture {
        val shingekiNoKyojinEpisodeListPage = MalEpisodeListPageDto(
            episodes = listOf(
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow1,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow2,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow3,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow4,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow5,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow6,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow7,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow8,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow9,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow10,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow11,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow12,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow13,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow14,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow15,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow16,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow17,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow18,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow19,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow20,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow21,
                ShingekiNoKyojinEpisodeRowDtoFixture.episodeRow22,
            ),
            hasNextPage = false,
        )

        val seihantaiEpisodeListPage = MalEpisodeListPageDto(
            episodes = listOf(
                SeihantaiEpisodeRowDtoFixture.episodeRow1,
                SeihantaiEpisodeRowDtoFixture.episodeRow2,
                SeihantaiEpisodeRowDtoFixture.episodeRow3,
                SeihantaiEpisodeRowDtoFixture.episodeRow4,
                SeihantaiEpisodeRowDtoFixture.episodeRow5,
                SeihantaiEpisodeRowDtoFixture.episodeRow6,
                SeihantaiEpisodeRowDtoFixture.episodeRow7,
                SeihantaiEpisodeRowDtoFixture.episodeRow8,
                SeihantaiEpisodeRowDtoFixture.episodeRow9,
                SeihantaiEpisodeRowDtoFixture.episodeRow10,
                SeihantaiEpisodeRowDtoFixture.episodeRow11,
                SeihantaiEpisodeRowDtoFixture.episodeRow12,
            ),
            hasNextPage = false,
        )

        val replicaEpisodeListPage = MalEpisodeListPageDto(
            episodes = listOf(
                ReplicaEpisodeRowDtoFixture.episodeRow1,
                ReplicaEpisodeRowDtoFixture.episodeRow2,
                ReplicaEpisodeRowDtoFixture.episodeRow3,
                ReplicaEpisodeRowDtoFixture.episodeRow4,
                ReplicaEpisodeRowDtoFixture.episodeRow5,
                ReplicaEpisodeRowDtoFixture.episodeRow6,
                ReplicaEpisodeRowDtoFixture.episodeRow7,
                ReplicaEpisodeRowDtoFixture.episodeRow8,
                ReplicaEpisodeRowDtoFixture.episodeRow9,
                ReplicaEpisodeRowDtoFixture.episodeRow10,
                ReplicaEpisodeRowDtoFixture.episodeRow11,
                ReplicaEpisodeRowDtoFixture.episodeRow12,
                ReplicaEpisodeRowDtoFixture.episodeRow13,
            ),
            hasNextPage = false,
        )
    }

    private object ShingekiNoKyojinEpisodeRowDtoFixture {
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

    private object SeihantaiEpisodeRowDtoFixture {
        val episodeRow1 = MalEpisodeRowDto(
            number = 1,
            title = "You, My Polar Opposite",
            titleEnglish = "You, My Polar Opposite",
            titleJapanese = "正反対な君",
            airedIsoDate = "2026-01-11",
        )

        val episodeRow2 = MalEpisodeRowDto(
            number = 2,
            title = "First Date!",
            titleEnglish = "First Date!",
            titleJapanese = "初デート!",
            airedIsoDate = "2026-01-18",
        )

        val episodeRow3 = MalEpisodeRowDto(
            number = 3,
            title = "Cute and Cool",
            titleEnglish = "Cute and Cool",
            titleJapanese = "カワイイとカッコイイ",
            airedIsoDate = "2026-01-25",
        )

        val episodeRow4 = MalEpisodeRowDto(
            number = 4,
            title = "Summer Night Vibes",
            titleEnglish = "Summer Night Vibes",
            titleJapanese = "夏の夜のバイブス",
            airedIsoDate = "2026-02-01",
        )

        val episodeRow5 = MalEpisodeRowDto(
            number = 5,
            title = "Someone Who Thinks and Someone Who Doesn't",
            titleEnglish = "Someone Who Thinks and Someone Who Doesn't",
            titleJapanese = "考える人、考えない人",
            airedIsoDate = "2026-02-08",
        )

        val episodeRow6 = MalEpisodeRowDto(
            number = 6,
            title = "Cultural Festival!",
            titleEnglish = "Cultural Festival!",
            titleJapanese = "文化祭！",
            airedIsoDate = "2026-02-15",
        )

        val episodeRow7 = MalEpisodeRowDto(
            number = 7,
            title = "Fluttery Confusion",
            titleEnglish = "Fluttery Confusion",
            titleJapanese = "ドキモヤ",
            airedIsoDate = "2026-02-22",
        )

        val episodeRow8 = MalEpisodeRowDto(
            number = 8,
            title = "This Autumn...",
            titleEnglish = "This Autumn...",
            titleJapanese = "今年の秋は...",
            airedIsoDate = "2026-03-01",
        )

        val episodeRow9 = MalEpisodeRowDto(
            number = 9,
            title = "Surprise!",
            titleEnglish = "Surprise!",
            titleJapanese = "サプライズ!",
            airedIsoDate = "2026-03-08",
        )

        val episodeRow10 = MalEpisodeRowDto(
            number = 10,
            title = "Class Trip (Part 1)",
            titleEnglish = "Class Trip (Part 1)",
            titleJapanese = "Shuugaku Ryokou!  )",
            airedIsoDate = "2026-03-15",
        )

        val episodeRow11 = MalEpisodeRowDto(
            number = 11,
            title = "Class Trip (Part 2)",
            titleEnglish = "Class Trip (Part 2)",
            titleJapanese = "Shuugaku Ryokou!  )",
            airedIsoDate = "2026-03-22",
        )

        val episodeRow12 = MalEpisodeRowDto(
            number = 12,
            title = "Step by Step",
            titleEnglish = "Step by Step",
            titleJapanese = "ほいっぽ",
            airedIsoDate = "2026-03-29",
        )
    }

    private object ReplicaEpisodeRowDtoFixture {
        val episodeRow1 = MalEpisodeRowDto(
            number = 1,
            title = "A Replica Never Dreams",
            titleEnglish = "A Replica Never Dreams",
            titleJapanese = "レプリカは、夢を見ない。",
            airedIsoDate = "2026-04-07",
        )

        val episodeRow2 = MalEpisodeRowDto(
            number = 2,
            title = "A Replica Skips School",
            titleEnglish = "A Replica Skips School",
            titleJapanese = "レプリカは、サボる。",
            airedIsoDate = "2026-04-14",
        )

        val episodeRow3 = MalEpisodeRowDto(
            number = 3,
            title = "A Replica Is Confused",
            titleEnglish = "A Replica Is Confused",
            titleJapanese = "レプリカは、惑う。",
            airedIsoDate = "2026-04-21",
        )

        val episodeRow4 = MalEpisodeRowDto(
            number = 4,
            title = "A Replica Cries",
            titleEnglish = "A Replica Cries",
            titleJapanese = "レプリカは、泣いている。",
            airedIsoDate = "2026-04-28",
        )

        val episodeRow5 = MalEpisodeRowDto(
            number = 5,
            title = "A Replica Dreams",
            titleEnglish = "A Replica Dreams",
            titleJapanese = "レプリカは、夢を見る。",
            airedIsoDate = "2026-05-05",
        )

        val episodeRow6 = MalEpisodeRowDto(
            number = 6,
            title = "A Replica is Shaken",
            titleEnglish = "A Replica is Shaken",
            titleJapanese = "Replica wa, Yureru.",
            airedIsoDate = "2026-05-12",
        )

        val episodeRow7 = MalEpisodeRowDto(
            number = 7,
            title = "A Replica Searches",
            titleEnglish = "A Replica Searches",
            titleJapanese = "Replica wa, Sagasu.",
            airedIsoDate = "2026-05-19",
        )

        val episodeRow8 = MalEpisodeRowDto(
            number = 8,
            title = "A Replica Is Messed Up",
            titleEnglish = "A Replica Is Messed Up",
            titleJapanese = "レプリカは、歪む。",
            airedIsoDate = "2026-05-26",
        )

        val episodeRow9 = MalEpisodeRowDto(
            number = 9,
            title = "A Replica is Lost",
            titleEnglish = "A Replica is Lost",
            titleJapanese = "レプリカは、失う。",
            airedIsoDate = "2026-06-02",
        )

        val episodeRow10 = MalEpisodeRowDto(
            number = 10,
            title = "A Replica Stumbles",
            titleEnglish = "A Replica Stumbles",
            titleJapanese = "レプリカは、転がる。",
            airedIsoDate = "2026-06-10",
        )

        val episodeRow11 = MalEpisodeRowDto(
            number = 11,
            title = "A Replica Goes On a Journey",
            titleEnglish = "A Replica Goes On a Journey",
            titleJapanese = "レプリカは、旅に出る。",
            airedIsoDate = "2026-06-16",
        )

        val episodeRow12 = MalEpisodeRowDto(
            number = 12,
            title = "A Replica Tosses",
            titleEnglish = "A Replica Tosses",
            titleJapanese = "レプリカは、投げる。",
            airedIsoDate = "2026-06-24",
        )

        val episodeRow13 = MalEpisodeRowDto(
            number = 13,
            title = "And Then, the Replica",
            titleEnglish = "And Then, the Replica",
            titleJapanese = "そして、レプリカは。",
            airedIsoDate = "2026-06-30",
        )
    }
}