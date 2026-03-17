package com.vuzeda.animewatchlist.tracker.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.vuzeda.animewatchlist.tracker.R
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.createNotificationChannel(channel)
    }

    fun showUpdateNotification(update: AnimeUpdate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }

        val anime = when (update) {
            is AnimeUpdate.NewEpisodes -> update.anime
            is AnimeUpdate.NewSeason -> update.anime
        }
        val (text, notificationId) = when (update) {
            is AnimeUpdate.NewEpisodes -> Pair(
                context.resources.getQuantityString(
                    R.plurals.new_episodes_aired,
                    update.newEpisodeCount,
                    update.newEpisodeCount
                ),
                "ep_${anime.id}".hashCode()
            )
            is AnimeUpdate.NewSeason -> Pair(
                "New season announced: ${update.sequelTitle}",
                "season_${anime.id}_${update.sequelMalId}".hashCode()
            )
        }
        val groupKey = "anime_${anime.id}"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(anime.title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setGroup(groupKey)
            .build()

        notificationManager.notify(notificationId, notification)

        val summary = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(anime.title)
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(anime.id.toInt(), summary)
    }

    companion object {
        const val CHANNEL_ID = "anime_updates"
        const val CHANNEL_NAME = "Anime Updates"
        const val CHANNEL_DESCRIPTION = "Notifications for new episodes and seasons"
    }
}
