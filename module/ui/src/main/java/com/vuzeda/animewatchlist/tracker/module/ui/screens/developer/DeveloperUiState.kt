package com.vuzeda.animewatchlist.tracker.module.ui.screens.developer

import kotlin.time.Instant

data class DeveloperUiState(
    val lastAnimeUpdateRun: Instant? = null,
    val isNotificationDebugInfoEnabled: Boolean = false
)
