package com.vuzeda.animewatchlist.tracker.module.domain

enum class TitleLanguage {
    DEFAULT,
    ENGLISH,
    JAPANESE
}

fun resolveDisplayTitle(
    title: String,
    titleEnglish: String?,
    titleJapanese: String?,
    language: TitleLanguage
): String = when (language) {
    TitleLanguage.DEFAULT -> title
    TitleLanguage.ENGLISH -> titleEnglish?.takeIf { it.isNotBlank() } ?: title
    TitleLanguage.JAPANESE -> titleJapanese?.takeIf { it.isNotBlank() } ?: title
}
