package com.vuzeda.animewatchlist.tracker.domain.model

sealed interface DataError {

    data class Network(val throwable: Throwable? = null) : DataError, RuntimeException(
        throwable?.message ?: "Network error",
        throwable
    )

    data class NotFound(val errorMessage: String = "Not found") : DataError, RuntimeException(errorMessage)

    data class RateLimited(val retryAfterMs: Long? = null) : DataError, RuntimeException(
        "Rate limited" + if (retryAfterMs != null) ", retry after ${retryAfterMs}ms" else ""
    )

    data class Unknown(val throwable: Throwable) : DataError, RuntimeException(
        throwable.message,
        throwable
    )
}
