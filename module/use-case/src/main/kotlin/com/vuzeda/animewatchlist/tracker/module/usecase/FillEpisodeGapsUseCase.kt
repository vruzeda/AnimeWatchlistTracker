package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import javax.inject.Inject

/**
 * Given a (possibly incomplete) episode list and the season's known episode count, fills in
 * placeholder [EpisodeInfo] entries for any missing episode numbers so that every expected episode
 * is representable in the UI.
 *
 * Placeholders are only added when [episodeCount] is known and the fetched list is shorter. The
 * fetched list is never trimmed — if the API returns more episodes than [episodeCount], all are
 * kept, as [episodeCount] may be stale.
 */
class FillEpisodeGapsUseCase @Inject constructor() {
    operator fun invoke(episodes: List<EpisodeInfo>, episodeCount: Int?): List<EpisodeInfo> {
        if (episodeCount == null || episodes.size >= episodeCount) return episodes
        val existing = episodes.map { it.number }.toSet()
        val placeholders = (1..episodeCount)
            .filter { it !in existing }
            .map { n ->
                EpisodeInfo(
                    number = n,
                    title = null,
                    aired = null,
                    isFiller = false,
                    isRecap = false,
                    isPlaceholder = true
                )
            }
        return (episodes + placeholders).sortedBy { it.number }
    }
}
