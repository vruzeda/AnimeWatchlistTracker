package com.vuzeda.animewatchlist.tracker.module.analytics

interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)
}
