package com.vuzeda.animewatchlist.tracker

import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AiredDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeDataDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeEpisodesResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeFullDataDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeFullResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeImagesDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeRelationDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeSearchResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.EpisodeDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.EpisodesPaginationDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.GenreDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.ImageUrlDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.RelatedEntryDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.SearchPaginationDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.JikanApiService
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class FakeJikanApiService : JikanApiService {

    override suspend fun searchAnime(query: String, limit: Int): AnimeSearchResponseDto =
        AnimeSearchResponseDto(data = fakeResults)

    override suspend fun getAnimeFullById(malId: Int): AnimeFullResponseDto {
        val anime = fakeResults.first { it.malId == malId }
        val relations = fakeRelations[malId] ?: emptyList()
        return AnimeFullResponseDto(
            data = AnimeFullDataDto(
                malId = malId,
                title = anime.title,
                episodes = anime.episodes,
                relations = relations
            )
        )
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
            synopsis= "Following the First-Class Mage Exam, the trio—elven mage Frieren, warrior Stark, and first-class mage Fern—gains access to the dangerous Northern Plateau. As the party presses onward toward Aureole, formidable adversaries force Stark to confront his insecurities, solidifying his resolve and his role as the party's frontliner. Meanwhile, Fern continues to cherish the gifts she has been blessed with throughout her life, each a reminder of those she holds dear.\n\nFrieren—still honoring her vow to understand humanity—revisits memories of her journey with the Hero's party and her fleeting encounter with a legendary figure. As she reflects on the passage of time, the elven mage quietly questions whether she has truly changed, yet in the small, almost subtle choices that she makes, there are signs that she might have become more human than she realizes.\n\n[Written by MAL Rewrite]",
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

    private val fakeRelations: Map<Int, List<AnimeRelationDto>> = mapOf(
        16498 to listOf(
            AnimeRelationDto(
                relation = "Sequel",
                entry = listOf(
                    RelatedEntryDto(malId = 25777, type = "anime", name = "Attack on Titan Season 2")
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

    private fun images(url: String) = AnimeImagesDto(
        jpg = ImageUrlDto(largeImageUrl = url, imageUrl = url)
    )

    private fun genres(vararg names: String) = names.map { GenreDto(name = it) }

    private fun aired(from: String) = AiredDto(
        from = from,
    )
}
