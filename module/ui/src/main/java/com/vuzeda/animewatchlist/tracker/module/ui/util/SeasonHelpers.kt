package com.vuzeda.animewatchlist.tracker.module.ui.util

import com.vuzeda.animewatchlist.tracker.module.domain.Season

internal fun List<Season>.latestWatchedSeason(): Season? =
    filter { it.isInWatchlist }
        .maxByOrNull { it.orderIndex }
