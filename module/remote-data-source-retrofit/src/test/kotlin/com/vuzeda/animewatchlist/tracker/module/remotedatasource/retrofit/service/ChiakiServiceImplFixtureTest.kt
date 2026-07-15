package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.ChiakiWatchOrderEntryDto
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ChiakiServiceImplFixtureTest {

    @Test
    fun `parses Fruits Basket watch order from real chiaki HTML fixture`() {
        val html = loadFixture("fixtures/chiaki_watch_order_40417_fruits_basket.html")

        val result = ChiakiServiceImpl.parseWatchOrderHtml(html)

        assertThat(result).isEqualTo(
            listOf(
                ChiakiWatchOrderEntryDtoFixture.entry120,
                ChiakiWatchOrderEntryDtoFixture.entry38680,
                ChiakiWatchOrderEntryDtoFixture.entry40417,
                ChiakiWatchOrderEntryDtoFixture.entry53751,
                ChiakiWatchOrderEntryDtoFixture.entry42938,
                ChiakiWatchOrderEntryDtoFixture.entry49310,
            )
        )
    }

    @Test
    fun `filters chain for 2001 series (malId 120) returns only itself`() {
        val html = loadFixture("fixtures/chiaki_watch_order_40417_fruits_basket.html")

        val chain = ChiakiServiceImpl.parseWatchOrderHtml(html, filterToChainContaining = 120)

        assertThat(chain).isEqualTo(
            listOf(
                ChiakiWatchOrderEntryDtoFixture.entry120,
            )
        )
    }

    @Test
    fun `filters chain for 1st Season (malId 38680) returns the entire chain`() {
        val html = loadFixture("fixtures/chiaki_watch_order_40417_fruits_basket.html")

        val chain = ChiakiServiceImpl.parseWatchOrderHtml(html, filterToChainContaining = 38680)

        assertThat(chain).isEqualTo(
            listOf(
                ChiakiWatchOrderEntryDtoFixture.entry38680,
                ChiakiWatchOrderEntryDtoFixture.entry40417,
                ChiakiWatchOrderEntryDtoFixture.entry53751,
                ChiakiWatchOrderEntryDtoFixture.entry42938,
                ChiakiWatchOrderEntryDtoFixture.entry49310,
            )
        )
    }

    @Test
    fun `filters chain for 2nd Season (malId 40417) returns the entire chain`() {
        val html = loadFixture("fixtures/chiaki_watch_order_40417_fruits_basket.html")

        val chain = ChiakiServiceImpl.parseWatchOrderHtml(html, filterToChainContaining = 40417)

        assertThat(chain).isEqualTo(
            listOf(
                ChiakiWatchOrderEntryDtoFixture.entry38680,
                ChiakiWatchOrderEntryDtoFixture.entry40417,
                ChiakiWatchOrderEntryDtoFixture.entry53751,
                ChiakiWatchOrderEntryDtoFixture.entry42938,
                ChiakiWatchOrderEntryDtoFixture.entry49310,
            )
        )
    }

    private fun loadFixture(name: String): String {
        val resource = javaClass.getResource("/$name")
            ?: throw IllegalStateException("Fixture not found: $name")
        return resource.readText()
    }

    private object ChiakiWatchOrderEntryDtoFixture {
        val entry120 = ChiakiWatchOrderEntryDto(
            malId = 120,
            title = "Fruits Basket",
            titleEnglish = null,
            typeCode = 1,
            episodeCount = 26,
            score = 7.69,
            imageUrl = "https://chiaki.site/media/a/ab/120.jpg",
            isMainSeries = true,
            startDate = LocalDate.of(2001, 7, 5),
            endDate = LocalDate.of(2001, 12, 27),
        )

        val entry38680 = ChiakiWatchOrderEntryDto(
            malId = 38680,
            title = "Fruits Basket 1st Season",
            titleEnglish = null,
            typeCode = 1,
            episodeCount = 25,
            score = 8.19,
            imageUrl = "https://chiaki.site/media/a/ea/38680.jpg",
            isMainSeries = true,
            startDate = LocalDate.of(2019, 4, 6),
            endDate = LocalDate.of(2019, 9, 21),
        )

        val entry40417 = ChiakiWatchOrderEntryDto(
            malId = 40417,
            title = "Fruits Basket 2nd Season",
            titleEnglish = null,
            typeCode = 1,
            episodeCount = 25,
            score = 8.52,
            imageUrl = "https://chiaki.site/media/a/51/40417.jpg",
            isMainSeries = true,
            startDate = LocalDate.of(2020, 4, 7),
            endDate = LocalDate.of(2020, 9, 22),
        )

        val entry53751 = ChiakiWatchOrderEntryDto(
            malId = 53751,
            title = "Ad Meliora",
            titleEnglish = null,
            typeCode = 6,
            episodeCount = 1,
            score = 6.37,
            imageUrl = "https://chiaki.site/media/a/02/53751.jpg",
            isMainSeries = false,
            startDate = LocalDate.of(2020, 4, 13),
            endDate = LocalDate.of(2020, 4, 13),
        )

        val entry42938 = ChiakiWatchOrderEntryDto(
            malId = 42938,
            title = "Fruits Basket: The Final",
            titleEnglish = "Fruits Basket: The Final Season",
            typeCode = 1,
            episodeCount = 13,
            score = 8.93,
            imageUrl = "https://chiaki.site/media/a/08/42938.jpg",
            isMainSeries = true,
            startDate = LocalDate.of(2021, 4, 6),
            endDate = LocalDate.of(2021, 6, 29),
        )

        val entry49310 = ChiakiWatchOrderEntryDto(
            malId = 49310,
            title = "Fruits Basket: Prelude",
            titleEnglish = null,
            typeCode = 3,
            episodeCount = 1,
            score = 8.36,
            imageUrl = "https://chiaki.site/media/a/dd/49310.jpg",
            isMainSeries = true,
            startDate = LocalDate.of(2022, 2, 18),
            endDate = LocalDate.of(2022, 2, 18),
        )
    }
}