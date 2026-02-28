package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

/** Toggles notification tracking on or off for a specific anime. */
class ToggleAnimeNotificationsUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    suspend operator fun invoke(id: Long, enabled: Boolean) =
        animeRepository.toggleNotifications(id = id, enabled = enabled)
}
