package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ChiakiServiceImplTest {

    @Test
    fun `parses multiple entries from HTML`() {
        val html = buildSampleHtml(
            entry(malId = 16498, typeCode = 1, eps = 25, title = "Shingeki no Kyojin", score = "8.54", ratingCount = "2M", image = "media/images/16498.jpg"),
            entry(malId = 25777, typeCode = 1, eps = 12, title = "Shingeki no Kyojin Season 2", score = "8.48", ratingCount = "1.5M", image = "media/images/25777.jpg")
        )

        val result = ChiakiServiceImpl.parseWatchOrderHtml(html)

        assertThat(result).hasSize(2)
        assertThat(result[0].malId).isEqualTo(16498)
        assertThat(result[0].title).isEqualTo("Shingeki no Kyojin")
        assertThat(result[0].typeCode).isEqualTo(1)
        assertThat(result[0].episodeCount).isEqualTo(25)
        assertThat(result[0].score).isEqualTo(8.54)
        assertThat(result[0].imageUrl).isEqualTo("https://chiaki.site/media/images/16498.jpg")

        assertThat(result[1].malId).isEqualTo(25777)
        assertThat(result[1].title).isEqualTo("Shingeki no Kyojin Season 2")
    }

    @Test
    fun `parses entry with no episode count`() {
        val html = buildSampleHtml(
            entry(malId = 100, typeCode = 3, eps = null, title = "Some Movie", score = "7.50", ratingCount = "500K", image = "media/images/100.jpg")
        )

        val result = ChiakiServiceImpl.parseWatchOrderHtml(html)

        assertThat(result).hasSize(1)
        assertThat(result[0].episodeCount).isNull()
        assertThat(result[0].typeCode).isEqualTo(3)
    }

    @Test
    fun `parses entry with no score`() {
        val html = buildSampleHtml(
            entry(malId = 200, typeCode = 1, eps = 12, title = "New Anime", score = null, ratingCount = null, image = "media/images/200.jpg")
        )

        val result = ChiakiServiceImpl.parseWatchOrderHtml(html)

        assertThat(result).hasSize(1)
        assertThat(result[0].score).isNull()
    }

    @Test
    fun `parses entry with no image`() {
        val html = buildSampleHtml(
            entry(malId = 300, typeCode = 1, eps = 24, title = "No Image Anime", score = "8.00", ratingCount = "100K", image = null)
        )

        val result = ChiakiServiceImpl.parseWatchOrderHtml(html)

        assertThat(result).hasSize(1)
        assertThat(result[0].imageUrl).isNull()
    }

    @Test
    fun `decodes HTML entities in title`() {
        val html = buildSampleHtml(
            entry(malId = 400, typeCode = 1, eps = 12, title = "Tom &amp; Jerry&#39;s Adventure", score = "7.00", ratingCount = "50K", image = null)
        )

        val result = ChiakiServiceImpl.parseWatchOrderHtml(html)

        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("Tom & Jerry's Adventure")
    }

    @Test
    fun `returns empty list for HTML with no entries`() {
        val html = "<html><body><table></table></body></html>"

        val result = ChiakiServiceImpl.parseWatchOrderHtml(html)

        assertThat(result).isEmpty()
    }

    @Test
    fun `prepends base URL to relative image paths`() {
        val html = buildSampleHtml(
            entry(malId = 500, typeCode = 1, eps = 12, title = "Relative Image", score = "8.00", ratingCount = "100K", image = "media/images/500.jpg")
        )

        val result = ChiakiServiceImpl.parseWatchOrderHtml(html)

        assertThat(result[0].imageUrl).startsWith("https://chiaki.site/")
    }

    @Test
    fun `preserves absolute image URL`() {
        val html = buildSampleHtml(
            entry(malId = 600, typeCode = 1, eps = 12, title = "Absolute Image", score = "8.00", ratingCount = "100K", image = "https://cdn.example.com/img.jpg")
        )

        val result = ChiakiServiceImpl.parseWatchOrderHtml(html)

        assertThat(result[0].imageUrl).isEqualTo("https://cdn.example.com/img.jpg")
    }

    @Test
    fun `marks secondary entries with isMainSeries false`() {
        val html = buildSampleHtml(
            entry(malId = 700, typeCode = 1, eps = 24, title = "Main Series", score = null, ratingCount = null, image = null),
            entry(malId = 701, typeCode = 1, eps = 1, title = "Side Story", score = null, ratingCount = null, image = null, isSecondary = true)
        )

        val result = ChiakiServiceImpl.parseWatchOrderHtml(html)

        assertThat(result).hasSize(2)
        assertThat(result[0].isMainSeries).isTrue()
        assertThat(result[1].isMainSeries).isFalse()
    }

    @Test
    fun `parses English title when present and non-blank`() {
        val html = buildSampleHtml(
            entry(malId = 800, typeCode = 1, eps = 25, title = "Shingeki no Kyojin", score = null, ratingCount = null, image = null, englishTitle = "Attack on Titan")
        )

        val result = ChiakiServiceImpl.parseWatchOrderHtml(html)

        assertThat(result[0].titleEnglish).isEqualTo("Attack on Titan")
    }

    @Test
    fun `returns null English title when English title div is blank`() {
        val html = buildSampleHtml(
            entry(malId = 900, typeCode = 1, eps = 12, title = "Some Anime", score = null, ratingCount = null, image = null, englishTitle = "   ")
        )

        val result = ChiakiServiceImpl.parseWatchOrderHtml(html)

        assertThat(result[0].titleEnglish).isNull()
    }

    @Test
    fun `handles row with no closing tr tag`() {
        val html = """
            <html><body><table>
            <tr data-id="999" data-type="1" data-eps="12" class="wo_row">
                <td><span class="wo_title">No End Tag</span></td>
            </table></body></html>
        """.trimIndent()

        val result = ChiakiServiceImpl.parseWatchOrderHtml(html)

        assertThat(result).hasSize(1)
        assertThat(result[0].malId).isEqualTo(999)
    }

    private fun entry(
        malId: Int,
        typeCode: Int,
        eps: Int?,
        title: String,
        score: String?,
        ratingCount: String?,
        image: String?,
        isSecondary: Boolean = false,
        englishTitle: String? = null
    ): String {
        val epsAttr = eps?.toString() ?: ""
        val rowClass = if (isSecondary) "wo_row_secondary" else "wo_row"
        val scoreHtml = if (score != null) """<span class="wo_rating">★$score ($ratingCount)</span>""" else ""
        val imageHtml = if (image != null) """<div class="wo_avatar_big" style="background-image: url('$image')"></div>""" else ""
        val englishTitleHtml = if (englishTitle != null) """<div class="uk-text-small">$englishTitle</div>""" else ""
        return """
            <tr data-id="$malId" data-type="$typeCode" data-eps="$epsAttr" class="$rowClass">
                <td>$imageHtml</td>
                <td><span class="wo_title">$title</span>$englishTitleHtml</td>
                <td>$scoreHtml</td>
            </tr>
        """.trimIndent()
    }

    private fun buildSampleHtml(vararg entries: String): String =
        """
        <html><body>
        <table class="wo_table">
        ${entries.joinToString("\n")}
        </table>
        </body></html>
        """.trimIndent()
}
