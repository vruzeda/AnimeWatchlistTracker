package com.vuzeda.animewatchlist.tracker.module.analytics

sealed class AnalyticsEvent {

    // Watchlist
    data class AddAnime(
        val status: String,
        val seasonCount: Int,
        val addedAllSeasons: Boolean
    ) : AnalyticsEvent()

    data class RemoveAnime(val status: String) : AnalyticsEvent()

    data class AddSeason(val status: String) : AnalyticsEvent()

    data class RemoveSeason(val isLastSeason: Boolean) : AnalyticsEvent()

    // Status / Rating
    data class UpdateAnimeStatus(val newStatus: String) : AnalyticsEvent()

    data class UpdateSeasonStatus(val newStatus: String) : AnalyticsEvent()

    /** [rating] is 0 when the rating was cleared, 1–10 otherwise. */
    data class UpdateUserRating(val rating: Int) : AnalyticsEvent()

    // Episodes
    data class SetEpisodeWatched(val isWatched: Boolean) : AnalyticsEvent()

    data object MarkAllEpisodesWatched : AnalyticsEvent()

    // Notifications
    data class SelectNotificationType(val notificationType: String) : AnalyticsEvent()

    data class ToggleEpisodeNotifications(val enabled: Boolean) : AnalyticsEvent()

    data object NotificationPermissionDenied : AnalyticsEvent()

    // Preferences
    data class SetTitleLanguage(val language: String) : AnalyticsEvent()

    data class SetHomeViewMode(val mode: String) : AnalyticsEvent()

    data class SelectSort(
        val screen: String,
        val sortOption: String,
        val isAscending: Boolean
    ) : AnalyticsEvent()

    data class SelectFilter(
        val filterType: String,
        val filterValue: String
    ) : AnalyticsEvent()

    // Discovery
    data class ExecuteSearch(
        val queryLength: Int,
        val resultCount: Int,
        val isSuccess: Boolean
    ) : AnalyticsEvent()

    // Data management
    data object DeleteAllData : AnalyticsEvent()

    // Feedback
    data class SubmitFeedback(val category: String) : AnalyticsEvent()
}
