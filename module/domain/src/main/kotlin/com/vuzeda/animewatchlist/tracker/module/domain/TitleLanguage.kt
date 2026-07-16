package com.vuzeda.animewatchlist.tracker.module.domain

enum class TitleLanguage {
    DEFAULT,
    ENGLISH,
    JAPANESE,
    ROMAJI
}

/**
 * Resolves the display title for anime/season titles.
 * DEFAULT falls back to Romaji (the traditional behavior for anime/season titles).
 */
fun resolveAnimeDisplayTitle(
    titleRomaji: String,
    titleEnglish: String?,
    titleJapanese: String?,
    language: TitleLanguage
): String =
    titleEnglish?.takeIf { language == TitleLanguage.ENGLISH && it.isNotBlank() }
        ?: titleJapanese?.takeIf { language == TitleLanguage.JAPANESE && it.isNotBlank() }
        ?: titleRomaji

/**
 * Resolves the display title for episode titles.
 * DEFAULT falls back to English (the only guaranteed field from episode APIs).
 */
fun resolveEpisodeDisplayTitle(
    titleRomaji: String?,
    titleEnglish: String?,
    titleJapanese: String?,
    language: TitleLanguage
): String? =
    titleRomaji?.takeIf { language == TitleLanguage.ROMAJI && it.isNotBlank() }
        ?: titleJapanese?.takeIf { language == TitleLanguage.JAPANESE && it.isNotBlank() }
        ?: titleEnglish?.takeIf { it.isNotBlank() }