package com.vuzeda.animewatchlist.tracker

import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeDataDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeFullDataDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeFullResponseDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeImagesDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeSearchResponseDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeSingleResponseDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.GenreDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.ImageUrlDto
import com.vuzeda.animewatchlist.tracker.data.api.service.JikanApiService

class FakeJikanApiService : JikanApiService {

    override suspend fun searchAnime(query: String, limit: Int): AnimeSearchResponseDto =
        AnimeSearchResponseDto(data = fakeResults)

    override suspend fun getAnimeById(malId: Int): AnimeSingleResponseDto {
        val anime = fakeResults.first { it.malId == malId }
        return AnimeSingleResponseDto(data = anime)
    }

    override suspend fun getAnimeFullById(malId: Int): AnimeFullResponseDto =
        AnimeFullResponseDto(
            data = AnimeFullDataDto(
                malId = malId,
                title = fakeResults.first { it.malId == malId }.title,
                episodes = fakeResults.first { it.malId == malId }.episodes,
                relations = emptyList()
            )
        )

    private val fakeResults = listOf(
        // Already in watchlist (seeded) — will show check + status chip
        AnimeDataDto(
            malId = 16498,
            title = "Attack on Titan",
            images = images("https://cdn.myanimelist.net/images/anime/10/47347.jpg"),
            synopsis = "Centuries ago, mankind was slaughtered to near extinction by monstrous humanoid creatures called Titans.",
            episodes = 25,
            score = 8.54,
            genres = genres("Action", "Drama", "Fantasy")
        ),
        AnimeDataDto(
            malId = 1535,
            title = "Death Note",
            images = images("https://cdn.myanimelist.net/images/anime/9/9453.jpg"),
            synopsis = "A shinigami, as a god of death, can kill any person—provided they see their victim's face and write their name in a Death Note.",
            episodes = 37,
            score = 8.62,
            genres = genres("Mystery", "Psychological", "Supernatural")
        ),
        AnimeDataDto(
            malId = 50265,
            title = "Spy x Family",
            images = images("https://cdn.myanimelist.net/images/anime/1441/122795.jpg"),
            synopsis = "Twilight, the greatest spy for Westalis, must investigate a politician by infiltrating his son's school.",
            episodes = 12,
            score = 8.53,
            genres = genres("Action", "Comedy", "Slice of Life")
        ),
        // NOT in watchlist — will show "+" add button
        AnimeDataDto(
            malId = 38000,
            title = "Demon Slayer: Kimetsu no Yaiba",
            images = images("https://cdn.myanimelist.net/images/anime/1286/99889.jpg"),
            synopsis = "Ever since the death of his father, the burden of supporting the family has fallen upon Tanjirou Kamado's shoulders.",
            episodes = 26,
            score = 8.45,
            genres = genres("Action", "Fantasy", "Historical")
        ),
        AnimeDataDto(
            malId = 21459,
            title = "Your Name.",
            images = images("https://cdn.myanimelist.net/images/anime/5/87048.jpg"),
            synopsis = "Mitsuha Miyamizu, a high school girl, yearns to live the life of a boy in the bustling city of Tokyo.",
            episodes = 1,
            score = 8.83,
            genres = genres("Drama", "Romance", "Supernatural")
        ),
        AnimeDataDto(
            malId = 40748,
            title = "Jujutsu Kaisen",
            images = images("https://cdn.myanimelist.net/images/anime/1171/109222.jpg"),
            synopsis = "Idly indulging in baseless paranormal activities with the Occult Club, Yuuji Itadori spends his days at either the clubroom or the hospital.",
            episodes = 24,
            score = 8.61,
            genres = genres("Action", "Drama", "Supernatural")
        ),
        AnimeDataDto(
            malId = 52991,
            title = "Oshi no Ko",
            images = images("https://cdn.myanimelist.net/images/anime/1812/134736.jpg"),
            synopsis = "Sixteen-year-old Ai Hoshino is a talented and beautiful idol who is adored by her fans.",
            episodes = 11,
            score = 8.54,
            genres = genres("Drama", "Supernatural")
        ),
        AnimeDataDto(
            malId = 51009,
            title = "Chainsaw Man",
            images = images("https://cdn.myanimelist.net/images/anime/1806/126216.jpg"),
            synopsis = "Denji has a simple dream—to live a happy and peaceful life, spending time with a girl he likes.",
            episodes = 12,
            score = 8.25,
            genres = genres("Action", "Fantasy", "Horror")
        )
    )

    private fun images(url: String) = AnimeImagesDto(
        jpg = ImageUrlDto(largeImageUrl = url, imageUrl = url)
    )

    private fun genres(vararg names: String) = names.map { GenreDto(name = it) }
}
