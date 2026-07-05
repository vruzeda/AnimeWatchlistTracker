package com.vuzeda.animewatchlist.tracker.module.notification.android

import android.Manifest
import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import androidx.core.app.NotificationCompat
import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

class TestLaunchActivity : Activity()

@RunWith(RobolectricTestRunner::class)
class NotificationHelperTest {

    private val context: Application = RuntimeEnvironment.getApplication()
    private val notificationHelper = NotificationHelper(context, TestLaunchActivity::class.java)
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val frieren = Anime(
        id = 1,
        title = "Sousou no Frieren",
        titleEnglish = "Frieren: Beyond Journey's End"
    )

    private val frierenSeason = Season(id = 1, animeId = 1, malId = 52991, title = "Sousou no Frieren")

    private val newEpisodes = AnimeUpdate.NewEpisodes(
        anime = frieren,
        season = frierenSeason,
        newEpisodeCount = 2
    )

    private val newSeason = AnimeUpdate.NewSeason(
        anime = frieren,
        sequelMalId = 59978,
        sequelTitle = "Sousou no Frieren 2nd Season"
    )

    @Before
    fun setup() {
        shadowOf(context).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        shadowOf(context.packageManager)
            .addActivityIfNotPresent(ComponentName(context, TestLaunchActivity::class.java))
        notificationHelper.createNotificationChannels()
    }

    private fun postedNotifications() = shadowOf(notificationManager).allNotifications

    private fun contentNotification() =
        postedNotifications().first { it.extras.getString(NotificationCompat.EXTRA_TEXT) != null }

    @Test
    fun `createNotificationChannels registers the episodes channel with localized texts`() {
        val channel = notificationManager.getNotificationChannel(NotificationHelper.EPISODES_CHANNEL_ID)

        assertThat(channel).isNotNull()
        assertThat(channel.name.toString())
            .isEqualTo(context.getString(R.string.notification_channel_episodes_name))
        assertThat(channel.description)
            .isEqualTo(context.getString(R.string.notification_channel_episodes_description))
    }

    @Test
    fun `createNotificationChannels registers the seasons channel with localized texts`() {
        val channel = notificationManager.getNotificationChannel(NotificationHelper.SEASONS_CHANNEL_ID)

        assertThat(channel).isNotNull()
        assertThat(channel.name.toString())
            .isEqualTo(context.getString(R.string.notification_channel_seasons_name))
        assertThat(channel.description)
            .isEqualTo(context.getString(R.string.notification_channel_seasons_description))
    }

    @Test
    fun `createNotificationChannels deletes the legacy combined channel`() {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NotificationHelper.LEGACY_CHANNEL_ID,
                "Anime Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        notificationHelper.createNotificationChannels()

        assertThat(notificationManager.getNotificationChannel(NotificationHelper.LEGACY_CHANNEL_ID)).isNull()
    }

    @Test
    fun `new episodes notification is posted on the episodes channel`() {
        notificationHelper.showUpdateNotification(newEpisodes, TitleLanguage.DEFAULT)

        assertThat(contentNotification().channelId).isEqualTo(NotificationHelper.EPISODES_CHANNEL_ID)
    }

    @Test
    fun `new season notification is posted on the seasons channel`() {
        notificationHelper.showUpdateNotification(newSeason, TitleLanguage.DEFAULT)

        assertThat(contentNotification().channelId).isEqualTo(NotificationHelper.SEASONS_CHANNEL_ID)
    }

    @Test
    fun `content notification expands the full text via big text style`() {
        notificationHelper.showUpdateNotification(newEpisodes, TitleLanguage.DEFAULT)

        val notification = contentNotification()

        assertThat(notification.extras.getCharSequence(NotificationCompat.EXTRA_BIG_TEXT).toString())
            .isEqualTo(context.resources.getQuantityString(R.plurals.new_episodes_aired, 2, 2))
    }

    @Test
    fun `new episodes update posts a content notification and a group summary`() {
        notificationHelper.showUpdateNotification(newEpisodes, TitleLanguage.DEFAULT)

        assertThat(postedNotifications()).hasSize(2)
    }

    @Test
    fun `posted notifications use the monochrome small icon`() {
        notificationHelper.showUpdateNotification(newEpisodes, TitleLanguage.DEFAULT)

        postedNotifications().forEach { notification ->
            assertThat(notification.smallIcon.resId).isEqualTo(R.drawable.ic_notification)
        }
    }

    @Test
    fun `posted notifications are tinted with the brand accent color`() {
        notificationHelper.showUpdateNotification(newEpisodes, TitleLanguage.DEFAULT)

        postedNotifications().forEach { notification ->
            assertThat(notification.color).isEqualTo(context.getColor(R.color.notification_accent))
        }
    }

    @Test
    fun `new episodes notification carries the plural episode text`() {
        notificationHelper.showUpdateNotification(newEpisodes, TitleLanguage.DEFAULT)

        val notification = contentNotification()

        assertThat(notification.extras.getString(NotificationCompat.EXTRA_TITLE))
            .isEqualTo("Sousou no Frieren")
        assertThat(notification.extras.getString(NotificationCompat.EXTRA_TEXT))
            .isEqualTo(context.resources.getQuantityString(R.plurals.new_episodes_aired, 2, 2))
    }

    @Test
    fun `new episodes notification resolves the english title when selected`() {
        notificationHelper.showUpdateNotification(newEpisodes, TitleLanguage.ENGLISH)

        val notification = contentNotification()

        assertThat(notification.extras.getString(NotificationCompat.EXTRA_TITLE))
            .isEqualTo("Frieren: Beyond Journey's End")
    }

    @Test
    fun `new season notification names the announced sequel`() {
        notificationHelper.showUpdateNotification(newSeason, TitleLanguage.DEFAULT)

        val notification = contentNotification()

        assertThat(notification.extras.getString(NotificationCompat.EXTRA_TEXT))
            .isEqualTo(context.getString(R.string.new_season_announced, "Sousou no Frieren 2nd Season"))
    }

    @Test
    fun `nothing is posted when the notifications permission is missing`() {
        shadowOf(context).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)

        notificationHelper.showUpdateNotification(newEpisodes, TitleLanguage.DEFAULT)

        assertThat(postedNotifications()).isEmpty()
    }

    @Test
    fun `nothing is posted when notifications are disabled for the app`() {
        shadowOf(notificationManager).setNotificationsEnabled(false)

        notificationHelper.showUpdateNotification(newEpisodes, TitleLanguage.DEFAULT)

        assertThat(postedNotifications()).isEmpty()
    }
}
