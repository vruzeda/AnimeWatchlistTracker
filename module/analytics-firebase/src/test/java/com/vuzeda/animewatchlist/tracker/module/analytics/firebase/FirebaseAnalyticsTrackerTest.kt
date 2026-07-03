package com.vuzeda.animewatchlist.tracker.module.analytics.firebase

import android.os.Bundle
import com.google.common.truth.Truth.assertThat
import com.google.firebase.analytics.FirebaseAnalytics
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsEvent
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FirebaseAnalyticsTrackerTest {

    private val firebaseAnalytics: FirebaseAnalytics = mockk(relaxed = true)
    private val tracker = FirebaseAnalyticsTracker(firebaseAnalytics)

    private fun trackAndCapture(event: AnalyticsEvent): Pair<String, Bundle> {
        val eventName = slot<String>()
        val params = slot<Bundle>()

        tracker.track(event)

        verify(exactly = 1) { firebaseAnalytics.logEvent(capture(eventName), capture(params)) }
        return eventName.captured to params.captured
    }

    @Test
    fun `AddAnime maps event name and all parameters`() {
        val (name, params) = trackAndCapture(
            AnalyticsEvent.AddAnime(status = "WATCHING", seasonCount = 3, addedAllSeasons = true)
        )

        assertThat(name).isEqualTo("add_anime")
        assertThat(params.getString("watch_status")).isEqualTo("WATCHING")
        assertThat(params.getInt("season_count")).isEqualTo(3)
        assertThat(params.getBoolean("added_all_seasons")).isTrue()
    }

    @Test
    fun `RemoveAnime maps the watch status`() {
        val (name, params) = trackAndCapture(AnalyticsEvent.RemoveAnime(status = "COMPLETED"))

        assertThat(name).isEqualTo("remove_anime")
        assertThat(params.getString("watch_status")).isEqualTo("COMPLETED")
    }

    @Test
    fun `AddSeason maps the watch status`() {
        val (name, params) = trackAndCapture(AnalyticsEvent.AddSeason(status = "PLAN_TO_WATCH"))

        assertThat(name).isEqualTo("add_season")
        assertThat(params.getString("watch_status")).isEqualTo("PLAN_TO_WATCH")
    }

    @Test
    fun `RemoveSeason maps the last-season flag`() {
        val (name, params) = trackAndCapture(AnalyticsEvent.RemoveSeason(isLastSeason = true))

        assertThat(name).isEqualTo("remove_season")
        assertThat(params.getBoolean("is_last_season")).isTrue()
    }

    @Test
    fun `UpdateAnimeStatus maps the new status`() {
        val (name, params) = trackAndCapture(AnalyticsEvent.UpdateAnimeStatus(newStatus = "DROPPED"))

        assertThat(name).isEqualTo("update_anime_status")
        assertThat(params.getString("new_status")).isEqualTo("DROPPED")
    }

    @Test
    fun `UpdateSeasonStatus maps the new status`() {
        val (name, params) = trackAndCapture(AnalyticsEvent.UpdateSeasonStatus(newStatus = "ON_HOLD"))

        assertThat(name).isEqualTo("update_season_status")
        assertThat(params.getString("new_status")).isEqualTo("ON_HOLD")
    }

    @Test
    fun `UpdateUserRating maps the rating value`() {
        val (name, params) = trackAndCapture(AnalyticsEvent.UpdateUserRating(rating = 9))

        assertThat(name).isEqualTo("update_user_rating")
        assertThat(params.getInt("rating")).isEqualTo(9)
    }

    @Test
    fun `SetEpisodeWatched maps the watched flag`() {
        val (name, params) = trackAndCapture(AnalyticsEvent.SetEpisodeWatched(isWatched = true))

        assertThat(name).isEqualTo("set_episode_watched")
        assertThat(params.getBoolean("is_watched")).isTrue()
    }

    @Test
    fun `MarkAllEpisodesWatched logs an empty bundle`() {
        val (name, params) = trackAndCapture(AnalyticsEvent.MarkAllEpisodesWatched)

        assertThat(name).isEqualTo("mark_all_episodes_watched")
        assertThat(params.isEmpty).isTrue()
    }

    @Test
    fun `SelectNotificationType maps the notification type`() {
        val (name, params) = trackAndCapture(
            AnalyticsEvent.SelectNotificationType(notificationType = "NEW_EPISODES")
        )

        assertThat(name).isEqualTo("select_notification_type")
        assertThat(params.getString("notification_type")).isEqualTo("NEW_EPISODES")
    }

    @Test
    fun `NotificationPermissionDenied logs an empty bundle`() {
        val (name, params) = trackAndCapture(AnalyticsEvent.NotificationPermissionDenied)

        assertThat(name).isEqualTo("notification_permission_denied")
        assertThat(params.isEmpty).isTrue()
    }

    @Test
    fun `SetTitleLanguage maps the language`() {
        val (name, params) = trackAndCapture(AnalyticsEvent.SetTitleLanguage(language = "ENGLISH"))

        assertThat(name).isEqualTo("set_title_language")
        assertThat(params.getString("language")).isEqualTo("ENGLISH")
    }

    @Test
    fun `SetHomeViewMode maps the mode`() {
        val (name, params) = trackAndCapture(AnalyticsEvent.SetHomeViewMode(mode = "SEASONS"))

        assertThat(name).isEqualTo("set_home_view_mode")
        assertThat(params.getString("mode")).isEqualTo("SEASONS")
    }

    @Test
    fun `SelectSort maps screen option and direction`() {
        val (name, params) = trackAndCapture(
            AnalyticsEvent.SelectSort(screen = "home", sortOption = "ALPHABETICAL", isAscending = false)
        )

        assertThat(name).isEqualTo("select_sort")
        assertThat(params.getString("screen")).isEqualTo("home")
        assertThat(params.getString("sort_option")).isEqualTo("ALPHABETICAL")
        assertThat(params.getBoolean("is_ascending")).isFalse()
    }

    @Test
    fun `SelectFilter maps type and value`() {
        val (name, params) = trackAndCapture(
            AnalyticsEvent.SelectFilter(filterType = "watch_status", filterValue = "WATCHING")
        )

        assertThat(name).isEqualTo("select_filter")
        assertThat(params.getString("filter_type")).isEqualTo("watch_status")
        assertThat(params.getString("filter_value")).isEqualTo("WATCHING")
    }

    @Test
    fun `LoadMoreResults maps screen and page`() {
        val (name, params) = trackAndCapture(AnalyticsEvent.LoadMoreResults(screen = "search", page = 3))

        assertThat(name).isEqualTo("load_more_results")
        assertThat(params.getString("screen")).isEqualTo("search")
        assertThat(params.getInt("page")).isEqualTo(3)
    }

    @Test
    fun `ExecuteSearch uses the canonical Firebase search event`() {
        val (name, params) = trackAndCapture(
            AnalyticsEvent.ExecuteSearch(queryLength = 7, resultCount = 25, isSuccess = true)
        )

        assertThat(name).isEqualTo(FirebaseAnalytics.Event.SEARCH)
        assertThat(params.getInt("query_length")).isEqualTo(7)
        assertThat(params.getInt("result_count")).isEqualTo(25)
        assertThat(params.getBoolean("is_success")).isTrue()
    }

    @Test
    fun `DeleteAllData logs an empty bundle`() {
        val (name, params) = trackAndCapture(AnalyticsEvent.DeleteAllData)

        assertThat(name).isEqualTo("delete_all_data")
        assertThat(params.isEmpty).isTrue()
    }

    @Test
    fun `SubmitFeedback maps the category`() {
        val (name, params) = trackAndCapture(AnalyticsEvent.SubmitFeedback(category = "BUG_REPORT"))

        assertThat(name).isEqualTo("submit_feedback")
        assertThat(params.getString("category")).isEqualTo("BUG_REPORT")
    }

    @Test
    fun `ToggleEpisodeNotifications maps the enabled flag`() {
        val (name, params) = trackAndCapture(AnalyticsEvent.ToggleEpisodeNotifications(enabled = false))

        assertThat(name).isEqualTo("toggle_episode_notifications")
        assertThat(params.getBoolean("enabled")).isFalse()
    }
}
