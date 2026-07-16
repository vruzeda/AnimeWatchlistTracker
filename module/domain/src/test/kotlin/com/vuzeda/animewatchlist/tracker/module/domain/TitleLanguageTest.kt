package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TitleLanguageTest {

    @Test
    fun `DEFAULT is the default value`() {
        val default = TitleLanguage.DEFAULT
        assertThat(default).isEqualTo(TitleLanguage.DEFAULT)
    }

    @Test
    fun `all TitleLanguage entries can be accessed`() {
        val entries = TitleLanguage.entries
        assertThat(entries).contains(TitleLanguage.ENGLISH)
        assertThat(entries).contains(TitleLanguage.JAPANESE)
        assertThat(entries).contains(TitleLanguage.DEFAULT)
        assertThat(entries).contains(TitleLanguage.ROMAJI)
    }

    @Test
    fun `resolveEpisodeDisplayTitle with DEFAULT prefers English`() {
        assertThat(resolveEpisodeDisplayTitle("Romaji", "English", "Japanese", TitleLanguage.DEFAULT)).isEqualTo("English")
        assertThat(resolveEpisodeDisplayTitle("Romaji", null, "Japanese", TitleLanguage.DEFAULT)).isEqualTo(null)
        assertThat(resolveEpisodeDisplayTitle("Romaji", "", "Japanese", TitleLanguage.DEFAULT)).isEqualTo(null)
        assertThat(resolveEpisodeDisplayTitle(null, "English", null, TitleLanguage.DEFAULT)).isEqualTo("English")
        assertThat(resolveEpisodeDisplayTitle(null, null, "Japanese", TitleLanguage.DEFAULT)).isEqualTo(null)
    }

    @Test
    fun `resolveEpisodeDisplayTitle with ENGLISH prefers English`() {
        assertThat(resolveEpisodeDisplayTitle("Romaji", "English", "Japanese", TitleLanguage.ENGLISH)).isEqualTo("English")
        assertThat(resolveEpisodeDisplayTitle("Romaji", null, "Japanese", TitleLanguage.ENGLISH)).isEqualTo(null)
        assertThat(resolveEpisodeDisplayTitle(null, null, "Japanese", TitleLanguage.ENGLISH)).isEqualTo(null)
    }

    @Test
    fun `resolveEpisodeDisplayTitle with JAPANESE prefers Japanese then English`() {
        assertThat(resolveEpisodeDisplayTitle("Romaji", "English", "Japanese", TitleLanguage.JAPANESE)).isEqualTo("Japanese")
        assertThat(resolveEpisodeDisplayTitle("Romaji", "English", null, TitleLanguage.JAPANESE)).isEqualTo("English")
        assertThat(resolveEpisodeDisplayTitle("Romaji", null, null, TitleLanguage.JAPANESE)).isEqualTo(null)
    }

    @Test
    fun `resolveEpisodeDisplayTitle with ROMAJI prefers Romaji then English`() {
        assertThat(resolveEpisodeDisplayTitle("Romaji", "English", "Japanese", TitleLanguage.ROMAJI)).isEqualTo("Romaji")
        assertThat(resolveEpisodeDisplayTitle(null, "English", "Japanese", TitleLanguage.ROMAJI)).isEqualTo("English")
        assertThat(resolveEpisodeDisplayTitle(null, null, "Japanese", TitleLanguage.ROMAJI)).isEqualTo(null)
    }

    @Test
    fun `resolveAnimeDisplayTitle with DEFAULT prefers Romaji`() {
        assertThat(resolveAnimeDisplayTitle("Romaji", "English", "Japanese", TitleLanguage.DEFAULT)).isEqualTo("Romaji")
        assertThat(resolveAnimeDisplayTitle("Romaji", null, "Japanese", TitleLanguage.DEFAULT)).isEqualTo("Romaji")
        assertThat(resolveAnimeDisplayTitle("Romaji", "", "Japanese", TitleLanguage.DEFAULT)).isEqualTo("Romaji")
    }

    @Test
    fun `resolveAnimeDisplayTitle with ENGLISH prefers English then Romaji`() {
        assertThat(resolveAnimeDisplayTitle("Romaji", "English", "Japanese", TitleLanguage.ENGLISH)).isEqualTo("English")
        assertThat(resolveAnimeDisplayTitle("Romaji", null, "Japanese", TitleLanguage.ENGLISH)).isEqualTo("Romaji")
    }

    @Test
    fun `resolveAnimeDisplayTitle with JAPANESE prefers Japanese then Romaji`() {
        assertThat(resolveAnimeDisplayTitle("Romaji", "English", "Japanese", TitleLanguage.JAPANESE)).isEqualTo("Japanese")
        assertThat(resolveAnimeDisplayTitle("Romaji", "English", null, TitleLanguage.JAPANESE)).isEqualTo("Romaji")
        assertThat(resolveAnimeDisplayTitle("Romaji", null, null, TitleLanguage.JAPANESE)).isEqualTo("Romaji")
    }

    @Test
    fun `resolveAnimeDisplayTitle with ROMAJI prefers Romaji`() {
        assertThat(resolveAnimeDisplayTitle("Romaji", "English", "Japanese", TitleLanguage.ROMAJI)).isEqualTo("Romaji")
    }
}