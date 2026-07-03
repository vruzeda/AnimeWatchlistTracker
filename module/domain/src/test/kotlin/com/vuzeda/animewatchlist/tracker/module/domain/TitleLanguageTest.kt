package com.vuzeda.animewatchlist.tracker.module.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TitleLanguageTest {

    @Test
    fun `DEFAULT is the default value`() {
        val default = TitleLanguage.DEFAULT
        assertThat(default).isEqualTo(TitleLanguage.DEFAULT)
    }

    @Test
    fun `all TitleLanguage entries can be accessed`() {
        val entries = TitleLanguage.entries
        assertThat(entries).contains(TitleLanguage.ENGLISH)
        assertThat(entries).contains(TitleLanguage.JAPANESE)
        assertThat(entries).contains(TitleLanguage.DEFAULT)
    }
}
