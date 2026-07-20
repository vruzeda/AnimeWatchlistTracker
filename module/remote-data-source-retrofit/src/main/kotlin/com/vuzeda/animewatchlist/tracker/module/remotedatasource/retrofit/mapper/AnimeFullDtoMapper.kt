package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.BroadcastTime
import com.vuzeda.animewatchlist.tracker.module.domain.SequelInfo
import com.vuzeda.animewatchlist.tracker.module.domain.StreamingInfo
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeFullDataDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.BroadcastDto
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

fun AnimeFullDataDto.toAnimeFullDetails(): AnimeFullDetails = AnimeFullDetails(
    malId = malId,
    title = title,
    titleEnglish = titleEnglish,
    titleJapanese = titleJapanese,
    imageUrl = images?.jpg?.largeImageUrl ?: images?.jpg?.imageUrl,
    type = type ?: "Unknown",
    episodes = episodes,
    score = score,
    synopsis = synopsis,
    genres = genres?.map { it.name } ?: emptyList(),
    airingStatus = status,
    broadcastInfo = broadcast?.string,
    broadcastTime = broadcast?.toBroadcastTime(),
    streamingLinks = streaming?.map { StreamingInfo(name = it.name, url = it.url) } ?: emptyList(),
    sequels = extractRelations("Sequel"),
    prequels = extractRelations("Prequel"),
    airingSeasonName = season,
    airingSeasonYear = year
)

private fun BroadcastDto.toBroadcastTime(): BroadcastTime? =
    BroadcastTime(
        day = day,
        time = time,
        timezone = timezone
    )

private fun AnimeFullDataDto.extractRelations(relationType: String): List<SequelInfo> =
    relations
        ?.filter { it.relation == relationType }
        ?.flatMap { relation ->
            relation.entry
                .filter { it.type == "anime" }
                .map { SequelInfo(malId = it.malId, title = it.name) }
        }
        ?: emptyList()
