package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service

import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.ChiakiWatchOrderEntryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class ChiakiServiceImpl(
    private val okHttpClient: OkHttpClient
) : ChiakiService {

    override suspend fun fetchWatchOrder(malId: Int): List<ChiakiWatchOrderEntryDto> =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${ChiakiService.BASE_URL}?/tools/watch_order/id/$malId")
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

            parseWatchOrderHtml(html)
        }

    companion object {
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
            """<div\s+class="wo_avatar_big"[^>]*style="background-image:\s*url\('([^']+)'\)"""
        )

        fun parseWatchOrderHtml(html: String): List<ChiakiWatchOrderEntryDto> {
            val entries = mutableListOf<ChiakiWatchOrderEntryDto>()

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

                entries += ChiakiWatchOrderEntryDto(
                    malId = malId,
                    title = title,
                    titleEnglish = titleEnglish,
                    typeCode = typeCode,
                    episodeCount = episodeCount,
                    score = score,
                    imageUrl = imageUrl,
                    isMainSeries = isMainSeries,
                )
            }

            return entries
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
