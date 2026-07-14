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
            val title = TITLE_PATTERN.find(rowHtml)?.groupValues?.get(1)
                ?.decodeHtmlEntities()?.trim()?.takeIf { it.isNotEmpty() }
            val airedIsoDate = AIRED_PATTERN.find(rowHtml)?.groupValues?.get(1)?.trim()
                ?.let { airedText ->
                    runCatching { LocalDate.parse(airedText, AIRED_DATE_FORMATTER) }.getOrNull()
                }
                ?.toString()
            return MalEpisodeRowDto(number = number, title = title, airedIsoDate = airedIsoDate)
        }
    }
}

class MalEpisodeListRequestException(
    val malId: Int,
    val statusCode: Int
) : RuntimeException("MAL episode list request failed for malId=$malId with status=$statusCode")
