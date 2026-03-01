package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeFullDataDto
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.SequelInfo

fun AnimeFullDataDto.toAnimeFullDetails(): AnimeFullDetails = AnimeFullDetails(
    malId = malId,
    title = title,
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
