package com.vuzeda.animewatchlist.tracker.domain.usecase

import com.vuzeda.animewatchlist.tracker.domain.model.NotificationType
import com.vuzeda.animewatchlist.tracker.domain.repository.AnimeRepository
import javax.inject.Inject

/** Updates the notification type for a specific anime. */
class ToggleAnimeNotificationsUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    suspend operator fun invoke(id: Long, notificationType: NotificationType) =
        animeRepository.updateNotificationType(id = id, notificationType = notificationType)
}
