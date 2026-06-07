package com.vuzeda.animewatchlist.tracker.module.ui.screens.developer

import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdateResult
import kotlin.time.Instant

data class DeveloperUiState(
    val lastAnimeUpdateRun: Instant? = null,
    val lastAnimeUpdateAttemptAt: Instant? = null,
    val lastAnimeUpdateAttemptResult: AnimeUpdateResult? = null,
    val isNotificationDebugInfoEnabled: Boolean = false
)
