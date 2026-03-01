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
import com.vuzeda.animewatchlist.tracker.domain.model.AnimeUpdate
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
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun showUpdateNotification(update: AnimeUpdate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val (title, text, notificationId) = when (update) {
            is AnimeUpdate.NewEpisodes -> Triple(
                update.anime.title,
                "New episodes available! (${update.previousCount} → ${update.currentCount})",
                update.anime.malId?.hashCode() ?: update.anime.id.toInt()
            )
            is AnimeUpdate.NewSeason -> Triple(
                update.anime.title,
                "New season announced: ${update.sequelTitle}",
                (update.anime.malId?.hashCode() ?: update.anime.id.toInt()) + update.sequelMalId
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    companion object {
        const val CHANNEL_ID = "anime_updates"
        const val CHANNEL_NAME = "Anime Updates"
        const val CHANNEL_DESCRIPTION = "Notifications for new episodes and seasons"
    }
}
