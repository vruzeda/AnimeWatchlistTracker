package com.vuzeda.animewatchlist.tracker

import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AiredDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeDataDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeEpisodesResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeFullDataDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeFullResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeImagesDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeRelationDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeSearchResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.BroadcastDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.EpisodeDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.EpisodesPaginationDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.GenreDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.ImageUrlDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.RelatedEntryDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.SearchPaginationDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.StreamingDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.JikanApiService
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class FakeJikanApiService : JikanApiService {

    override suspend fun searchAnime(query: String, limit: Int): AnimeSearchResponseDto =
        AnimeSearchResponseDto(data = fakeResults)

    override suspend fun getAnimeFullById(malId: Int): AnimeFullResponseDto {
        val fullData = fakeFullDetails[malId]
            ?: fakeResults.firstOrNull { it.malId == malId }?.let { anime ->
                AnimeFullDataDto(
                    malId = malId,
                    title = anime.title,
                    titleEnglish = anime.titleEnglish,
                    titleJapanese = anime.titleJapanese,
                    type = anime.type,
                    images = anime.images,
                    episodes = anime.episodes,
                    score = anime.score,
                    synopsis = anime.synopsis,
                    genres = anime.genres,
                    status = anime.status,
                    relations = fakeRelations[malId] ?: emptyList(),
                )
            }
            ?: AnimeFullDataDto(malId = malId, title = "Unknown Anime")
        return AnimeFullResponseDto(data = fullData)
    }

    override suspend fun getSeasonAnime(
        year: Int,
        season: String,
        page: Int,
        filter: String
    ): AnimeSearchResponseDto = AnimeSearchResponseDto(
        pagination = SearchPaginationDto(hasNextPage = false, lastVisiblePage = 1),
        data = fakeResults.take(4)
    )

    override suspend fun getAnimeEpisodes(malId: Int, page: Int): AnimeEpisodesResponseDto {
        delay(1.seconds)
        val anime = fakeResults.firstOrNull { it.malId == malId }
        val totalEpisodes = anime?.episodes ?: 0
        val pageSize = 5
        val start = (page - 1) * pageSize + 1
        val end = minOf(start + pageSize - 1, totalEpisodes)
        val episodes = (start..end).map { ep ->
            EpisodeDto(
                malId = ep,
                title = "Episode $ep",
                aired = "2024-01-${ep.toString().padStart(2, '0')}T00:00:00+00:00"
            )
        }
        val hasNext = end < totalEpisodes
        val lastPage = if (totalEpisodes > 0) (totalEpisodes + pageSize - 1) / pageSize else 1
        return AnimeEpisodesResponseDto(
            pagination = EpisodesPaginationDto(
                lastVisiblePage = lastPage,
                hasNextPage = hasNext
            ),
            data = episodes
        )
    }

    private val fakeResults = listOf(
        // Already in watchlist (seeded) — will show check + status chip
        AnimeDataDto(
            malId = 16498,
            title = "Shingeki no Kyojin",
            titleEnglish = "Attack on Titan",
            titleJapanese = "進撃の巨人",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/10/47347l.jpg"),
            synopsis = "Centuries ago, mankind was slaughtered to near extinction by monstrous humanoid creatures called Titans, forcing humans to hide in fear behind enormous concentric walls.",
            episodes = 25,
            score = 8.57,
            genres = genres("Action", "Award Winning", "Drama", "Suspense"),
            status = "Finished Airing",
            aired = aired("2013-04-07T00:00:00+00:00"),
        ),
        AnimeDataDto(
            malId = 1535,
            title = "Death Note",
            titleEnglish = "Death Note",
            titleJapanese = "デスノート",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/1079/138100l.jpg"),
            synopsis = "A shinigami, as a god of death, can kill any person—provided they see their victim's face and write their name in a Death Note.",
            episodes = 37,
            score = 8.62,
            genres = genres("Supernatural", "Suspense"),
            status = "Finished Airing",
            aired = aired("2006-10-04T00:00:00+00:00"),
        ),
        AnimeDataDto(
            malId = 50265,
            title = "Spy x Family",
            titleJapanese = "SPY×FAMILY",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/1441/122795l.jpg"),
            synopsis = "Corrupt politicians, frenzied nationalists, and other warmongering forces constantly jeopardize the thin veneer of peace between neighboring countries Ostania and Westalis.",
            episodes = 12,
            score = 8.42,
            genres = genres("Action", "Award Winning", "Comedy"),
            status = "Finished Airing",
            aired = aired("2022-04-09T00:00:00+00:00"),
        ),
        AnimeDataDto(
            malId = 30276,
            title = "One Punch Man",
            titleEnglish = "One-Punch Man",
            titleJapanese = "ワンパンマン",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/12/76049l.jpg"),
            synopsis = "The seemingly unimpressive Saitama has a rather unique hobby: being a hero. In order to pursue his childhood dream, Saitama relentlessly trained for three years, losing all of his hair in the process.",
            episodes = 12,
            score = 8.48,
            genres = genres("Action", "Comedy"),
            status = "Finished Airing",
            aired = aired("2015-10-05T00:00:00+00:00"),
        ),
        // NOT in watchlist — will show "+" add button
        AnimeDataDto(
            malId = 52991,
            title = "Sousou no Frieren",
            titleEnglish = "Frieren: Beyond Journey's End",
            titleJapanese = "葬送のフリーレン",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/1015/138006.jpg"),
            synopsis = "During their decade-long quest to defeat the Demon King, the members of the hero's party—Himmel himself, the priest Heiter, the dwarf warrior Eisen, and the elven mage Frieren—forge bonds through adventures and battles, creating unforgettable precious memories for most of them.\n\nHowever, the time that Frieren spends with her comrades is equivalent to merely a fraction of her life, which has lasted over a thousand years. When the party disbands after their victory, Frieren casually returns to her \"usual\" routine of collecting spells across the continent. Due to her different sense of time, she seemingly holds no strong feelings toward the experiences she went through.\n\nAs the years pass, Frieren gradually realizes how her days in the hero's party truly impacted her. Witnessing the deaths of two of her former companions, Frieren begins to regret having taken their presence for granted; she vows to better understand humans and create real personal connections. Although the story of that once memorable journey has long ended, a new tale is about to begin.\n\n[Written by MAL Rewrite]",
            episodes = 28,
            score = 9.28,
            genres = genres("Adventure", "Drama", "Fantasy"),
            status = "Finished Airing",
            aired = aired("2023-09-29T00:00:00+00:00"),
        ),
        AnimeDataDto(
            malId = 59978,
            title = "Sousou no Frieren 2nd Season",
            titleEnglish = "Frieren: Beyond Journey's End Season 2",
            titleJapanese = "葬送のフリーレン 第2期",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/1921/154528.jpg"),
            synopsis = "Following the First-Class Mage Exam, the trio—elven mage Frieren, warrior Stark, and first-class mage Fern—gains access to the dangerous Northern Plateau. As the party presses onward toward Aureole, formidable adversaries force Stark to confront his insecurities, solidifying his resolve and his role as the party's frontliner.",
            episodes = 10,
            score = 9.16,
            genres = genres("Adventure", "Drama", "Fantasy"),
            status = "Currently Airing",
            aired = aired("2026-01-16T00:00:00+00:00"),
        ),
        AnimeDataDto(
            malId = 56805,
            title = "Yuusha",
            titleEnglish = "The Brave",
            titleJapanese = "勇者",
            type = "Music",
            images = images("https://myanimelist.net/images/anime/1947/138863.jpg"),
            synopsis = "Music video for the song Yuusha by YOASOBI. The song was used as the first opening theme of the anime Sousou no Frieren.",
            episodes = 1,
            score = 7.86,
            genres = emptyList(),
            status = "Finished Airing",
            aired = aired("2023-09-29T00:00:00+00:00"),
        ),
        AnimeDataDto(
            malId = 38000,
            title = "Kimetsu no Yaiba",
            titleEnglish = "Demon Slayer: Kimetsu no Yaiba",
            titleJapanese = "鬼滅の刃",
            type = "TV",
            images = images("https://cdn.myanimelist.net/images/anime/1286/99889.jpg"),
            synopsis = "Ever since the death of his father, the burden of supporting the family has fallen upon Tanjirou Kamado's shoulders.",
            episodes = 26,
            score = 8.45,
            genres = genres("Action", "Fantasy", "Historical"),
            status = "Finished Airing",
            aired = aired("2019-04-06T00:00:00+00:00"),
        ),
        AnimeDataDto(
            malId = 32281,
            title = "Kimi no Na wa.",
            titleEnglish = "Your Name.",
            titleJapanese = "君の名は。",
            type = "Movie",
            images = images("https://cdn.myanimelist.net/images/anime/5/87048.jpg"),
            synopsis = "Mitsuha Miyamizu, a high school girl, yearns to live the life of a boy in the bustling city of Tokyo.",
            episodes = 1,
            score = 8.83,
            genres = genres("Drama", "Romance", "Supernatural"),
            status = "Finished Airing",
            aired = aired("2016-08-26T00:00:00+00:00"),
        ),
        AnimeDataDto(
            malId = 40748,
            title = "Jujutsu Kaisen",
            titleEnglish = "Jujutsu Kaisen",
            titleJapanese = "呪術廻戦",
            type = "TV",
            images = images("https://cdn.myanimelist.net/images/anime/1171/109222.jpg"),
            synopsis = "Idly indulging in baseless paranormal activities with the Occult Club, Yuuji Itadori spends his days at either the clubroom or the hospital.",
            episodes = 24,
            score = 8.61,
            genres = genres("Action", "Drama", "Supernatural"),
            status = "Finished Airing",
            aired = aired("2020-10-03T00:00:00+00:00"),
        ),
        AnimeDataDto(
            malId = 53446,
            title = "Oshi no Ko",
            titleEnglish = "Oshi no Ko",
            titleJapanese = "【推しの子】",
            type = "TV",
            images = images("https://cdn.myanimelist.net/images/anime/1812/134736.jpg"),
            synopsis = "Sixteen-year-old Ai Hoshino is a talented and beautiful idol who is adored by her fans.",
            episodes = 11,
            score = 8.54,
            genres = genres("Drama", "Supernatural"),
            status = "Finished Airing",
            aired = aired("2023-04-12T00:00:00+00:00"),
        ),
        AnimeDataDto(
            malId = 51009,
            title = "Chainsaw Man",
            titleEnglish = "Chainsaw Man",
            titleJapanese = "チェンソーマン",
            type = "TV",
            images = images("https://cdn.myanimelist.net/images/anime/1806/126216.jpg"),
            synopsis = "Denji has a simple dream—to live a happy and peaceful life, spending time with a girl he likes.",
            episodes = 12,
            score = 8.25,
            genres = genres("Action", "Fantasy", "Horror"),
            status = "Finished Airing",
            aired = aired("2022-10-11T00:00:00+00:00"),
        ),
    )

    private val fakeRelations: Map<Int, List<AnimeRelationDto>> = mapOf(
        16498 to listOf(
            AnimeRelationDto(
                relation = "Sequel",
                entry = listOf(
                    RelatedEntryDto(malId = 25777, type = "anime", name = "Attack on Titan Season 2")
                )
            )
        ),
        25777 to listOf(
            AnimeRelationDto(
                relation = "Prequel",
                entry = listOf(
                    RelatedEntryDto(malId = 16498, type = "anime", name = "Attack on Titan")
                )
            )
        ),
        30276 to listOf(
            AnimeRelationDto(
                relation = "Sequel",
                entry = listOf(
                    RelatedEntryDto(malId = 34134, type = "anime", name = "One Punch Man 2nd Season")
                )
            )
        ),
        34134 to listOf(
            AnimeRelationDto(
                relation = "Prequel",
                entry = listOf(
                    RelatedEntryDto(malId = 30276, type = "anime", name = "One Punch Man")
                )
            )
        ),
        38000 to listOf(
            AnimeRelationDto(
                relation = "Sequel",
                entry = listOf(
                    RelatedEntryDto(malId = 47778, type = "anime", name = "Demon Slayer: Mugen Train Arc")
                )
            )
        ),
        52991 to listOf(
            AnimeRelationDto(
                relation = "Prequel",
                entry = listOf(
                    RelatedEntryDto(malId = 56805, type = "music", name = "Yuusha"),
                ),
            ),
            AnimeRelationDto(
                relation = "Sequel",
                entry = listOf(
                    RelatedEntryDto(malId = 59978, type = "anime", name = "Sousou no Frieren 2nd Season"),
                ),
            ),
        ),
        59978 to listOf(
            AnimeRelationDto(
                relation = "Prequel",
                entry = listOf(
                    RelatedEntryDto(malId = 56805, type = "music", name = "Yuusha"),
                    RelatedEntryDto(malId = 52991, type = "anime", name = "Sousou no Frieren"),
                ),
            ),
        ),
        56805 to listOf(
            AnimeRelationDto(
                relation = "Sequel",
                entry = listOf(
                    RelatedEntryDto(malId = 52991, type = "anime", name = "Sousou no Frieren"),
                    RelatedEntryDto(malId = 59978, type = "anime", name = "Sousou no Frieren 2nd Season"),
                ),
            ),
        ),
    )

    private val fakeFullDetails: Map<Int, AnimeFullDataDto> = mapOf(
        16498 to AnimeFullDataDto(
            malId = 16498,
            title = "Shingeki no Kyojin",
            titleEnglish = "Attack on Titan",
            titleJapanese = "進撃の巨人",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/10/47347l.jpg"),
            episodes = 25,
            score = 8.57,
            synopsis = "Centuries ago, mankind was slaughtered to near extinction by monstrous humanoid creatures called Titans, forcing humans to hide in fear behind enormous concentric walls. What makes these giants truly terrifying is that their taste for human flesh appears to be not for sustenance, but rather for pleasure.",
            genres = genres("Action", "Award Winning", "Drama", "Suspense"),
            status = "Finished Airing",
            broadcast = broadcast("Sundays at 01:58 (JST)", "Sundays", "01:58"),
            streaming = streaming(
                "Crunchyroll" to "http://www.crunchyroll.com/series-280312",
                "Netflix" to "https://www.netflix.com/title/70299043",
            ),
            relations = fakeRelations[16498] ?: emptyList(),
            season = "spring",
            year = 2013,
        ),
        25777 to AnimeFullDataDto(
            malId = 25777,
            title = "Shingeki no Kyojin Season 2",
            titleEnglish = "Attack on Titan Season 2",
            titleJapanese = "進撃の巨人 Season2",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/4/84177l.jpg"),
            episodes = 12,
            score = 8.54,
            synopsis = "For centuries, humanity has been hunted by giant, mysterious predators known as the Titans. Three mighty walls—Wall Maria, Rose, and Sheena—provided peace and protection for humanity for over a hundred years.",
            genres = genres("Action", "Drama", "Suspense"),
            status = "Finished Airing",
            broadcast = broadcast("Saturdays at 22:00 (JST)", "Saturdays", "22:00"),
            streaming = streaming(
                "Crunchyroll" to "http://www.crunchyroll.com/series-254209",
                "Netflix" to "https://www.netflix.com/title/70299043",
            ),
            relations = fakeRelations[25777] ?: emptyList(),
            season = "spring",
            year = 2017,
        ),
        30276 to AnimeFullDataDto(
            malId = 30276,
            title = "One Punch Man",
            titleEnglish = "One-Punch Man",
            titleJapanese = "ワンパンマン",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/12/76049l.jpg"),
            episodes = 12,
            score = 8.48,
            synopsis = "The seemingly unimpressive Saitama has a rather unique hobby: being a hero. In order to pursue his childhood dream, Saitama relentlessly trained for three years, losing all of his hair in the process.",
            genres = genres("Action", "Comedy"),
            status = "Finished Airing",
            broadcast = broadcast("Mondays at 01:05 (JST)", "Mondays", "01:05"),
            streaming = streaming(
                "Crunchyroll" to "http://www.crunchyroll.com/series-277822",
                "Netflix" to "https://www.netflix.com/title/80117291",
            ),
            relations = fakeRelations[30276] ?: emptyList(),
            season = "fall",
            year = 2015,
        ),
        34134 to AnimeFullDataDto(
            malId = 34134,
            title = "One Punch Man 2nd Season",
            titleEnglish = "One-Punch Man Season 2",
            titleJapanese = "ワンパンマン 2期",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/1247/122044l.jpg"),
            episodes = 12,
            score = 7.53,
            synopsis = "In the wake of defeating Boros and his mighty army, Saitama has returned to his unremarkable everyday life in Z-City. However, unbeknownst to him, the Hero Association's top ranks have already identified him as a grave threat.",
            genres = genres("Action", "Comedy"),
            status = "Finished Airing",
            broadcast = broadcast("Wednesdays at 01:35 (JST)", "Wednesdays", "01:35"),
            streaming = streaming(
                "Crunchyroll" to "http://www.crunchyroll.com/series-277822",
            ),
            relations = fakeRelations[34134] ?: emptyList(),
            season = "spring",
            year = 2019,
        ),
        1535 to AnimeFullDataDto(
            malId = 1535,
            title = "Death Note",
            titleEnglish = "Death Note",
            titleJapanese = "デスノート",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/1079/138100l.jpg"),
            episodes = 37,
            score = 8.62,
            synopsis = "Brutal murders, petty thefts, and senseless violence pollute the human world. In contrast, the realm of death gods is a humdrum, unchanging gambling den. The ingenious 17-year-old Japanese student Light Yagami discovers a supernatural Death Note notebook and uses it to try to create a utopia free of crime.",
            genres = genres("Supernatural", "Suspense"),
            status = "Finished Airing",
            broadcast = broadcast("Wednesdays at 00:56 (JST)", "Wednesdays", "00:56"),
            streaming = streaming(
                "Crunchyroll" to "http://www.crunchyroll.com/series-278866",
                "Netflix" to "https://www.netflix.com/title/70204970",
            ),
            relations = fakeRelations[1535] ?: emptyList(),
            season = "fall",
            year = 2006,
        ),
        11061 to AnimeFullDataDto(
            malId = 11061,
            title = "Hunter x Hunter (2011)",
            titleEnglish = "Hunter x Hunter",
            titleJapanese = "HUNTER×HUNTER（ハンター×ハンター）",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/1337/99013l.jpg"),
            episodes = 148,
            score = 9.03,
            synopsis = "Hunters devote themselves to accomplishing hazardous tasks, all from traversing the world's uncharted territories to locating rare items and monsters. Before becoming a Hunter, one must pass the Hunter Examination—a high-risk selection process in which most applicants end up handicapped or worse.",
            genres = genres("Action", "Adventure", "Fantasy"),
            status = "Finished Airing",
            broadcast = broadcast("Sundays at 10:55 (JST)", "Sundays", "10:55"),
            streaming = streaming(
                "Crunchyroll" to "http://www.crunchyroll.com/series-237800",
                "Netflix" to "https://www.netflix.com/title/70300472",
            ),
            relations = fakeRelations[11061] ?: emptyList(),
            season = "fall",
            year = 2011,
        ),
        28977 to AnimeFullDataDto(
            malId = 28977,
            title = "Gintama°",
            titleEnglish = "Gintama Season 4",
            titleJapanese = "銀魂°",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/3/72078l.jpg"),
            episodes = 51,
            score = 9.05,
            synopsis = "Gintoki, Shinpachi, and Kagura return as the fun-loving but broke members of the Yorozuya team! Living in an alternate-reality Edo, where swords are prohibited and alien overlords have taken over Japan.",
            genres = genres("Action", "Comedy", "Sci-Fi"),
            status = "Finished Airing",
            broadcast = broadcast("Wednesdays at 18:00 (JST)", "Wednesdays", "18:00"),
            streaming = streaming(
                "Crunchyroll" to "http://www.crunchyroll.com/series-47620",
            ),
            relations = fakeRelations[28977] ?: emptyList(),
            season = "spring",
            year = 2015,
        ),
        50265 to AnimeFullDataDto(
            malId = 50265,
            title = "Spy x Family",
            titleJapanese = "SPY×FAMILY",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/1441/122795l.jpg"),
            episodes = 12,
            score = 8.42,
            synopsis = "Corrupt politicians, frenzied nationalists, and other warmongering forces constantly jeopardize the thin veneer of peace between neighboring countries Ostania and Westalis.",
            genres = genres("Action", "Award Winning", "Comedy"),
            status = "Finished Airing",
            broadcast = broadcast("Saturdays at 23:00 (JST)", "Saturdays", "23:00"),
            streaming = streaming(
                "Crunchyroll" to "http://www.crunchyroll.com/series-282626",
                "Netflix" to "https://www.netflix.com/",
                "Hulu" to "https://www.hulu.com/",
            ),
            relations = fakeRelations[50265] ?: emptyList(),
            season = "spring",
            year = 2022,
        ),
        5114 to AnimeFullDataDto(
            malId = 5114,
            title = "Fullmetal Alchemist: Brotherhood",
            titleEnglish = "Fullmetal Alchemist: Brotherhood",
            titleJapanese = "鋼の錬金術師 FULLMETAL ALCHEMIST",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/1208/94745l.jpg"),
            episodes = 64,
            score = 9.11,
            synopsis = "After a horrific alchemy experiment goes wrong in the Elric household, brothers Edward and Alphonse are left in a catastrophic new reality. Ignoring the alchemical principle banning human transmutation, the boys attempted to bring their recently deceased mother back to life.",
            genres = genres("Action", "Adventure", "Drama", "Fantasy"),
            status = "Finished Airing",
            broadcast = broadcast("Sundays at 17:00 (JST)", "Sundays", "17:00"),
            streaming = streaming(
                "Crunchyroll" to "http://www.crunchyroll.com/series-271031",
            ),
            relations = fakeRelations[5114] ?: emptyList(),
            season = "spring",
            year = 2009,
        ),
        20 to AnimeFullDataDto(
            malId = 20,
            title = "Naruto",
            titleEnglish = "Naruto",
            titleJapanese = "ナルト",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/1141/142503l.jpg"),
            episodes = 220,
            score = 8.02,
            synopsis = "Twelve years ago, a colossal demon fox terrorized the world. During the monster's attack on the Hidden Leaf Village, the Hokage—the village's leader and most powerful ninja—sealed the demon fox into the body of his newborn son Naruto.",
            genres = genres("Action", "Adventure", "Fantasy"),
            status = "Finished Airing",
            broadcast = broadcast("Thursdays at 19:30 (JST)", "Thursdays", "19:30"),
            streaming = streaming(
                "Crunchyroll" to "http://www.crunchyroll.com/series-280621",
                "Netflix" to "https://www.netflix.com/title/70205012",
            ),
            relations = fakeRelations[20] ?: emptyList(),
            season = "fall",
            year = 2002,
        ),
        9253 to AnimeFullDataDto(
            malId = 9253,
            title = "Steins;Gate",
            titleEnglish = "Steins;Gate",
            titleJapanese = "STEINS;GATE",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/1935/127974l.jpg"),
            episodes = 24,
            score = 9.07,
            synopsis = "Eccentric scientist Rintarou Okabe has a never-ending thirst for scientific exploration. Together with his ditzy but well-meaning friend Mayuri Shiina and his roommate Itaru Hashida, Okabe founds the Future Gadget Laboratory in the hopes of creating applicable scientific theories.",
            genres = genres("Drama", "Sci-Fi", "Suspense"),
            status = "Finished Airing",
            broadcast = broadcast("Wednesdays at 02:05 (JST)", "Wednesdays", "02:05"),
            streaming = streaming(
                "Crunchyroll" to "http://www.crunchyroll.com/series-229050",
                "Netflix" to "https://www.netflix.com/",
            ),
            relations = fakeRelations[9253] ?: emptyList(),
            season = "spring",
            year = 2011,
        ),
        52991 to AnimeFullDataDto(
            malId = 52991,
            title = "Sousou no Frieren",
            titleEnglish = "Frieren: Beyond Journey's End",
            titleJapanese = "葬送のフリーレン",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/1015/138006.jpg"),
            episodes = 28,
            score = 9.28,
            synopsis = "During their decade-long quest to defeat the Demon King, the members of the hero's party—Himmel himself, the priest Heiter, the dwarf warrior Eisen, and the elven mage Frieren—forge bonds through adventures and battles, creating unforgettable precious memories for most of them.",
            genres = genres("Adventure", "Drama", "Fantasy"),
            status = "Finished Airing",
            broadcast = broadcast("Fridays at 23:00 (JST)", "Fridays", "23:00"),
            streaming = streaming(
                "Crunchyroll" to "https://www.crunchyroll.com/series/GG5H5XQ7D",
            ),
            relations = fakeRelations[52991] ?: emptyList(),
            season = "fall",
            year = 2023,
        ),
        59978 to AnimeFullDataDto(
            malId = 59978,
            title = "Sousou no Frieren 2nd Season",
            titleEnglish = "Frieren: Beyond Journey's End Season 2",
            titleJapanese = "葬送のフリーレン 第2期",
            type = "TV",
            images = images("https://myanimelist.net/images/anime/1921/154528.jpg"),
            episodes = 10,
            score = 9.16,
            synopsis = "Following the First-Class Mage Exam, the trio—elven mage Frieren, warrior Stark, and first-class mage Fern—gains access to the dangerous Northern Plateau. As the party presses onward toward Aureole, formidable adversaries force Stark to confront his insecurities.",
            genres = genres("Adventure", "Drama", "Fantasy"),
            status = "Currently Airing",
            broadcast = broadcast("Thursdays at 23:30 (JST)", "Thursdays", "23:30"),
            streaming = streaming(
                "Crunchyroll" to "https://www.crunchyroll.com/series/GG5H5XQ7D",
            ),
            relations = fakeRelations[59978] ?: emptyList(),
            season = "winter",
            year = 2026,
        ),
        56805 to AnimeFullDataDto(
            malId = 56805,
            title = "Yuusha",
            titleEnglish = "The Brave",
            titleJapanese = "勇者",
            type = "Music",
            images = images("https://myanimelist.net/images/anime/1947/138863.jpg"),
            episodes = 1,
            score = 7.86,
            synopsis = "Music video for the song Yuusha by YOASOBI. The song was used as the first opening theme of the anime Sousou no Frieren.",
            genres = emptyList(),
            status = "Finished Airing",
            broadcast = null,
            streaming = emptyList(),
            relations = fakeRelations[56805] ?: emptyList(),
            season = "fall",
            year = 2023,
        ),
    )

    private fun images(url: String) = AnimeImagesDto(
        jpg = ImageUrlDto(largeImageUrl = url, imageUrl = url)
    )

    private fun genres(vararg names: String) = names.map { GenreDto(name = it) }

    private fun aired(from: String) = AiredDto(from = from)

    private fun broadcast(string: String, day: String, time: String, timezone: String = "Asia/Tokyo") =
        BroadcastDto(string = string, day = day, time = time, timezone = timezone)

    private fun streaming(vararg pairs: Pair<String, String>) =
        pairs.map { (name, url) -> StreamingDto(name = name, url = url) }
}
