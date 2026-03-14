package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class AnimeTest {

    @Test
    fun `isNotificationsEnabled returns false when notificationType is NONE`() {
        val anime = Anime(title = "Test", notificationType = NotificationType.NONE)

        assertThat(anime.isNotificationsEnabled).isFalse()
    }

    @Test
    fun `isNotificationsEnabled returns true when notificationType is NEW_EPISODES`() {
        val anime = Anime(title = "Test", notificationType = NotificationType.NEW_EPISODES)

        assertThat(anime.isNotificationsEnabled).isTrue()
    }

    @Test
    fun `isNotificationsEnabled returns true when notificationType is NEW_SEASONS`() {
        val anime = Anime(title = "Test", notificationType = NotificationType.NEW_SEASONS)

        assertThat(anime.isNotificationsEnabled).isTrue()
    }

    @Test
    fun `isNotificationsEnabled returns true when notificationType is BOTH`() {
        val anime = Anime(title = "Test", notificationType = NotificationType.BOTH)

        assertThat(anime.isNotificationsEnabled).isTrue()
    }
}
