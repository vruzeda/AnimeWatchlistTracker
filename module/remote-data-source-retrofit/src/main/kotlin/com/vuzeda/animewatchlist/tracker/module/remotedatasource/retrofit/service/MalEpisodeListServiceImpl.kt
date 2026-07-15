package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service

import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalEpisodeListPageDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalEpisodeRowDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.text.Regex
import kotlin.text.RegexOption

class MalEpisodeListServiceImpl(
    private val okHttpClient: OkHttpClient
) : MalEpisodeListService {

    override suspend fun fetchEpisodePage(malId: Int, page: Int): MalEpisodeListPageDto =
        withContext(Dispatchers.IO) {
            val offset = (page - 1) * MalEpisodeListService.PAGE_SIZE
            val request = Request.Builder()
                .url("${MalEpisodeListService.BASE_URL}/anime/$malId/_/episode?offset=$offset")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .build()

            val html = okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw MalEpisodeListRequestException(
                        malId = malId,
                        statusCode = response.code
                    )
                }
                response.body.string().ifEmpty {
                    throw MalEpisodeListRequestException(
                        malId = malId,
                        statusCode = response.code
                    )
                }
            }

            parseEpisodeListHtml(html, currentOffset = offset)
        }

    companion object {
        private val ROW_PATTERN = Regex(
            """<tr class="episode-list-data".*?</tr>""",
            RegexOption.DOT_MATCHES_ALL
        )
        private val NUMBER_PATTERN = Regex(
            """<td[^>]*class="episode-number[^"]*"[^>]*data-raw="(\d+)""""
        )
        private val TITLE_PATTERN = Regex(
            """<td[^>]*class="episode-title[^"]*"[^>]*>\s*<a[^>]*>([^<]*)</a>"""
        )
        private val JAPANESE_TITLE_PATTERN = Regex(
            """<br>\s*<span[^>]*class="di-ib"[^>]*>([^<]*)</span>"""
        )
        private val AIRED_PATTERN = Regex(
            """<td[^>]*class="episode-aired[^"]*"[^>]*>([^<]*)</td>"""
        )
        private val AIRED_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)

        fun parseEpisodeListHtml(html: String, currentOffset: Int): MalEpisodeListPageDto {
            val episodes = ROW_PATTERN.findAll(html).mapNotNull { rowMatch ->
                parseEpisodeRow(rowMatch.value)
            }.toList()

            val nextOffset = currentOffset + MalEpisodeListService.PAGE_SIZE
            return MalEpisodeListPageDto(
                episodes = episodes,
                hasNextPage = html.contains("episode?offset=$nextOffset")
            )
        }

        private fun parseEpisodeRow(rowHtml: String): MalEpisodeRowDto? {
            val number = NUMBER_PATTERN.find(rowHtml)?.groupValues?.get(1)?.toIntOrNull()
                ?: return null
            val titleEnglish = TITLE_PATTERN.find(rowHtml)?.groupValues?.get(1)
                ?.decodeHtmlEntities()?.trim()?.takeIf { it.isNotEmpty() }

            val japaneseWithRomaji = JAPANESE_TITLE_PATTERN.find(rowHtml)?.groupValues?.get(1)
                ?.decodeHtmlEntities()?.trim()?.takeIf { it.isNotEmpty() }
            val titleJapanese = extractJapaneseTitle(japaneseWithRomaji)

            val airedIsoDate = AIRED_PATTERN.find(rowHtml)?.groupValues?.get(1)?.trim()
                ?.let { airedText ->
                    runCatching { LocalDate.parse(airedText, AIRED_DATE_FORMATTER) }.getOrNull()
                }
                ?.toString()
            return MalEpisodeRowDto(
                number = number,
                title = titleEnglish, // default/fallback title is English
                titleEnglish = titleEnglish,
                titleJapanese = titleJapanese,
                airedIsoDate = airedIsoDate
            )
        }
    }
}

internal fun extractJapaneseTitle(japaneseWithRomaji: String?): String? {
    return japaneseWithRomaji?.let { text ->
        // Pattern: "Romaji (日本語)" or "(日本語)" or "Romaji"
        // We want to extract just the Japanese characters inside parentheses, or the whole text if no parentheses
        val parenPattern = Regex("\\(([^)]+)\\)")
        val match = parenPattern.find(text)
        if (match != null) {
            val insideParens = match.groupValues[1]
            // Check if it contains Japanese characters (Kanji, Hiragana, Katakana)
            if (insideParens.any { it.isJapaneseLetterOrDigit() }) {
                insideParens
            } else {
                // No Japanese chars in parens, might be just Romaji in parens
                // Return the text without the parenthesized part
                text.replace(parenPattern, "").trim()
            }
        } else {
            // No parentheses, return as-is (could be just Romaji or just Japanese)
            text
        }
    }
}

internal fun Char.isJapaneseLetterOrDigit(): Boolean {
    return when {
        this in '㐀'..'䶿' -> true // CJK Unified Ideographs Extension A
        this in '一'..'龯' -> true // CJK Unified Ideographs
        this in '぀'..'ゟ' -> true // Hiragana
        this in '゠'..'ヿ' -> true // Katakana
        this in 'ㇰ'..'ㇿ' -> true // Katakana Phonetic Extensions
        this in '㈀'..'㈿' -> true // Enclosed CJK Letters
        this in '㊀'..'㊿' -> true // Enclosed CJK Letters and Months
        else -> false
    }
}

class MalEpisodeListRequestException(
    val malId: Int,
    val statusCode: Int
) : RuntimeException("MAL episode list request failed for malId=$malId with status=$statusCode")
