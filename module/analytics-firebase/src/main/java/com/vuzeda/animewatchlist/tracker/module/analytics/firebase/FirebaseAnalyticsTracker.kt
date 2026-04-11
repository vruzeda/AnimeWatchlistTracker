package com.vuzeda.animewatchlist.tracker.module.analytics.firebase

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsEvent
import com.vuzeda.animewatchlist.tracker.module.analytics.AnalyticsTracker

class FirebaseAnalyticsTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsTracker {

    override fun track(event: AnalyticsEvent) {
        val (name, params) = event.toFirebaseParams()
        firebaseAnalytics.logEvent(name, params)
    }

    private fun AnalyticsEvent.toFirebaseParams(): Pair<String, Bundle> {
        val bundle = Bundle()
        return when (this) {
            is AnalyticsEvent.AddAnime -> {
                bundle.putString("watch_status", status)
                bundle.putInt("season_count", seasonCount)
                bundle.putBoolean("added_all_seasons", addedAllSeasons)
                "add_anime" to bundle
            }
            is AnalyticsEvent.RemoveAnime -> {
                bundle.putString("watch_status", status)
                "remove_anime" to bundle
            }
            is AnalyticsEvent.AddSeason -> {
                bundle.putString("watch_status", status)
                "add_season" to bundle
            }
            is AnalyticsEvent.RemoveSeason -> {
                bundle.putBoolean("is_last_season", isLastSeason)
                "remove_season" to bundle
            }
            is AnalyticsEvent.UpdateAnimeStatus -> {
                bundle.putString("new_status", newStatus)
                "update_anime_status" to bundle
            }
            is AnalyticsEvent.UpdateSeasonStatus -> {
                bundle.putString("new_status", newStatus)
                "update_season_status" to bundle
            }
            is AnalyticsEvent.UpdateUserRating -> {
                bundle.putInt("rating", rating)
                "update_user_rating" to bundle
            }
            is AnalyticsEvent.SetEpisodeWatched -> {
                bundle.putBoolean("is_watched", isWatched)
                "set_episode_watched" to bundle
            }
            AnalyticsEvent.MarkAllEpisodesWatched -> {
                "mark_all_episodes_watched" to bundle
            }
            is AnalyticsEvent.SelectNotificationType -> {
                bundle.putString("notification_type", notificationType)
                "select_notification_type" to bundle
            }
            is AnalyticsEvent.ToggleEpisodeNotifications -> {
                bundle.putBoolean("enabled", enabled)
                "toggle_episode_notifications" to bundle
            }
            AnalyticsEvent.NotificationPermissionDenied -> {
                "notification_permission_denied" to bundle
            }
            is AnalyticsEvent.SetTitleLanguage -> {
                bundle.putString("language", language)
                "set_title_language" to bundle
            }
            is AnalyticsEvent.SetHomeViewMode -> {
                bundle.putString("mode", mode)
                "set_home_view_mode" to bundle
            }
            is AnalyticsEvent.SelectSort -> {
                bundle.putString("screen", screen)
                bundle.putString("sort_option", sortOption)
                bundle.putBoolean("is_ascending", isAscending)
                "select_sort" to bundle
            }
            is AnalyticsEvent.SelectFilter -> {
                bundle.putString("filter_type", filterType)
                bundle.putString("filter_value", filterValue)
                "select_filter" to bundle
            }
            is AnalyticsEvent.ExecuteSearch -> {
                bundle.putInt("query_length", queryLength)
                bundle.putInt("result_count", resultCount)
                bundle.putBoolean("is_success", isSuccess)
                FirebaseAnalytics.Event.SEARCH to bundle
            }
            AnalyticsEvent.DeleteAllData -> {
                "delete_all_data" to bundle
            }
            is AnalyticsEvent.SubmitFeedback -> {
                bundle.putString("category", category)
                "submit_feedback" to bundle
            }
        }
    }
}
