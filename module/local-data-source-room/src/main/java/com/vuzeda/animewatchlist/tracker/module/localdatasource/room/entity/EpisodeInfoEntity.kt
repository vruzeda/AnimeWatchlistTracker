package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity

import androidx.room.Entity
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo

@Entity(tableName = "episode_info", primaryKeys = ["malId", "number"])
data class EpisodeInfoEntity(
    val malId: Int,
    val number: Int,
    val title: String?,
    val titleEnglish: String?,
    val titleJapanese: String?,
    val aired: String?,
    val isFiller: Boolean,
    val isRecap: Boolean
)

fun EpisodeInfoEntity.toDomainModel(): EpisodeInfo = EpisodeInfo(
    number = number,
    title = title,
    titleEnglish = titleEnglish,
    titleJapanese = titleJapanese,
    aired = aired,
    isFiller = isFiller,
    isRecap = isRecap
)

fun EpisodeInfo.toEntity(malId: Int): EpisodeInfoEntity = EpisodeInfoEntity(
    malId = malId,
    number = number,
    title = title,
    titleEnglish = titleEnglish,
    titleJapanese = titleJapanese,
    aired = aired,
    isFiller = isFiller,
    isRecap = isRecap
)
