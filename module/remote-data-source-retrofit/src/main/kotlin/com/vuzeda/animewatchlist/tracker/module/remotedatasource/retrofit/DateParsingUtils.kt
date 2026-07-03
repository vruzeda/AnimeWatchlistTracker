package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit

import java.time.LocalDate

fun parseAiredDateFromString(aired: String?): LocalDate? {
    if (aired == null) return null
    return try {
        LocalDate.parse(aired.take(10))
    } catch (_: Exception) {
        null
    }
}
