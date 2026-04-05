package com.vuzeda.animewatchlist.tracker

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.Season
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
            imageUrl = "https://cdn.myanimelist.net/images/anime/10/47347.jpg",
            synopsis = "Centuries ago, mankind was slaughtered to near extinction by monstrous humanoid creatures called Titans.",
            userRating = 9,
            status = WatchStatus.WATCHING,
            genres = listOf("Action", "Drama", "Fantasy"),
            addedAt = now - 86400000 * 30
        ) to listOf(
            Season(animeId = 0, malId = 16498, title = "Attack on Titan", imageUrl = "https://cdn.myanimelist.net/images/anime/10/47347.jpg", episodeCount = 25, watchedEpisodeCount = 18, score = 8.54, orderIndex = 0),
            Season(animeId = 0, malId = 25777, title = "Attack on Titan Season 2", imageUrl = "https://cdn.myanimelist.net/images/anime/4/84177.jpg", episodeCount = 12, watchedEpisodeCount = 8, score = 8.48, orderIndex = 1),
        ),
        Anime(
            title = "One Punch Man",
            imageUrl = "https://cdn.myanimelist.net/images/anime/12/76049.jpg",
            synopsis = "The seemingly unimpressive Saitama has a rather unique hobby: being a hero.",
            userRating = 8,
            status = WatchStatus.COMPLETED,
            genres = listOf("Action", "Comedy", "Sci-Fi", "Supernatural"),
            addedAt = now - 86400000 * 60
        ) to listOf(
            Season(animeId = 0, malId = 21, title = "One Punch Man", episodeCount = 12, watchedEpisodeCount = 12, score = 8.50, orderIndex = 0),
            Season(animeId = 0, malId = 30276, title = "One Punch Man Season 2", episodeCount = 12, watchedEpisodeCount = 12, score = 7.43, orderIndex = 1)
        ),
        Anime(
            title = "Death Note",
            imageUrl = "https://cdn.myanimelist.net/images/anime/9/9453.jpg",
            synopsis = "A shinigami, as a god of death, can kill any person—provided they see their victim's face and write their name in a Death Note.",
            userRating = 10,
            status = WatchStatus.COMPLETED,
            genres = listOf("Mystery", "Psychological", "Supernatural", "Thriller"),
            addedAt = now - 86400000 * 90
        ) to listOf(
            Season(animeId = 0, malId = 1535, title = "Death Note", episodeCount = 37, watchedEpisodeCount = 37, score = 8.62, orderIndex = 0)
        ),
        Anime(
            title = "Hunter x Hunter (2011)",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1337/99013.jpg",
            synopsis = "Twelve-year-old Gon Freecss discovers that the father he had always been told was dead was alive.",
            userRating = 9,
            status = WatchStatus.WATCHING,
            genres = listOf("Action", "Adventure", "Fantasy"),
            addedAt = now - 86400000 * 14
        ) to listOf(
            Season(animeId = 0, malId = 11061, title = "Hunter x Hunter (2011)", episodeCount = 148, watchedEpisodeCount = 75, score = 9.04, orderIndex = 0)
        ),
        Anime(
            title = "Gintama",
            imageUrl = "https://cdn.myanimelist.net/images/anime/3/72078.jpg",
            synopsis = "Gintoki, Shinpachi, and Kagura return as the fun-loving but broke members of the Yorozuya trio.",
            userRating = 8,
            status = WatchStatus.ON_HOLD,
            genres = listOf("Action", "Comedy", "Drama", "Sci-Fi"),
            addedAt = now - 86400000 * 45
        ) to listOf(
            Season(animeId = 0, malId = 28977, title = "Gintama°", episodeCount = 51, watchedEpisodeCount = 20, score = 9.06, orderIndex = 0)
        ),
        Anime(
            title = "Spy x Family",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1441/122795.jpg",
            synopsis = "Twilight, the greatest spy for Westalis, must infiltrate a politician's son's school.",
            userRating = 9,
            status = WatchStatus.COMPLETED,
            genres = listOf("Action", "Comedy", "Slice of Life"),
            addedAt = now - 86400000 * 20
        ) to listOf(
            Season(animeId = 0, malId = 50265, title = "Spy x Family", episodeCount = 12, watchedEpisodeCount = 12, score = 8.53, orderIndex = 0)
        ),
        Anime(
            title = "Fullmetal Alchemist: Brotherhood",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1208/94745.jpg",
            synopsis = "After a horrific alchemy experiment goes wrong, brothers Edward and Alphonse Elric are left in a catastrophic new reality.",
            status = WatchStatus.PLAN_TO_WATCH,
            genres = listOf("Action", "Adventure", "Drama", "Fantasy"),
            addedAt = now - 86400000 * 2
        ) to listOf(
            Season(animeId = 0, malId = 5114, title = "Fullmetal Alchemist: Brotherhood", episodeCount = 64, score = 9.09, orderIndex = 0)
        ),
        Anime(
            title = "Naruto",
            imageUrl = "https://cdn.myanimelist.net/images/anime/13/17405.jpg",
            synopsis = "Moments prior to Naruto Uzumaki's birth, a huge demon known as the Kyuubi attacked Konohagakure.",
            userRating = 7,
            status = WatchStatus.DROPPED,
            genres = listOf("Action", "Adventure", "Fantasy"),
            addedAt = now - 86400000 * 120
        ) to listOf(
            Season(animeId = 0, malId = 20, title = "Naruto", episodeCount = 220, watchedEpisodeCount = 135, score = 8.0, orderIndex = 0)
        ),
        Anime(
            title = "Steins;Gate",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1935/127974.jpg",
            synopsis = "Eccentric scientist Rintarou Okabe has a never-ending thirst for scientific exploration.",
            userRating = 9,
            status = WatchStatus.WATCHING,
            genres = listOf("Drama", "Sci-Fi", "Suspense"),
            addedAt = now - 86400000 * 7
        ) to listOf(
            Season(animeId = 0, malId = 9253, title = "Steins;Gate", episodeCount = 24, watchedEpisodeCount = 10, score = 9.07, orderIndex = 0)
        )
    )
}
