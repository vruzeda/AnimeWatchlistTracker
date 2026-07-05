package com.vuzeda.animewatchlist.tracker.module.notification.android

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.domain.resolveDisplayTitle
import com.vuzeda.animewatchlist.tracker.module.notification.AnimeUpdateNotifier
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    @NotificationLaunchActivity private val launchActivityClass: Class<*>
) : AnimeUpdateNotifier {

    override fun createNotificationChannels() {
        val episodesChannel = NotificationChannel(
            EPISODES_CHANNEL_ID,
            context.getString(R.string.notification_channel_episodes_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_episodes_description)
        }
        val seasonsChannel = NotificationChannel(
            SEASONS_CHANNEL_ID,
            context.getString(R.string.notification_channel_seasons_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_seasons_description)
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.createNotificationChannel(episodesChannel)
        notificationManager.createNotificationChannel(seasonsChannel)
        notificationManager.deleteNotificationChannel(LEGACY_CHANNEL_ID)
    }

    @SuppressLint("MissingPermission")
    override fun showUpdateNotification(update: AnimeUpdate, titleLanguage: TitleLanguage) {
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
        val animeTitle = resolveDisplayTitle(anime.title, anime.titleEnglish, anime.titleJapanese, titleLanguage)
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
                context.getString(
                    R.string.new_season_announced,
                    resolveDisplayTitle(update.sequelTitle, update.sequelTitleEnglish, update.sequelTitleJapanese, titleLanguage)
                ),
                "season_${anime.id}_${update.sequelMalId}".hashCode()
            )
        }
        val channelId = when (update) {
            is AnimeUpdate.NewEpisodes -> EPISODES_CHANNEL_ID
            is AnimeUpdate.NewSeason -> SEASONS_CHANNEL_ID
        }
        val groupKey = "anime_${anime.id}"
        val seasonMalId = when (update) {
            is AnimeUpdate.NewEpisodes -> update.season.malId
            is AnimeUpdate.NewSeason -> update.sequelMalId
        }
        val contentIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(
                Intent(context, launchActivityClass)
                    .putExtra(EXTRA_SEASON_MAL_ID, seasonMalId)
            )
            getPendingIntent(notificationId, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setContentTitle(animeTitle)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setGroup(groupKey)
            .setContentIntent(contentIntent)
            .build()

        notificationManager.notify(notificationId, notification)

        val summary = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setContentTitle(animeTitle)
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(anime.id.toInt(), summary)
    }

    companion object {
        const val EXTRA_SEASON_MAL_ID = "extra_season_mal_id"
        const val EPISODES_CHANNEL_ID = "anime_new_episodes"
        const val SEASONS_CHANNEL_ID = "anime_new_seasons"
        const val LEGACY_CHANNEL_ID = "anime_updates"
    }
}
