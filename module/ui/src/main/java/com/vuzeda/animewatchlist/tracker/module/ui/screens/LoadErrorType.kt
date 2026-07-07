package com.vuzeda.animewatchlist.tracker.module.ui.screens

import com.vuzeda.animewatchlist.tracker.module.domain.DataError
import com.vuzeda.animewatchlist.tracker.module.ui.R

enum class LoadErrorType {
    NETWORK,
    RATE_LIMITED,
    UNKNOWN
}

fun Throwable.toLoadErrorType(): LoadErrorType = when (this) {
    is DataError.Network -> LoadErrorType.NETWORK
    is DataError.RateLimited -> LoadErrorType.RATE_LIMITED
    else -> LoadErrorType.UNKNOWN
}

val LoadErrorType.displayMessageRes: Int
    get() = when (this) {
        LoadErrorType.NETWORK -> R.string.error_network
        LoadErrorType.RATE_LIMITED -> R.string.error_rate_limited
        LoadErrorType.UNKNOWN -> R.string.error_generic
    }
