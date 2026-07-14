package com.vuzeda.animewatchlist.tracker

import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalEpisodeListPageDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalEpisodeRowDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service.MalEpisodeListService

class FakeMalEpisodeListService(
    private val fakeJikanApiService: FakeJikanApiService
) : MalEpisodeListService {

    override suspend fun fetchEpisodePage(malId: Int, page: Int): MalEpisodeListPageDto {
        val response = fakeJikanApiService.getAnimeEpisodes(malId = malId, page = page)
        return MalEpisodeListPageDto(
            episodes = response.data.map { episode ->
                MalEpisodeRowDto(
                    number = episode.malId,
                    title = episode.title,
                    airedIsoDate = episode.aired?.take(10)
                )
            },
            hasNextPage = response.pagination.hasNextPage
        )
    }
}
