package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeFullDataDto
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.module.domain.SequelInfo

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
    sequels = extractRelations("Sequel"),
    prequels = extractRelations("Prequel")
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
