package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class FeedbackTest {

    @Test
    fun `Feedback holds all provided field values`() {
        val feedback = Feedback(
            category = FeedbackCategory.BUG_REPORT,
            message = "App crashes on startup",
            appVersion = "1.2.3",
            timestamp = 1700000000000L,
            deviceModel = "Pixel 8",
            androidVersion = 34,
            installationId = "abc-123",
            titleLanguage = "ENGLISH",
            homeViewMode = "ANIME"
        )

        assertThat(feedback.category).isEqualTo(FeedbackCategory.BUG_REPORT)
        assertThat(feedback.message).isEqualTo("App crashes on startup")
        assertThat(feedback.appVersion).isEqualTo("1.2.3")
        assertThat(feedback.timestamp).isEqualTo(1700000000000L)
        assertThat(feedback.deviceModel).isEqualTo("Pixel 8")
        assertThat(feedback.androidVersion).isEqualTo(34)
        assertThat(feedback.installationId).isEqualTo("abc-123")
        assertThat(feedback.titleLanguage).isEqualTo("ENGLISH")
        assertThat(feedback.homeViewMode).isEqualTo("ANIME")
    }

    @Test
    fun `two Feedback instances with same values are equal`() {
        val a = Feedback(
            category = FeedbackCategory.GENERAL,
            message = "Nice app",
            appVersion = "1.0.0",
            timestamp = 1000L,
            deviceModel = "Galaxy S24",
            androidVersion = 35,
            installationId = "xyz",
            titleLanguage = "DEFAULT",
            homeViewMode = "SEASON"
        )
        val b = a.copy()

        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `FeedbackCategory values contains all three categories`() {
        assertThat(FeedbackCategory.entries).containsExactly(
            FeedbackCategory.BUG_REPORT,
            FeedbackCategory.FEATURE_REQUEST,
            FeedbackCategory.GENERAL
        )
    }

    @Test
    fun `BUG_REPORT name is BUG_REPORT`() {
        assertThat(FeedbackCategory.BUG_REPORT.name).isEqualTo("BUG_REPORT")
    }

    @Test
    fun `FEATURE_REQUEST name is FEATURE_REQUEST`() {
        assertThat(FeedbackCategory.FEATURE_REQUEST.name).isEqualTo("FEATURE_REQUEST")
    }

    @Test
    fun `GENERAL name is GENERAL`() {
        assertThat(FeedbackCategory.GENERAL.name).isEqualTo("GENERAL")
    }
}
