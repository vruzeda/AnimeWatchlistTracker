package com.vuzeda.animewatchlist.tracker.module.analytics

object NoOpAnalyticsTracker : AnalyticsTracker {
    override fun track(event: AnalyticsEvent) = Unit
}
