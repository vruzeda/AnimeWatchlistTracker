package com.vuzeda.animewatchlist.tracker

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.StreamingInfo
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.localdatasource.AnimeLocalDataSource
import com.vuzeda.animewatchlist.tracker.module.localdatasource.SeasonLocalDataSource
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenshotSeeder @Inject constructor(
    private val animeLocalDataSource: AnimeLocalDataSource,
    private val seasonLocalDataSource: SeasonLocalDataSource
) {
    suspend fun seedIfEmpty() {
        val existing = animeLocalDataSource.observeAll().first()
        if (existing.isNotEmpty()) return

        sampleData.forEach { (anime, seasons) ->
            val animeId = animeLocalDataSource.insert(anime)
            seasonLocalDataSource.insertAll(seasons.map { it.copy(animeId = animeId) })
        }
    }

    private val now = System.currentTimeMillis()

    private val sampleData = listOf(
        Anime(
            title = "Attack on Titan",
            titleEnglish = "Attack on Titan",
            titleJapanese = "進撃の巨人",
            imageUrl = "https://myanimelist.net/images/anime/10/47347l.jpg",
            synopsis = "Centuries ago, mankind was slaughtered to near extinction by monstrous humanoid creatures called Titans, forcing humans to hide in fear behind enormous concentric walls.",
            userRating = 9,
            status = WatchStatus.WATCHING,
            genres = listOf("Action", "Award Winning", "Drama", "Suspense"),
            addedAt = now - 86400000 * 30
        ) to listOf(
            Season(
                animeId = 0,
                malId = 16498,
                title = "Attack on Titan",
                titleEnglish = "Attack on Titan",
                titleJapanese = "進撃の巨人",
                imageUrl = "https://myanimelist.net/images/anime/10/47347l.jpg",
                episodeCount = 25,
                watchedEpisodeCount = 18,
                status = WatchStatus.WATCHING,
                score = 8.57,
                orderIndex = 0,
                airingStatus = "Finished Airing",
                broadcastInfo = "Sundays at 01:58 (JST)",
                broadcastDay = "Sundays",
                broadcastTime = "01:58",
                broadcastTimezone = "Asia/Tokyo",
                streamingLinks = listOf(
                    StreamingInfo("Crunchyroll", "http://www.crunchyroll.com/series-280312"),
                    StreamingInfo("Netflix", "https://www.netflix.com/title/70299043"),
                ),
                airingSeasonName = "spring",
                airingSeasonYear = 2013,
            ),
            Season(
                animeId = 0,
                malId = 25777,
                title = "Attack on Titan Season 2",
                titleEnglish = "Attack on Titan Season 2",
                titleJapanese = "進撃の巨人 Season2",
                imageUrl = "https://myanimelist.net/images/anime/4/84177l.jpg",
                episodeCount = 12,
                watchedEpisodeCount = 8,
                status = WatchStatus.WATCHING,
                score = 8.54,
                orderIndex = 1,
                airingStatus = "Finished Airing",
                broadcastInfo = "Saturdays at 22:00 (JST)",
                broadcastDay = "Saturdays",
                broadcastTime = "22:00",
                broadcastTimezone = "Asia/Tokyo",
                streamingLinks = listOf(
                    StreamingInfo("Crunchyroll", "http://www.crunchyroll.com/series-254209"),
                    StreamingInfo("Netflix", "https://www.netflix.com/title/70299043"),
                ),
                airingSeasonName = "spring",
                airingSeasonYear = 2017,
            ),
        ),
        Anime(
            title = "One Punch Man",
            titleEnglish = "One-Punch Man",
            titleJapanese = "ワンパンマン",
            imageUrl = "https://myanimelist.net/images/anime/12/76049l.jpg",
            synopsis = "The seemingly unimpressive Saitama has a rather unique hobby: being a hero. In order to pursue his childhood dream, Saitama relentlessly trained for three years, losing all of his hair in the process.",
            userRating = 8,
            status = WatchStatus.COMPLETED,
            genres = listOf("Action", "Comedy"),
            addedAt = now - 86400000 * 60
        ) to listOf(
            Season(
                animeId = 0,
                malId = 30276,
                title = "One Punch Man",
                titleEnglish = "One-Punch Man",
                titleJapanese = "ワンパンマン",
                imageUrl = "https://myanimelist.net/images/anime/12/76049l.jpg",
                episodeCount = 12,
                watchedEpisodeCount = 12,
                status = WatchStatus.COMPLETED,
                score = 8.48,
                orderIndex = 0,
                airingStatus = "Finished Airing",
                broadcastInfo = "Mondays at 01:05 (JST)",
                broadcastDay = "Mondays",
                broadcastTime = "01:05",
                broadcastTimezone = "Asia/Tokyo",
                streamingLinks = listOf(
                    StreamingInfo("Crunchyroll", "http://www.crunchyroll.com/series-277822"),
                    StreamingInfo("Netflix", "https://www.netflix.com/title/80117291"),
                ),
                airingSeasonName = "fall",
                airingSeasonYear = 2015,
            ),
            Season(
                animeId = 0,
                malId = 34134,
                title = "One Punch Man 2nd Season",
                titleEnglish = "One-Punch Man Season 2",
                titleJapanese = "ワンパンマン 2期",
                imageUrl = "https://myanimelist.net/images/anime/1247/122044l.jpg",
                episodeCount = 12,
                watchedEpisodeCount = 12,
                status = WatchStatus.COMPLETED,
                score = 7.53,
                orderIndex = 1,
                airingStatus = "Finished Airing",
                broadcastInfo = "Wednesdays at 01:35 (JST)",
                broadcastDay = "Wednesdays",
                broadcastTime = "01:35",
                broadcastTimezone = "Asia/Tokyo",
                streamingLinks = listOf(
                    StreamingInfo("Crunchyroll", "http://www.crunchyroll.com/series-277822"),
                ),
                airingSeasonName = "spring",
                airingSeasonYear = 2019,
            ),
        ),
        Anime(
            title = "Death Note",
            titleEnglish = "Death Note",
            titleJapanese = "デスノート",
            imageUrl = "https://myanimelist.net/images/anime/1079/138100l.jpg",
            synopsis = "A shinigami, as a god of death, can kill any person—provided they see their victim's face and write their name in a Death Note.",
            userRating = 10,
            status = WatchStatus.COMPLETED,
            genres = listOf("Supernatural", "Suspense"),
            addedAt = now - 86400000 * 90
        ) to listOf(
            Season(
                animeId = 0,
                malId = 1535,
                title = "Death Note",
                titleEnglish = "Death Note",
                titleJapanese = "デスノート",
                imageUrl = "https://myanimelist.net/images/anime/1079/138100l.jpg",
                episodeCount = 37,
                watchedEpisodeCount = 37,
                status = WatchStatus.COMPLETED,
                score = 8.62,
                orderIndex = 0,
                airingStatus = "Finished Airing",
                broadcastInfo = "Wednesdays at 00:56 (JST)",
                broadcastDay = "Wednesdays",
                broadcastTime = "00:56",
                broadcastTimezone = "Asia/Tokyo",
                streamingLinks = listOf(
                    StreamingInfo("Crunchyroll", "http://www.crunchyroll.com/series-278866"),
                    StreamingInfo("Netflix", "https://www.netflix.com/title/70204970"),
                ),
                airingSeasonName = "fall",
                airingSeasonYear = 2006,
            ),
        ),
        Anime(
            title = "Hunter x Hunter (2011)",
            titleEnglish = "Hunter x Hunter",
            titleJapanese = "HUNTER×HUNTER（ハンター×ハンター）",
            imageUrl = "https://myanimelist.net/images/anime/1337/99013l.jpg",
            synopsis = "Twelve-year-old Gon Freecss discovers that the father he had always been told was dead was alive.",
            userRating = 9,
            status = WatchStatus.WATCHING,
            genres = listOf("Action", "Adventure", "Fantasy"),
            addedAt = now - 86400000 * 14
        ) to listOf(
            Season(
                animeId = 0,
                malId = 11061,
                title = "Hunter x Hunter (2011)",
                titleEnglish = "Hunter x Hunter",
                titleJapanese = "HUNTER×HUNTER（ハンター×ハンター）",
                imageUrl = "https://myanimelist.net/images/anime/1337/99013l.jpg",
                episodeCount = 148,
                watchedEpisodeCount = 75,
                status = WatchStatus.WATCHING,
                score = 9.03,
                orderIndex = 0,
                airingStatus = "Finished Airing",
                broadcastInfo = "Sundays at 10:55 (JST)",
                broadcastDay = "Sundays",
                broadcastTime = "10:55",
                broadcastTimezone = "Asia/Tokyo",
                streamingLinks = listOf(
                    StreamingInfo("Crunchyroll", "http://www.crunchyroll.com/series-237800"),
                    StreamingInfo("Netflix", "https://www.netflix.com/title/70300472"),
                ),
                airingSeasonName = "fall",
                airingSeasonYear = 2011,
            ),
        ),
        Anime(
            title = "Gintama°",
            titleEnglish = "Gintama Season 4",
            titleJapanese = "銀魂°",
            imageUrl = "https://myanimelist.net/images/anime/3/72078l.jpg",
            synopsis = "Gintoki, Shinpachi, and Kagura return as the fun-loving but broke members of the Yorozuya trio.",
            userRating = 8,
            status = WatchStatus.ON_HOLD,
            genres = listOf("Action", "Comedy", "Sci-Fi"),
            addedAt = now - 86400000 * 45
        ) to listOf(
            Season(
                animeId = 0,
                malId = 28977,
                title = "Gintama°",
                titleEnglish = "Gintama Season 4",
                titleJapanese = "銀魂°",
                imageUrl = "https://myanimelist.net/images/anime/3/72078l.jpg",
                episodeCount = 51,
                watchedEpisodeCount = 20,
                status = WatchStatus.ON_HOLD,
                score = 9.05,
                orderIndex = 0,
                airingStatus = "Finished Airing",
                broadcastInfo = "Wednesdays at 18:00 (JST)",
                broadcastDay = "Wednesdays",
                broadcastTime = "18:00",
                broadcastTimezone = "Asia/Tokyo",
                streamingLinks = listOf(
                    StreamingInfo("Crunchyroll", "http://www.crunchyroll.com/series-47620"),
                ),
                airingSeasonName = "spring",
                airingSeasonYear = 2015,
            ),
        ),
        Anime(
            title = "Spy x Family",
            titleJapanese = "SPY×FAMILY",
            imageUrl = "https://myanimelist.net/images/anime/1441/122795l.jpg",
            synopsis = "Twilight, the greatest spy for Westalis, must infiltrate a politician's son's school.",
            userRating = 9,
            status = WatchStatus.COMPLETED,
            genres = listOf("Action", "Award Winning", "Comedy"),
            addedAt = now - 86400000 * 20
        ) to listOf(
            Season(
                animeId = 0,
                malId = 50265,
                title = "Spy x Family",
                titleJapanese = "SPY×FAMILY",
                imageUrl = "https://myanimelist.net/images/anime/1441/122795l.jpg",
                episodeCount = 12,
                watchedEpisodeCount = 12,
                status = WatchStatus.COMPLETED,
                score = 8.42,
                orderIndex = 0,
                airingStatus = "Finished Airing",
                broadcastInfo = "Saturdays at 23:00 (JST)",
                broadcastDay = "Saturdays",
                broadcastTime = "23:00",
                broadcastTimezone = "Asia/Tokyo",
                streamingLinks = listOf(
                    StreamingInfo("Crunchyroll", "http://www.crunchyroll.com/series-282626"),
                    StreamingInfo("Netflix", "https://www.netflix.com/"),
                    StreamingInfo("Hulu", "https://www.hulu.com/"),
                ),
                airingSeasonName = "spring",
                airingSeasonYear = 2022,
            ),
        ),
        Anime(
            title = "Fullmetal Alchemist: Brotherhood",
            titleEnglish = "Fullmetal Alchemist: Brotherhood",
            titleJapanese = "鋼の錬金術師 FULLMETAL ALCHEMIST",
            imageUrl = "https://myanimelist.net/images/anime/1208/94745l.jpg",
            synopsis = "After a horrific alchemy experiment goes wrong, brothers Edward and Alphonse Elric are left in a catastrophic new reality.",
            status = WatchStatus.PLAN_TO_WATCH,
            genres = listOf("Action", "Adventure", "Drama", "Fantasy"),
            addedAt = now - 86400000 * 2
        ) to listOf(
            Season(
                animeId = 0,
                malId = 5114,
                title = "Fullmetal Alchemist: Brotherhood",
                titleEnglish = "Fullmetal Alchemist: Brotherhood",
                titleJapanese = "鋼の錬金術師 FULLMETAL ALCHEMIST",
                imageUrl = "https://myanimelist.net/images/anime/1208/94745l.jpg",
                episodeCount = 64,
                status = WatchStatus.PLAN_TO_WATCH,
                score = 9.11,
                orderIndex = 0,
                airingStatus = "Finished Airing",
                broadcastInfo = "Sundays at 17:00 (JST)",
                broadcastDay = "Sundays",
                broadcastTime = "17:00",
                broadcastTimezone = "Asia/Tokyo",
                streamingLinks = listOf(
                    StreamingInfo("Crunchyroll", "http://www.crunchyroll.com/series-271031"),
                ),
                airingSeasonName = "spring",
                airingSeasonYear = 2009,
            ),
        ),
        Anime(
            title = "Naruto",
            titleEnglish = "Naruto",
            titleJapanese = "ナルト",
            imageUrl = "https://myanimelist.net/images/anime/1141/142503l.jpg",
            synopsis = "Moments prior to Naruto Uzumaki's birth, a huge demon known as the Kyuubi attacked Konohagakure.",
            userRating = 7,
            status = WatchStatus.DROPPED,
            genres = listOf("Action", "Adventure", "Fantasy"),
            addedAt = now - 86400000 * 120
        ) to listOf(
            Season(
                animeId = 0,
                malId = 20,
                title = "Naruto",
                titleEnglish = "Naruto",
                titleJapanese = "ナルト",
                imageUrl = "https://myanimelist.net/images/anime/1141/142503l.jpg",
                episodeCount = 220,
                watchedEpisodeCount = 135,
                status = WatchStatus.DROPPED,
                score = 8.02,
                orderIndex = 0,
                airingStatus = "Finished Airing",
                broadcastInfo = "Thursdays at 19:30 (JST)",
                broadcastDay = "Thursdays",
                broadcastTime = "19:30",
                broadcastTimezone = "Asia/Tokyo",
                streamingLinks = listOf(
                    StreamingInfo("Crunchyroll", "http://www.crunchyroll.com/series-280621"),
                    StreamingInfo("Netflix", "https://www.netflix.com/title/70205012"),
                ),
                airingSeasonName = "fall",
                airingSeasonYear = 2002,
            ),
        ),
        Anime(
            title = "Steins;Gate",
            titleEnglish = "Steins;Gate",
            titleJapanese = "STEINS;GATE",
            imageUrl = "https://myanimelist.net/images/anime/1935/127974l.jpg",
            synopsis = "Eccentric scientist Rintarou Okabe has a never-ending thirst for scientific exploration.",
            userRating = 9,
            status = WatchStatus.WATCHING,
            genres = listOf("Drama", "Sci-Fi", "Suspense"),
            addedAt = now - 86400000 * 7
        ) to listOf(
            Season(
                animeId = 0,
                malId = 9253,
                title = "Steins;Gate",
                titleEnglish = "Steins;Gate",
                titleJapanese = "STEINS;GATE",
                imageUrl = "https://myanimelist.net/images/anime/1935/127974l.jpg",
                episodeCount = 24,
                watchedEpisodeCount = 10,
                status = WatchStatus.WATCHING,
                score = 9.07,
                orderIndex = 0,
                airingStatus = "Finished Airing",
                broadcastInfo = "Wednesdays at 02:05 (JST)",
                broadcastDay = "Wednesdays",
                broadcastTime = "02:05",
                broadcastTimezone = "Asia/Tokyo",
                streamingLinks = listOf(
                    StreamingInfo("Crunchyroll", "http://www.crunchyroll.com/series-229050"),
                    StreamingInfo("Netflix", "https://www.netflix.com/"),
                ),
                airingSeasonName = "spring",
                airingSeasonYear = 2011,
            ),
        ),
    )
}
