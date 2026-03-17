package com.vuzeda.animewatchlist.tracker.module.ui.screens.developer

import androidx.lifecycle.ViewModel
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.ui.notification.AnimeUpdateNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DeveloperViewModel @Inject constructor(
    private val notifier: AnimeUpdateNotifier
) : ViewModel() {

    fun fireTestEpisodeNotification() {
        notifier.showUpdateNotification(
            AnimeUpdate.NewEpisodes(
                anime = Anime(title = "Fullmetal Alchemist"),
                season = Season(malId = 121, title = "Fullmetal Alchemist"),
                newEpisodeCount = 3
            )
        )
    }

    fun fireTestSeasonNotification() {
        notifier.showUpdateNotification(
            AnimeUpdate.NewSeason(
                anime = Anime(title = "Fullmetal Alchemist"),
                sequelMalId = 430,
                sequelTitle = "Fullmetal Alchemist: The Conqueror of Shamballa"
            )
        )
    }
}
