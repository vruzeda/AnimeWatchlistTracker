package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.Season
import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes all seasons for a given anime, ordered by orderIndex. */
class ObserveSeasonsForAnimeUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {

    operator fun invoke(animeId: Long): Flow<List<Season>> =
        seasonRepository.observeSeasonsForAnime(animeId)
}
