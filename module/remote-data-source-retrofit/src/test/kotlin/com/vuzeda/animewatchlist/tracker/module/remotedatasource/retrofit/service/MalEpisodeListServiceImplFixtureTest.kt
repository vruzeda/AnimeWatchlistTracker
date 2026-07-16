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
            titleEnglish = "Smoke Signal",
            titleRomaji = "Noroshi",
            titleJapanese = "狼煙",
            airedIsoDate = "2018-07-23",
        )

        val episodeRow2 = MalEpisodeRowDto(
            number = 2,
            titleEnglish = "Pain",
            titleRomaji = "Itami",
            titleJapanese = "痛み",
            airedIsoDate = "2018-07-30",
        )

        val episodeRow3 = MalEpisodeRowDto(
            number = 3,
            titleEnglish = "Old Story",
            titleRomaji = "Mukashibanashi",
            titleJapanese = "オールドストーリー",
            airedIsoDate = "2018-08-06",
        )

        val episodeRow4 = MalEpisodeRowDto(
            number = 4,
            titleEnglish = "Trust",
            titleRomaji = "Shinrai",
            titleJapanese = "信頼",
            airedIsoDate = "2018-08-13",
        )

        val episodeRow5 = MalEpisodeRowDto(
            number = 5,
            titleEnglish = "Reply",
            titleRomaji = "Kaitou",
            titleJapanese = "回答",
            airedIsoDate = "2018-08-20",
        )

        val episodeRow6 = MalEpisodeRowDto(
            number = 6,
            titleEnglish = "Sin",
            titleRomaji = "Tsumi",
            titleJapanese = "罪",
            airedIsoDate = "2018-08-27",
        )

        val episodeRow7 = MalEpisodeRowDto(
            number = 7,
            titleEnglish = "Wish",
            titleRomaji = "Negai",
            titleJapanese = "願い",
            airedIsoDate = "2018-09-03",
        )

        val episodeRow8 = MalEpisodeRowDto(
            number = 8,
            titleEnglish = "Outside the Walls of Orvud District",
            titleRomaji = "Orvud-ku Sotokabe",
            titleJapanese = "オルブド区外壁",
            airedIsoDate = "2018-09-10",
        )

        val episodeRow9 = MalEpisodeRowDto(
            number = 9,
            titleEnglish = "Ruler of the Walls",
            titleRomaji = "Kabe no Ou",
            titleJapanese = "壁の王",
            airedIsoDate = "2018-09-17",
        )

        val episodeRow10 = MalEpisodeRowDto(
            number = 10,
            titleEnglish = "Friends",
            titleRomaji = "Yuujin",
            titleJapanese = "友人",
            airedIsoDate = "2018-09-24",
        )

        val episodeRow11 = MalEpisodeRowDto(
            number = 11,
            titleEnglish = "Bystander",
            titleRomaji = "Boukansha",
            titleJapanese = "傍観者",
            airedIsoDate = "2018-10-08",
        )

        val episodeRow12 = MalEpisodeRowDto(
            number = 12,
            titleEnglish = "Night of the Battle to Retake the Wall",
            titleRomaji = "Dakkan Sakusen no Yoru",
            titleJapanese = "奪還作戦の夜",
            airedIsoDate = "2018-10-15",
        )

        val episodeRow13 = MalEpisodeRowDto(
            number = 13,
            titleEnglish = "The Town Where Everything Began",
            titleRomaji = null,
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow14 = MalEpisodeRowDto(
            number = 14,
            titleEnglish = "Thunder Spears",
            titleRomaji = null,
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow15 = MalEpisodeRowDto(
            number = 15,
            titleEnglish = "Descent",
            titleRomaji = null,
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow16 = MalEpisodeRowDto(
            number = 16,
            titleEnglish = "Perfect Game",
            titleRomaji = null,
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow17 = MalEpisodeRowDto(
            number = 17,
            titleEnglish = "Hero",
            titleRomaji = null,
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow18 = MalEpisodeRowDto(
            number = 18,
            titleEnglish = "Midnight Sun",
            titleRomaji = null,
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow19 = MalEpisodeRowDto(
            number = 19,
            titleEnglish = "The Basement",
            titleRomaji = null,
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow20 = MalEpisodeRowDto(
            number = 20,
            titleEnglish = "That Day",
            titleRomaji = null,
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow21 = MalEpisodeRowDto(
            number = 21,
            titleEnglish = "Attack Titan",
            titleRomaji = null,
            titleJapanese = null,
            airedIsoDate = null,
        )

        val episodeRow22 = MalEpisodeRowDto(
            number = 22,
            titleEnglish = "The Other Side of the Wall",
            titleRomaji = null,
            titleJapanese = null,
            airedIsoDate = null,
        )
    }

    private object SeihantaiEpisodeRowDtoFixture {
        val episodeRow1 = MalEpisodeRowDto(
            number = 1,
            titleEnglish = "You, My Polar Opposite",
            titleRomaji = "Seihantai na Kimi",
            titleJapanese = "正反対な君",
            airedIsoDate = "2026-01-11",
        )

        val episodeRow2 = MalEpisodeRowDto(
            number = 2,
            titleEnglish = "First Date!",
            titleRomaji = "Hatsu Date!",
            titleJapanese = "初デート!",
            airedIsoDate = "2026-01-18",
        )

        val episodeRow3 = MalEpisodeRowDto(
            number = 3,
            titleEnglish = "Cute and Cool",
            titleRomaji = "Kawaii to Kakkoii",
            titleJapanese = "カワイイとカッコイイ",
            airedIsoDate = "2026-01-25",
        )

        val episodeRow4 = MalEpisodeRowDto(
            number = 4,
            titleEnglish = "Summer Night Vibes",
            titleRomaji = "Natsu no Yoru no Vibes",
            titleJapanese = "夏の夜のバイブス",
            airedIsoDate = "2026-02-01",
        )

        val episodeRow5 = MalEpisodeRowDto(
            number = 5,
            titleEnglish = "Someone Who Thinks and Someone Who Doesn't",
            titleRomaji = "Kangaeru Hito, Kangaenai Hito",
            titleJapanese = "考える人、考えない人",
            airedIsoDate = "2026-02-08",
        )

        val episodeRow6 = MalEpisodeRowDto(
            number = 6,
            titleEnglish = "Cultural Festival!",
            titleRomaji = "Bunkasai!",
            titleJapanese = "文化祭！",
            airedIsoDate = "2026-02-15",
        )

        val episodeRow7 = MalEpisodeRowDto(
            number = 7,
            titleEnglish = "Fluttery Confusion",
            titleRomaji = "Doki Moya",
            titleJapanese = "ドキモヤ",
            airedIsoDate = "2026-02-22",
        )

        val episodeRow8 = MalEpisodeRowDto(
            number = 8,
            titleEnglish = "This Autumn...",
            titleRomaji = "Kotoshi no Aki wa...",
            titleJapanese = "今年の秋は...",
            airedIsoDate = "2026-03-01",
        )

        val episodeRow9 = MalEpisodeRowDto(
            number = 9,
            titleEnglish = "Surprise!",
            titleRomaji = "Surprise!",
            titleJapanese = "サプライズ!",
            airedIsoDate = "2026-03-08",
        )

        val episodeRow10 = MalEpisodeRowDto(
            number = 10,
            titleEnglish = "Class Trip (Part 1)",
            titleRomaji = "Shuugaku Ryokou! (Zenpen)",
            titleJapanese = "修学旅行！(前編)",
            airedIsoDate = "2026-03-15",
        )

        val episodeRow11 = MalEpisodeRowDto(
            number = 11,
            titleEnglish = "Class Trip (Part 2)",
            titleRomaji = "Shuugaku Ryokou! (Kouhen)",
            titleJapanese = "修学旅行！(後編)",
            airedIsoDate = "2026-03-22",
        )

        val episodeRow12 = MalEpisodeRowDto(
            number = 12,
            titleEnglish = "Step by Step",
            titleRomaji = "Hoippo",
            titleJapanese = "ほいっぽ",
            airedIsoDate = "2026-03-29",
        )
    }

    private object ReplicaEpisodeRowDtoFixture {
        val episodeRow1 = MalEpisodeRowDto(
            number = 1,
            titleEnglish = "A Replica Never Dreams",
            titleRomaji = null,
            titleJapanese = "レプリカは、夢を見ない。",
            airedIsoDate = "2026-04-07",
        )

        val episodeRow2 = MalEpisodeRowDto(
            number = 2,
            titleEnglish = "A Replica Skips School",
            titleRomaji = null,
            titleJapanese = "レプリカは、サボる。",
            airedIsoDate = "2026-04-14",
        )

        val episodeRow3 = MalEpisodeRowDto(
            number = 3,
            titleEnglish = "A Replica Is Confused",
            titleRomaji = "Replica wa, Madou.",
            titleJapanese = "レプリカは、惑う。",
            airedIsoDate = "2026-04-21",
        )

        val episodeRow4 = MalEpisodeRowDto(
            number = 4,
            titleEnglish = "A Replica Cries",
            titleRomaji = "Replica wa, Naiteiru.",
            titleJapanese = "レプリカは、泣いている。",
            airedIsoDate = "2026-04-28",
        )

        val episodeRow5 = MalEpisodeRowDto(
            number = 5,
            titleEnglish = "A Replica Dreams",
            titleRomaji = "Replica wa, Yume wo Miru.",
            titleJapanese = "レプリカは、夢を見る。",
            airedIsoDate = "2026-05-05",
        )

        val episodeRow6 = MalEpisodeRowDto(
            number = 6,
            titleEnglish = "A Replica is Shaken",
            titleRomaji = "Replica wa, Yureru.",
            titleJapanese = null,
            airedIsoDate = "2026-05-12",
        )

        val episodeRow7 = MalEpisodeRowDto(
            number = 7,
            titleEnglish = "A Replica Searches",
            titleRomaji = "Replica wa, Sagasu.",
            titleJapanese = null,
            airedIsoDate = "2026-05-19",
        )

        val episodeRow8 = MalEpisodeRowDto(
            number = 8,
            titleEnglish = "A Replica Is Messed Up",
            titleRomaji = null,
            titleJapanese = "レプリカは、歪む。",
            airedIsoDate = "2026-05-26",
        )

        val episodeRow9 = MalEpisodeRowDto(
            number = 9,
            titleEnglish = "A Replica is Lost",
            titleRomaji = null,
            titleJapanese = "レプリカは、失う。",
            airedIsoDate = "2026-06-02",
        )

        val episodeRow10 = MalEpisodeRowDto(
            number = 10,
            titleEnglish = "A Replica Stumbles",
            titleRomaji = "Replica wa, Korogaru.",
            titleJapanese = "レプリカは、転がる。",
            airedIsoDate = "2026-06-10",
        )

        val episodeRow11 = MalEpisodeRowDto(
            number = 11,
            titleEnglish = "A Replica Goes On a Journey",
            titleRomaji = null,
            titleJapanese = "レプリカは、旅に出る。",
            airedIsoDate = "2026-06-16",
        )

        val episodeRow12 = MalEpisodeRowDto(
            number = 12,
            titleEnglish = "A Replica Tosses",
            titleRomaji = "Replica wa, Nageru.",
            titleJapanese = "レプリカは、投げる。",
            airedIsoDate = "2026-06-24",
        )

        val episodeRow13 = MalEpisodeRowDto(
            number = 13,
            titleEnglish = "And Then, the Replica",
            titleRomaji = "Soshite, Replica wa.",
            titleJapanese = "そして、レプリカは。",
            airedIsoDate = "2026-06-30",
        )
    }
}