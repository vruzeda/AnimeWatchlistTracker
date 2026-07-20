package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Moshi
import com.vuzeda.animewatchlist.tracker.module.domain.BroadcastTime
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.StreamingInfo
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val moshi = Moshi.Builder().build()

private fun streamingInfoToJson(links: List<StreamingInfo>): String {
    val jsonArray = moshi.adapter(List::class.java).toJson(links.map { mapOf("name" to it.name, "url" to it.url) })
    return jsonArray ?: "[]"
}

private fun jsonToStreamingInfo(json: String): List<StreamingInfo> {
    if (json.isBlank()) return emptyList()
    return try {
        @Suppress("UNCHECKED_CAST")
        val list = moshi.adapter(List::class.java).fromJson(json) as? List<Map<String, String>> ?: emptyList()
        list.mapNotNull { entry ->
            val name = entry["name"] ?: return@mapNotNull null
            val url = entry["url"] ?: return@mapNotNull null
            StreamingInfo(name = name, url = url)
        }
    } catch (e: Exception) {
        emptyList()
    }
}

@Entity(
    tableName = "season",
    foreignKeys = [
        ForeignKey(
            entity = AnimeEntity::class,
            parentColumns = ["id"],
            childColumns = ["animeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["animeId"]),
        Index(value = ["malId"], unique = true)
    ]
)
data class SeasonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val animeId: Long,
    val malId: Int,
    val title: String,
    val titleEnglish: String? = null,
    val titleJapanese: String? = null,
    val imageUrl: String? = null,
    val type: String = "TV",
    val episodeCount: Int? = null,
    val status: String = "PLAN_TO_WATCH",
    val score: Double? = null,
    val orderIndex: Int = 0,
    val airingStatus: String? = null,
    val broadcastInfo: String? = null,
    val broadcastDay: String? = null,
    val broadcastTime: String? = null,
    val broadcastTimezone: String? = null,
    val streamingLinks: String = "",
    val lastCheckedAiredEpisodeCount: Int? = null,
    val latestKnownEpisodeAirDate: LocalDate? = null,
    val lastEpisodeCheckPerformedDate: LocalDate? = null,
    val isEpisodeNotificationsEnabled: Boolean = false,
    val isInWatchlist: Boolean = true,
    val airingSeasonName: String? = null,
    val airingSeasonYear: Int? = null,
    val addedAt: Long = 0
)

fun SeasonEntity.toDomainModel(): Season = Season(
    id = id,
    animeId = animeId,
    malId = malId,
    title = title,
    titleEnglish = titleEnglish,
    titleJapanese = titleJapanese,
    imageUrl = imageUrl,
    type = type,
    episodeCount = episodeCount,
    status = WatchStatus.entries.firstOrNull { it.name == status } ?: WatchStatus.PLAN_TO_WATCH,
    score = score,
    orderIndex = orderIndex,
    airingStatus = airingStatus,
    broadcastInfo = broadcastInfo,
    broadcastTime = toBroadcastTime(),
    streamingLinks = jsonToStreamingInfo(streamingLinks),
    lastCheckedAiredEpisodeCount = lastCheckedAiredEpisodeCount,
    latestKnownEpisodeAirDate = latestKnownEpisodeAirDate,
    lastEpisodeCheckPerformedDate = lastEpisodeCheckPerformedDate,
    isEpisodeNotificationsEnabled = isEpisodeNotificationsEnabled,
    isInWatchlist = isInWatchlist,
    airingSeasonName = airingSeasonName,
    airingSeasonYear = airingSeasonYear,
    addedAt = addedAt
)

private fun SeasonEntity.toBroadcastTime(): BroadcastTime? =
    BroadcastTime(
        dayOfWeek = broadcastDay?.let {
            DayOfWeek.entries.firstOrNull { dow -> it.lowercase().startsWith(dow.getDisplayName(TextStyle.FULL, Locale.ENGLISH).lowercase()) }
        } ?: return null,
        time = broadcastTime?.let {
            LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm"))
        },
        zoneId = broadcastTimezone?.let {
            ZoneId.of(it)
        } ?: return null,
    )

fun Season.toEntity(): SeasonEntity = SeasonEntity(
    id = id,
    animeId = animeId,
    malId = malId,
    title = title,
    titleEnglish = titleEnglish,
    titleJapanese = titleJapanese,
    imageUrl = imageUrl,
    type = type,
    episodeCount = episodeCount,
    status = status.name,
    score = score,
    orderIndex = orderIndex,
    airingStatus = airingStatus,
    broadcastInfo = broadcastInfo,
    broadcastDay = broadcastTime?.dayOfWeek?.getDisplayName(TextStyle.FULL, Locale.ENGLISH)?.lowercase(),
    broadcastTime = broadcastTime?.time?.format(DateTimeFormatter.ofPattern("HH:mm")),
    broadcastTimezone = broadcastTime?.zoneId?.id,
    streamingLinks = streamingInfoToJson(streamingLinks),
    lastCheckedAiredEpisodeCount = lastCheckedAiredEpisodeCount,
    latestKnownEpisodeAirDate = latestKnownEpisodeAirDate,
    lastEpisodeCheckPerformedDate = lastEpisodeCheckPerformedDate,
    isEpisodeNotificationsEnabled = isEpisodeNotificationsEnabled,
    isInWatchlist = isInWatchlist,
    airingSeasonName = airingSeasonName,
    airingSeasonYear = airingSeasonYear,
    addedAt = addedAt
)
