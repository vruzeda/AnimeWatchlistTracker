package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.repository.SeasonRepository
import javax.inject.Inject

/** Toggles episode notification tracking on or off for a specific season. */
class ToggleSeasonEpisodeNotificationsUseCase @Inject constructor(
    private val seasonRepository: SeasonRepository
) {

    suspend operator fun invoke(seasonId: Long, enabled: Boolean) =
        seasonRepository.toggleSeasonEpisodeNotifications(seasonId = seasonId, enabled = enabled)
}
