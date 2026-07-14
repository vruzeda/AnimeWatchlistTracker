package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit

import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import java.time.LocalDate

internal const val EPISODE_LIST_PAGE_SIZE = 100

internal suspend fun collectEpisodesAiredBetween(
    after: LocalDate,
    upTo: LocalDate,
    startingFromEpisode: Int?,
    fetchPage: suspend (page: Int) -> EpisodePage
): List<EpisodeInfo> {
    val startPage = maxOf(1, ((startingFromEpisode ?: 0) - 1) / EPISODE_LIST_PAGE_SIZE + 1)
    val accumulated = mutableListOf<EpisodeInfo>()
    var page = startPage
    var stopPagination = false

    while (!stopPagination) {
        val episodePage = fetchPage(page)

        for (episode in episodePage.episodes) {
            val airedDate = parseAiredDateFromString(episode.aired)
            if (airedDate != null) {
                if (airedDate.isAfter(upTo)) {
                    stopPagination = true
                    break
                }
                if (airedDate.isAfter(after)) {
                    accumulated += episode
                }
            }
        }

        if (!stopPagination && episodePage.hasNextPage) {
            page++
        } else {
            break
        }
    }

    return accumulated
}
