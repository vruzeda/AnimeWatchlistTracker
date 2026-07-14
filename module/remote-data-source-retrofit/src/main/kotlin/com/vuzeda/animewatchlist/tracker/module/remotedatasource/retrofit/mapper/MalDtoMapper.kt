package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResultPage
import com.vuzeda.animewatchlist.tracker.module.domain.SeasonalAnimePage
import com.vuzeda.animewatchlist.tracker.module.domain.SequelInfo
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalAnimeDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalAnimeListResponseDto

private const val MAL_BROADCAST_TIMEZONE = "Asia/Tokyo"

fun MalAnimeDto.toSearchResult(): SearchResult = SearchResult(
    malId = id,
    title = title,
    titleEnglish = alternativeTitles?.en?.takeIf { it.isNotBlank() },
    titleJapanese = alternativeTitles?.ja?.takeIf { it.isNotBlank() },
    imageUrl = mainPicture?.large ?: mainPicture?.medium,
    synopsis = synopsis?.takeIf { it.isNotBlank() },
    episodeCount = numEpisodes?.takeIf { it > 0 },
    score = mean,
    type = mediaType.malMediaTypeToDisplayType(),
    genres = genres?.map { it.name } ?: emptyList()
)

fun MalAnimeListResponseDto.toSearchResultPage(currentPage: Int): SearchResultPage =
    SearchResultPage(
        results = data.map { it.node.toSearchResult() }.distinctBy { it.malId },
        hasNextPage = paging?.next != null,
        currentPage = currentPage
    )

fun MalAnimeListResponseDto.toSeasonalAnimePage(currentPage: Int): SeasonalAnimePage =
    SeasonalAnimePage(
        results = data.map { it.node.toSearchResult() }.distinctBy { it.malId },
        hasNextPage = paging?.next != null,
        currentPage = currentPage
    )

fun MalAnimeDto.toAnimeFullDetails(): AnimeFullDetails {
    val displayDay = broadcast?.dayOfTheWeek.malBroadcastDayToDisplayDay()
    val startTime = broadcast?.startTime
    return AnimeFullDetails(
        malId = id,
        title = title,
        titleEnglish = alternativeTitles?.en?.takeIf { it.isNotBlank() },
        titleJapanese = alternativeTitles?.ja?.takeIf { it.isNotBlank() },
        imageUrl = mainPicture?.large ?: mainPicture?.medium,
        type = mediaType.malMediaTypeToDisplayType(),
        episodes = numEpisodes?.takeIf { it > 0 },
        score = mean,
        synopsis = synopsis?.takeIf { it.isNotBlank() },
        genres = genres?.map { it.name } ?: emptyList(),
        airingStatus = status.malStatusToDisplayStatus(),
        broadcastInfo = composeBroadcastInfo(displayDay, startTime),
        broadcastDay = displayDay,
        broadcastTime = startTime,
        broadcastTimezone = displayDay?.let { MAL_BROADCAST_TIMEZONE },
        sequels = extractRelations("sequel"),
        prequels = extractRelations("prequel"),
        airingSeasonName = startSeason?.season,
        airingSeasonYear = startSeason?.year
    )
}

private fun composeBroadcastInfo(displayDay: String?, startTime: String?): String? = when {
    displayDay != null && startTime != null -> "$displayDay at $startTime (JST)"
    displayDay != null -> displayDay
    else -> null
}

private fun MalAnimeDto.extractRelations(relationType: String): List<SequelInfo> =
    relatedAnime
        ?.filter { it.relationType == relationType }
        ?.map { SequelInfo(malId = it.node.id, title = it.node.title) }
        ?: emptyList()
