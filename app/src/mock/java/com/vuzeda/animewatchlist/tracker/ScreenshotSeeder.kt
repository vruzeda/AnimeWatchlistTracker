package com.vuzeda.animewatchlist.tracker

import com.vuzeda.animewatchlist.tracker.data.local.dao.AnimeDao
import com.vuzeda.animewatchlist.tracker.data.local.entity.AnimeEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenshotSeeder @Inject constructor(
    private val animeDao: AnimeDao
) {
    suspend fun seedIfEmpty() {
        val existing = animeDao.observeAll().first()
        if (existing.isNotEmpty()) return

        sampleAnime.forEach { animeDao.insert(it) }
    }

    private val now = System.currentTimeMillis()

    private val sampleAnime = listOf(
        AnimeEntity(
            malId = 16498,
            title = "Attack on Titan",
            imageUrl = "https://cdn.myanimelist.net/images/anime/10/47347.jpg",
            synopsis = "Centuries ago, mankind was slaughtered to near extinction by monstrous humanoid creatures called Titans, forcing humans to hide in fear behind enormous concentric walls.",
            episodeCount = 25,
            currentEpisode = 18,
            score = 8.54,
            userRating = 9,
            status = "WATCHING",
            genres = "Action,Drama,Fantasy,Military",
            addedAt = now - 86400000 * 30
        ),
        AnimeEntity(
            malId = 21,
            title = "One Punch Man",
            imageUrl = "https://cdn.myanimelist.net/images/anime/12/76049.jpg",
            synopsis = "The seemingly unimpressive Saitama has a rather unique hobby: being a hero. In order to pursue his childhood dream, Saitama relentlessly trained for three years, losing all of his hair in the process.",
            episodeCount = 12,
            currentEpisode = 12,
            score = 8.50,
            userRating = 8,
            status = "COMPLETED",
            genres = "Action,Comedy,Sci-Fi,Supernatural",
            addedAt = now - 86400000 * 60
        ),
        AnimeEntity(
            malId = 1535,
            title = "Death Note",
            imageUrl = "https://cdn.myanimelist.net/images/anime/9/9453.jpg",
            synopsis = "A shinigami, as a god of death, can kill any person—provided they see their victim's face and write their victim's name in a notebook called a Death Note.",
            episodeCount = 37,
            currentEpisode = 37,
            score = 8.62,
            userRating = 10,
            status = "COMPLETED",
            genres = "Mystery,Psychological,Supernatural,Thriller",
            addedAt = now - 86400000 * 90
        ),
        AnimeEntity(
            malId = 11061,
            title = "Hunter x Hunter (2011)",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1337/99013.jpg",
            synopsis = "Twelve-year-old Gon Freecss one day discovers that the father he had always been told was dead was alive.",
            episodeCount = 148,
            currentEpisode = 75,
            score = 9.04,
            userRating = 9,
            status = "WATCHING",
            genres = "Action,Adventure,Fantasy",
            addedAt = now - 86400000 * 14
        ),
        AnimeEntity(
            malId = 30276,
            title = "One Punch Man Season 2",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1247/122044.jpg",
            synopsis = "In the wake of defeating Boros and his mighty army, Saitama has returned to his unremarkable everyday life in Z-City.",
            episodeCount = 12,
            currentEpisode = 0,
            score = 7.43,
            status = "PLAN_TO_WATCH",
            genres = "Action,Comedy,Sci-Fi",
            addedAt = now - 86400000 * 5
        ),
        AnimeEntity(
            malId = 28977,
            title = "Gintama°",
            imageUrl = "https://cdn.myanimelist.net/images/anime/3/72078.jpg",
            synopsis = "Gintoki, Shinpachi, and Kagura return as the fun-loving but broke members of the Yorozuya trio.",
            episodeCount = 51,
            currentEpisode = 20,
            score = 9.06,
            userRating = 8,
            status = "ON_HOLD",
            genres = "Action,Comedy,Drama,Sci-Fi",
            addedAt = now - 86400000 * 45
        ),
        AnimeEntity(
            malId = 50265,
            title = "Spy x Family",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1441/122795.jpg",
            synopsis = "Twilight, the greatest spy for the nation of Westalis, has to investigate Ostanian politician Donovan Desmond by infiltrating his son's school.",
            episodeCount = 12,
            currentEpisode = 12,
            score = 8.53,
            userRating = 9,
            status = "COMPLETED",
            genres = "Action,Comedy,Slice of Life",
            addedAt = now - 86400000 * 20
        ),
        AnimeEntity(
            malId = 5114,
            title = "Fullmetal Alchemist: Brotherhood",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1208/94745.jpg",
            synopsis = "After a horrific alchemy experiment goes wrong, brothers Edward and Alphonse Elric are left in a catastrophic new reality.",
            episodeCount = 64,
            currentEpisode = 0,
            score = 9.09,
            status = "PLAN_TO_WATCH",
            genres = "Action,Adventure,Drama,Fantasy",
            addedAt = now - 86400000 * 2
        ),
        AnimeEntity(
            malId = 20,
            title = "Naruto",
            imageUrl = "https://cdn.myanimelist.net/images/anime/13/17405.jpg",
            synopsis = "Moments prior to Naruto Uzumaki's birth, a huge demon known as the Kyuubi, the Nine-Tailed Fox, attacked Konohagakure.",
            episodeCount = 220,
            currentEpisode = 135,
            score = 8.0,
            userRating = 7,
            status = "DROPPED",
            genres = "Action,Adventure,Fantasy",
            addedAt = now - 86400000 * 120
        ),
        AnimeEntity(
            malId = 9253,
            title = "Steins;Gate",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1935/127974.jpg",
            synopsis = "Eccentric scientist Rintarou Okabe has a never-ending thirst for scientific exploration.",
            episodeCount = 24,
            currentEpisode = 10,
            score = 9.07,
            userRating = 9,
            status = "WATCHING",
            genres = "Drama,Sci-Fi,Suspense",
            addedAt = now - 86400000 * 7
        )
    )
}
