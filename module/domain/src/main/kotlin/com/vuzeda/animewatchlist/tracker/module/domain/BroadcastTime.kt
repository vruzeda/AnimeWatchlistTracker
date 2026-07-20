package com.vuzeda.animewatchlist.tracker.module.domain

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class BroadcastTime(
    val dayOfWeek: DayOfWeek,
    val time: LocalTime? = null,
    val zoneId: ZoneId? = null
) {

    fun toZoneId(targetZoneId: ZoneId): BroadcastTime {
        val sourceDayOfWeek = dayOfWeek
        val sourceTime = time ?: return this
        val sourceZone = zoneId ?: return this

        val source = ZonedDateTime.now(sourceZone)
            .with(sourceDayOfWeek)
            .withHour(sourceTime.hour)
            .withMinute(sourceTime.minute)
            .withSecond(0)
            .withNano(0)

        val target = source.withZoneSameInstant(targetZoneId)

        return BroadcastTime(
            dayOfWeek = target.dayOfWeek,
            time = target.toLocalTime(),
            zoneId = targetZoneId
        )
    }

    companion object {
        operator fun invoke(
            day: String?,
            time: String?,
            timezone: String?,
        ): BroadcastTime? {
            val dayOfWeek = day?.let {
                DayOfWeek.entries.firstOrNull { dow -> it.lowercase().startsWith(dow.getDisplayName(TextStyle.FULL, Locale.ENGLISH).lowercase())  }
            } ?: return null
            val time = time?.let {
                LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm"))
            }
            val zoneId = ZoneId.of(timezone)

            return if (time != null && zoneId != null) {
                BroadcastTime(
                    dayOfWeek = dayOfWeek,
                    time = time,
                    zoneId = zoneId
                )
            } else {
                BroadcastTime(
                    dayOfWeek = dayOfWeek
                )
            }
        }
    }
}
