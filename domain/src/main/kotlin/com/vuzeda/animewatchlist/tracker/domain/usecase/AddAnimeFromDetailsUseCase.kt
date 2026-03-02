package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeFullDetails
import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

/** Constructs an Anime with its initial Season from API details and persists them. */
class AddAnimeFromDetailsUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    suspend operator fun invoke(details: AnimeFullDetails, status: WatchStatus): Long {
        val anime = Anime(
            title = details.title,
            titleEnglish = details.titleEnglish,
            titleJapanese = details.titleJapanese,
            imageUrl = details.imageUrl,
            synopsis = details.synopsis,
            genres = details.genres,
            status = status,
            addedAt = System.currentTimeMillis()
        )
        val season = Season(
            malId = details.malId,
            title = details.title,
            titleEnglish = details.titleEnglish,
            titleJapanese = details.titleJapanese,
            imageUrl = details.imageUrl,
            type = details.type,
            episodeCount = details.episodes,
            score = details.score,
            airingStatus = details.airingStatus,
            orderIndex = 0
        )
        return animeRepository.addAnime(anime = anime, seasons = listOf(season))
    }
}
