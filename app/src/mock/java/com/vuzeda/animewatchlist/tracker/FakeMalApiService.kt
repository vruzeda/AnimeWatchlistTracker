package com.vuzeda.animewatchlist.tracker

import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeDataDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeFullDataDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeImagesDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeSearchResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalAlternativeTitlesDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalAnimeDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalAnimeListResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalAnimeNodeWrapperDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalBroadcastDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalGenreDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalMainPictureDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalPagingDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalRelatedAnimeDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalRelatedNodeDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalStartSeasonDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.MalApiService

class FakeMalApiService(
    private val fakeTenraiApiService: FakeTenraiApiService
) : MalApiService {

    override suspend fun searchAnime(
        query: String,
        limit: Int,
        offset: Int,
        nsfw: Boolean,
        fields: String
    ): MalAnimeListResponseDto =
        fakeTenraiApiService.searchAnime(query = query, page = offset / limit + 1).toMalListResponse()

    override suspend fun getAnimeById(malId: Int, fields: String): MalAnimeDto =
        fakeTenraiApiService.getAnimeFullById(malId).data.toMalAnime()

    override suspend fun getSeasonAnime(
        year: Int,
        season: String,
        limit: Int,
        offset: Int,
        nsfw: Boolean,
        fields: String
    ): MalAnimeListResponseDto =
        fakeTenraiApiService.getSeasonAnime(year = year, season = season, page = offset / limit + 1)
            .toMalListResponse()
}

private fun AnimeSearchResponseDto.toMalListResponse(): MalAnimeListResponseDto =
    MalAnimeListResponseDto(
        data = data.map { MalAnimeNodeWrapperDto(it.toMalAnime()) },
        paging = MalPagingDto(
            next = if (pagination?.hasNextPage == true) "https://api.myanimelist.net/v2/anime?offset=next" else null
        )
    )

private fun AnimeDataDto.toMalAnime(): MalAnimeDto = MalAnimeDto(
    id = malId,
    title = title,
    alternativeTitles = MalAlternativeTitlesDto(en = titleEnglish, ja = titleJapanese),
    mainPicture = images.toMalMainPicture(),
    mediaType = type.toMalWireValue(),
    status = status.toMalWireValue(),
    numEpisodes = episodes ?: 0,
    mean = score,
    synopsis = synopsis,
    genres = genres?.map { MalGenreDto(it.name) }
)

private fun AnimeFullDataDto.toMalAnime(): MalAnimeDto = MalAnimeDto(
    id = malId,
    title = title,
    alternativeTitles = MalAlternativeTitlesDto(en = titleEnglish, ja = titleJapanese),
    mainPicture = images.toMalMainPicture(),
    mediaType = type.toMalWireValue(),
    status = status.toMalWireValue(),
    numEpisodes = episodes ?: 0,
    mean = score,
    synopsis = synopsis,
    genres = genres?.map { MalGenreDto(it.name) },
    broadcast = broadcast?.let { jikanBroadcast ->
        MalBroadcastDto(
            dayOfTheWeek = jikanBroadcast.day?.lowercase()?.trimEnd('s'),
            startTime = jikanBroadcast.time
        )
    },
    startSeason = if (season != null || year != null) {
        MalStartSeasonDto(year = year, season = season)
    } else {
        null
    },
    relatedAnime = relations?.flatMap { relation ->
        relation.entry
            .filter { it.type == "anime" }
            .map { entry ->
                MalRelatedAnimeDto(
                    node = MalRelatedNodeDto(id = entry.malId, title = entry.name),
                    relationType = relation.relation.lowercase()
                )
            }
    }
)

private fun AnimeImagesDto?.toMalMainPicture(): MalMainPictureDto? =
    this?.jpg?.let { MalMainPictureDto(medium = it.imageUrl, large = it.largeImageUrl) }

private fun String?.toMalWireValue(): String? = this?.lowercase()?.replace(' ', '_')
