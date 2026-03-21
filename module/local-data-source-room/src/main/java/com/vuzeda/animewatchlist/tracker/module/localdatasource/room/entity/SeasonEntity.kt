package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.StreamingInfo
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import java.time.LocalDate

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
    indices = [Index(value = ["animeId"])]
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
    val currentEpisode: Int = 0,
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
    val lastEpisodeCheckDate: LocalDate? = null,
    val isEpisodeNotificationsEnabled: Boolean = false,
    val isInWatchlist: Boolean = true
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
    currentEpisode = currentEpisode,
    status = WatchStatus.entries.firstOrNull { it.name == status } ?: WatchStatus.PLAN_TO_WATCH,
    score = score,
    orderIndex = orderIndex,
    airingStatus = airingStatus,
    broadcastInfo = broadcastInfo,
    broadcastDay = broadcastDay,
    broadcastTime = broadcastTime,
    broadcastTimezone = broadcastTimezone,
    streamingLinks = if (streamingLinks.isBlank()) emptyList() else streamingLinks.split("\n").mapNotNull { entry ->
        val parts = entry.split("\t")
        if (parts.size >= 2) StreamingInfo(name = parts[0], url = parts[1]) else null
    },
    lastCheckedAiredEpisodeCount = lastCheckedAiredEpisodeCount,
    lastEpisodeCheckDate = lastEpisodeCheckDate,
    isEpisodeNotificationsEnabled = isEpisodeNotificationsEnabled,
    isInWatchlist = isInWatchlist
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
    currentEpisode = currentEpisode,
    status = status.name,
    score = score,
    orderIndex = orderIndex,
    airingStatus = airingStatus,
    broadcastInfo = broadcastInfo,
    broadcastDay = broadcastDay,
    broadcastTime = broadcastTime,
    broadcastTimezone = broadcastTimezone,
    streamingLinks = streamingLinks.joinToString("\n") { "${it.name}\t${it.url}" },
    lastCheckedAiredEpisodeCount = lastCheckedAiredEpisodeCount,
    lastEpisodeCheckDate = lastEpisodeCheckDate,
    isEpisodeNotificationsEnabled = isEpisodeNotificationsEnabled,
    isInWatchlist = isInWatchlist
)
