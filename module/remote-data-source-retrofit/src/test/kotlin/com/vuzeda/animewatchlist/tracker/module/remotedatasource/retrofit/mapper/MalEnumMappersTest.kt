package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class MalEnumMappersTest {

    @Test
    fun `maps known media types to Tenrai-style display values`() {
        assertThat("tv".malMediaTypeToDisplayType()).isEqualTo("TV")
        assertThat("ova".malMediaTypeToDisplayType()).isEqualTo("OVA")
        assertThat("movie".malMediaTypeToDisplayType()).isEqualTo("Movie")
        assertThat("special".malMediaTypeToDisplayType()).isEqualTo("Special")
        assertThat("ona".malMediaTypeToDisplayType()).isEqualTo("ONA")
        assertThat("music".malMediaTypeToDisplayType()).isEqualTo("Music")
        assertThat("tv_special".malMediaTypeToDisplayType()).isEqualTo("TV Special")
        assertThat("cm".malMediaTypeToDisplayType()).isEqualTo("CM")
        assertThat("pv".malMediaTypeToDisplayType()).isEqualTo("PV")
    }

    @Test
    fun `maps null media type to Unknown`() {
        assertThat(null.malMediaTypeToDisplayType()).isEqualTo("Unknown")
    }

    @Test
    fun `maps unrecognised media type to uppercased words`() {
        assertThat("some_new_type".malMediaTypeToDisplayType()).isEqualTo("SOME NEW TYPE")
    }

    @Test
    fun `maps airing statuses to AiringStatus display names`() {
        assertThat("finished_airing".malStatusToDisplayStatus()).isEqualTo("Finished Airing")
        assertThat("currently_airing".malStatusToDisplayStatus()).isEqualTo("Currently Airing")
        assertThat("not_yet_aired".malStatusToDisplayStatus()).isEqualTo("Not yet aired")
    }

    @Test
    fun `maps unknown or missing status to null`() {
        assertThat("something_else".malStatusToDisplayStatus()).isNull()
        assertThat(null.malStatusToDisplayStatus()).isNull()
    }

    @Test
    fun `maps broadcast days to Tenrai-style plural day names`() {
        assertThat("monday".malBroadcastDayToDisplayDay()).isEqualTo("Mondays")
        assertThat("tuesday".malBroadcastDayToDisplayDay()).isEqualTo("Tuesdays")
        assertThat("wednesday".malBroadcastDayToDisplayDay()).isEqualTo("Wednesdays")
        assertThat("thursday".malBroadcastDayToDisplayDay()).isEqualTo("Thursdays")
        assertThat("friday".malBroadcastDayToDisplayDay()).isEqualTo("Fridays")
        assertThat("saturday".malBroadcastDayToDisplayDay()).isEqualTo("Saturdays")
        assertThat("sunday".malBroadcastDayToDisplayDay()).isEqualTo("Sundays")
    }

    @Test
    fun `maps unknown or missing broadcast day to null`() {
        assertThat("someday".malBroadcastDayToDisplayDay()).isNull()
        assertThat(null.malBroadcastDayToDisplayDay()).isNull()
    }
}
