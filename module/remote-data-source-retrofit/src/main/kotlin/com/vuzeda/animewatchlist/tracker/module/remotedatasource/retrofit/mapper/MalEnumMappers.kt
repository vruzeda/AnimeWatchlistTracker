package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

internal fun String?.malMediaTypeToDisplayType(): String = when (this) {
    "tv" -> "TV"
    "ova" -> "OVA"
    "movie" -> "Movie"
    "special" -> "Special"
    "ona" -> "ONA"
    "music" -> "Music"
    "tv_special" -> "TV Special"
    "cm" -> "CM"
    "pv" -> "PV"
    null -> "Unknown"
    else -> replace('_', ' ').uppercase()
}

internal fun String?.malStatusToDisplayStatus(): String? = when (this) {
    "finished_airing" -> "Finished Airing"
    "currently_airing" -> "Currently Airing"
    "not_yet_aired" -> "Not yet aired"
    else -> null
}

internal fun String?.malBroadcastDayToDisplayDay(): String? = when (this) {
    "monday" -> "Mondays"
    "tuesday" -> "Tuesdays"
    "wednesday" -> "Wednesdays"
    "thursday" -> "Thursdays"
    "friday" -> "Fridays"
    "saturday" -> "Saturdays"
    "sunday" -> "Sundays"
    else -> null
}
