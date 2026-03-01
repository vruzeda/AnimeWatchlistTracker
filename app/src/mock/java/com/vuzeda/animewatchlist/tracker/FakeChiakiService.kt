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
                imageUrl = "https://cdn.myanimelist.net/images/anime/10/47347.jpg"
            ),
            ChiakiWatchOrderEntryDto(
                malId = 25777,
                title = "Attack on Titan Season 2",
                typeCode = 1,
                episodeCount = 12,
                score = 8.48,
                imageUrl = "https://cdn.myanimelist.net/images/anime/4/84177.jpg"
            )
        ),
        25777 to listOf(
            ChiakiWatchOrderEntryDto(
                malId = 16498,
                title = "Attack on Titan",
                typeCode = 1,
                episodeCount = 25,
                score = 8.54,
                imageUrl = "https://cdn.myanimelist.net/images/anime/10/47347.jpg"
            ),
            ChiakiWatchOrderEntryDto(
                malId = 25777,
                title = "Attack on Titan Season 2",
                typeCode = 1,
                episodeCount = 12,
                score = 8.48,
                imageUrl = "https://cdn.myanimelist.net/images/anime/4/84177.jpg"
            )
        ),
        38000 to listOf(
            ChiakiWatchOrderEntryDto(
                malId = 38000,
                title = "Demon Slayer: Kimetsu no Yaiba",
                typeCode = 1,
                episodeCount = 26,
                score = 8.45,
                imageUrl = "https://cdn.myanimelist.net/images/anime/1286/99889.jpg"
            ),
            ChiakiWatchOrderEntryDto(
                malId = 47778,
                title = "Demon Slayer: Mugen Train Arc",
                typeCode = 1,
                episodeCount = 7,
                score = 8.20,
                imageUrl = null
            )
        )
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
