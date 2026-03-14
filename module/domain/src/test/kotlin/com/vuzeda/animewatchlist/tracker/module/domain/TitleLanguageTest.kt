package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TitleLanguageTest {

    @Test
    fun `resolveDisplayTitle returns default title for DEFAULT language`() {
        val result = resolveDisplayTitle(
            title = "Default Title",
            titleEnglish = "English Title",
            titleJapanese = "Japanese Title",
            language = TitleLanguage.DEFAULT
        )

        assertThat(result).isEqualTo("Default Title")
    }

    @Test
    fun `resolveDisplayTitle returns english title when language is ENGLISH and titleEnglish is present`() {
        val result = resolveDisplayTitle(
            title = "Default Title",
            titleEnglish = "English Title",
            titleJapanese = null,
            language = TitleLanguage.ENGLISH
        )

        assertThat(result).isEqualTo("English Title")
    }

    @Test
    fun `resolveDisplayTitle falls back to default title when language is ENGLISH and titleEnglish is null`() {
        val result = resolveDisplayTitle(
            title = "Default Title",
            titleEnglish = null,
            titleJapanese = null,
            language = TitleLanguage.ENGLISH
        )

        assertThat(result).isEqualTo("Default Title")
    }

    @Test
    fun `resolveDisplayTitle falls back to default title when language is ENGLISH and titleEnglish is blank`() {
        val result = resolveDisplayTitle(
            title = "Default Title",
            titleEnglish = "   ",
            titleJapanese = null,
            language = TitleLanguage.ENGLISH
        )

        assertThat(result).isEqualTo("Default Title")
    }

    @Test
    fun `resolveDisplayTitle returns japanese title when language is JAPANESE and titleJapanese is present`() {
        val result = resolveDisplayTitle(
            title = "Default Title",
            titleEnglish = null,
            titleJapanese = "日本語タイトル",
            language = TitleLanguage.JAPANESE
        )

        assertThat(result).isEqualTo("日本語タイトル")
    }

    @Test
    fun `resolveDisplayTitle falls back to default title when language is JAPANESE and titleJapanese is null`() {
        val result = resolveDisplayTitle(
            title = "Default Title",
            titleEnglish = null,
            titleJapanese = null,
            language = TitleLanguage.JAPANESE
        )

        assertThat(result).isEqualTo("Default Title")
    }

    @Test
    fun `resolveDisplayTitle falls back to default title when language is JAPANESE and titleJapanese is blank`() {
        val result = resolveDisplayTitle(
            title = "Default Title",
            titleEnglish = null,
            titleJapanese = "",
            language = TitleLanguage.JAPANESE
        )

        assertThat(result).isEqualTo("Default Title")
    }
}
