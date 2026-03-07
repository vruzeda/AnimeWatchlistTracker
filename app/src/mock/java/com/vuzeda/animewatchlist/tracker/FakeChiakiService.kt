package com.vuzeda.animewatchlist.tracker

import com.vuzeda.animewatchlist.tracker.data.api.dto.ChiakiWatchOrderEntryDto
import com.vuzeda.animewatchlist.tracker.data.api.service.ChiakiService

class FakeChiakiService : ChiakiService {

    private val fakeWatchOrders: Map<Int, List<ChiakiWatchOrderEntryDto>> = mapOf(
        16498 to listOf(
            ChiakiWatchOrderEntryDto(
                malId = 16498,
                title = "Attack on Titan",
                typeCode = 1,
                episodeCount = 25,
                score = 8.54,
                imageUrl = "https://cdn.myanimelist.net/images/anime/10/47347.jpg",
                isMainSeries = true,
            ),
            ChiakiWatchOrderEntryDto(
                malId = 25777,
                title = "Attack on Titan Season 2",
                typeCode = 1,
                episodeCount = 12,
                score = 8.48,
                imageUrl = "https://cdn.myanimelist.net/images/anime/4/84177.jpg",
                isMainSeries = true,
            )
        ),
        25777 to listOf(
            ChiakiWatchOrderEntryDto(
                malId = 16498,
                title = "Attack on Titan",
                typeCode = 1,
                episodeCount = 25,
                score = 8.54,
                imageUrl = "https://cdn.myanimelist.net/images/anime/10/47347.jpg",
                isMainSeries = true,
            ),
            ChiakiWatchOrderEntryDto(
                malId = 25777,
                title = "Attack on Titan Season 2",
                typeCode = 1,
                episodeCount = 12,
                score = 8.48,
                imageUrl = "https://cdn.myanimelist.net/images/anime/4/84177.jpg",
                isMainSeries = true,
            )
        ),
        38000 to listOf(
            ChiakiWatchOrderEntryDto(
                malId = 38000,
                title = "Demon Slayer: Kimetsu no Yaiba",
                typeCode = 1,
                episodeCount = 26,
                score = 8.45,
                imageUrl = "https://cdn.myanimelist.net/images/anime/1286/99889.jpg",
                isMainSeries = true,
            ),
            ChiakiWatchOrderEntryDto(
                malId = 47778,
                title = "Demon Slayer: Mugen Train Arc",
                typeCode = 1,
                episodeCount = 7,
                score = 8.20,
                imageUrl = null,
                isMainSeries = true,
            )
        ),
        52991 to listOf(
            ChiakiWatchOrderEntryDto(
                malId = 56805,
                title = "Yuusha",
                titleEnglish = "The Brave",
                typeCode = 6,
                episodeCount = 1,
                score = 7.86,
                imageUrl = "https://chiaki.site/media/a/a2/56805.jpg",
                isMainSeries = false,
            ),
            ChiakiWatchOrderEntryDto(
                malId = 52991,
                title = "Sousou no Frieren",
                titleEnglish = "Frieren: Beyond Journey's End",
                typeCode = 1,
                episodeCount = 28,
                score = 9.27,
                imageUrl = "https://chiaki.site/media/a/ec/52991.jpg",
                isMainSeries = true,
            ),
            ChiakiWatchOrderEntryDto(
                malId = 59978,
                title = "Sousou no Frieren 2nd Season",
                titleEnglish = "Frieren: Beyond Journey's End Season 2",
                typeCode = 1,
                episodeCount = 10,
                score = 9.25,
                imageUrl = "https://chiaki.site/media/a/ec/59978.jpg",
                isMainSeries = true,
            ),
        ),
        59978 to listOf(
            ChiakiWatchOrderEntryDto(
                malId = 56805,
                title = "Yuusha",
                titleEnglish = "The Brave",
                typeCode = 6,
                episodeCount = 1,
                score = 7.86,
                imageUrl = "https://chiaki.site/media/a/a2/56805.jpg",
                isMainSeries = false,
            ),
            ChiakiWatchOrderEntryDto(
                malId = 52991,
                title = "Sousou no Frieren",
                titleEnglish = "Frieren: Beyond Journey's End",
                typeCode = 1,
                episodeCount = 28,
                score = 9.27,
                imageUrl = "https://chiaki.site/media/a/ec/52991.jpg",
                isMainSeries = true,
            ),
            ChiakiWatchOrderEntryDto(
                malId = 59978,
                title = "Sousou no Frieren 2nd Season",
                titleEnglish = "Frieren: Beyond Journey's End Season 2",
                typeCode = 1,
                episodeCount = 10,
                score = 9.25,
                imageUrl = "https://chiaki.site/media/a/ec/59978.jpg",
                isMainSeries = true,
            ),
        ),
        56805 to listOf(
            ChiakiWatchOrderEntryDto(
                malId = 56805,
                title = "Yuusha",
                titleEnglish = "The Brave",
                typeCode = 6,
                episodeCount = 1,
                score = 7.86,
                imageUrl = "https://chiaki.site/media/a/a2/56805.jpg",
                isMainSeries = false,
            ),
            ChiakiWatchOrderEntryDto(
                malId = 52991,
                title = "Sousou no Frieren",
                titleEnglish = "Frieren: Beyond Journey's End",
                typeCode = 1,
                episodeCount = 28,
                score = 9.27,
                imageUrl = "https://chiaki.site/media/a/ec/52991.jpg",
                isMainSeries = true,
            ),
            ChiakiWatchOrderEntryDto(
                malId = 59978,
                title = "Sousou no Frieren 2nd Season",
                titleEnglish = "Frieren: Beyond Journey's End Season 2",
                typeCode = 1,
                episodeCount = 10,
                score = 9.25,
                imageUrl = "https://chiaki.site/media/a/ec/59978.jpg",
                isMainSeries = true,
            ),
        ),
    )

    override suspend fun fetchWatchOrder(malId: Int): List<ChiakiWatchOrderEntryDto> =
        fakeWatchOrders[malId] ?: listOf(
            ChiakiWatchOrderEntryDto(
                malId = malId,
                title = "Unknown Anime",
                typeCode = 1,
                episodeCount = null,
                score = null,
                imageUrl = null
            )
        )
}
