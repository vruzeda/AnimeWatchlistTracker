package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service

import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.ChiakiWatchOrderEntryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ChiakiServiceImpl(
    private val okHttpClient: OkHttpClient
) : ChiakiService {

    override suspend fun fetchWatchOrder(malId: Int): List<ChiakiWatchOrderEntryDto> =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${ChiakiService.BASE_URL}?/tools/watch_order/id/$malId")
                .header("User-Agent", BROWSER_USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .build()

            val html = okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ChiakiRequestException(
                        malId = malId,
                        statusCode = response.code
                    )
                }
                response.body.string().ifEmpty {
                    throw ChiakiRequestException(
                        malId = malId,
                        statusCode = response.code
                    )
                }
            }

            parseWatchOrderHtml(html, filterToChainContaining = malId)
        }

    companion object {
        private const val BROWSER_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

        private val ROW_PATTERN = Regex(
            """<tr[^>]*\bdata-id="(\d+)"[^>]*\bdata-type="(\d+)"[^>]*\bdata-eps="(\d*)"[^>]*>"""
        )
        private val TITLE_PATTERN = Regex(
            """<span\s+class="wo_title"[^>]*>([^<]+)</span>"""
        )
        private val ENGLISH_TITLE_PATTERN = Regex(
            """<div[^>]*class="[^"]*uk-text-small[^"]*"[^>]*>([^<]+)</div>"""
        )
        private val SCORE_PATTERN = Regex(
            """<span\s+class="wo_rating"[^>]*>★([\d.]+)"""
        )
        private val IMAGE_PATTERN = Regex(
            """<div\s+class="wo_avatar_big"[^>]*style="background-image:\s*url\(['"]?([^'")\s]+)['"]?\)"""
        )
        private val RELATED_PATTERN = Regex("""data-related='(\{[^']*\})'""")
        private val RELATED_ENTRY_PATTERN = Regex(""""(\d+)":"([^"]+)"""")
        private val WO_META_PATTERN = Regex("""<span\s+class="wo_meta">\s*([^|<]+?)\s*\|""")
        private val FULL_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
        private val YEAR_PATTERN = Regex("""\d{4}""")

        private data class ParsedEntry(
            val dto: ChiakiWatchOrderEntryDto,
            val relatedIds: Map<Int, String>
        )

        fun parseWatchOrderHtml(html: String, filterToChainContaining: Int? = null): List<ChiakiWatchOrderEntryDto> {
            val parsedEntries = mutableListOf<ParsedEntry>()

            val rowMatches = ROW_PATTERN.findAll(html).toList()
            for (rowMatch in rowMatches) {
                val malId = rowMatch.groupValues[1].toIntOrNull() ?: continue
                val typeCode = rowMatch.groupValues[2].toIntOrNull() ?: continue
                val episodeCount = rowMatch.groupValues[3].toIntOrNull()

                val rowStart = rowMatch.range.first
                val rowEnd = html.indexOf("</tr>", rowStart).takeIf { it >= 0 }
                    ?: html.length
                val rowHtml = html.substring(rowStart, rowEnd)

                val title = TITLE_PATTERN.find(rowHtml)?.groupValues?.get(1)
                    ?.decodeHtmlEntities() ?: continue
                val titleEnglish = ENGLISH_TITLE_PATTERN.find(rowHtml)?.groupValues?.get(1)
                    ?.decodeHtmlEntities()?.takeIf { it.isNotBlank() }
                val score = SCORE_PATTERN.find(rowHtml)?.groupValues?.get(1)?.toDoubleOrNull()
                val imageRelativePath = IMAGE_PATTERN.find(rowHtml)?.groupValues?.get(1)
                val imageUrl = imageRelativePath?.let { path ->
                    if (path.startsWith("http")) path else "${ChiakiService.BASE_URL}$path"
                }
                val isMainSeries = !rowHtml.contains("wo_row_secondary")

                val relatedIds = RELATED_PATTERN.find(rowHtml)?.groupValues?.get(1)?.let { json ->
                    RELATED_ENTRY_PATTERN.findAll(json).associate { m ->
                        m.groupValues[1].toInt() to m.groupValues[2]
                    }
                } ?: emptyMap()

                val startDate = WO_META_PATTERN.find(rowHtml)?.groupValues?.get(1)
                    ?.let { parseChiakiStartDate(it) }

                parsedEntries += ParsedEntry(
                    dto = ChiakiWatchOrderEntryDto(
                        malId = malId,
                        title = title,
                        titleEnglish = titleEnglish,
                        typeCode = typeCode,
                        episodeCount = episodeCount,
                        score = score,
                        imageUrl = imageUrl,
                        isMainSeries = isMainSeries,
                        startDate = startDate,
                    ),
                    relatedIds = relatedIds,
                )
            }

            if (filterToChainContaining == null) return parsedEntries.map { it.dto }

            val chain = findChain(filterToChainContaining, parsedEntries)
            return parsedEntries.filter { it.dto.malId in chain }.map { it.dto }
        }

        fun parseChiakiStartDate(dateRange: String): LocalDate? {
            val startPart = dateRange.substringBefore(" – ").trim()
            val endPart = dateRange.substringAfter(" – ", missingDelimiterValue = "").trim()
            return try {
                LocalDate.parse(startPart, FULL_DATE_FORMATTER)
            } catch (_: Exception) {
                val year = YEAR_PATTERN.find(endPart)?.value ?: return null
                try {
                    LocalDate.parse("$startPart, $year", FULL_DATE_FORMATTER)
                } catch (_: Exception) {
                    null
                }
            }
        }

        private fun findChain(startMalId: Int, entries: List<ParsedEntry>): Set<Int> {
            val adjacency = mutableMapOf<Int, MutableSet<Int>>()
            for (entry in entries) {
                val id = entry.dto.malId
                for ((relatedId, label) in entry.relatedIds) {
                    if (label != "Alternative Version") {
                        adjacency.getOrPut(id) { mutableSetOf() } += relatedId
                        adjacency.getOrPut(relatedId) { mutableSetOf() } += id
                    }
                }
            }

            val visited = mutableSetOf(startMalId)
            val queue = ArrayDeque<Int>()
            queue += startMalId
            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                for (neighbor in adjacency[current] ?: emptySet()) {
                    if (visited.add(neighbor)) queue += neighbor
                }
            }
            return visited
        }
    }
}

class ChiakiRequestException(
    val malId: Int,
    val statusCode: Int
) : RuntimeException("Chiaki request failed for malId=$malId with status=$statusCode")

private fun String.decodeHtmlEntities(): String =
    replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
