package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.service

private val HTML_ENTITY_PATTERN = Regex("""&(?:#(\d+)|#x([0-9a-fA-F]+)|([a-zA-Z]+));""")

private val NAMED_HTML_ENTITIES = mapOf(
    "amp" to "&",
    "lt" to "<",
    "gt" to ">",
    "quot" to "\"",
    "apos" to "'",
    "nbsp" to " ",
)

internal fun String.decodeHtmlEntities(): String =
    replace(HTML_ENTITY_PATTERN) { mr ->
        when {
            mr.groupValues[1].isNotEmpty() ->
                mr.groupValues[1].toIntOrNull()
                    ?.let { runCatching { String(Character.toChars(it)) }.getOrNull() }
                    ?: mr.value
            mr.groupValues[2].isNotEmpty() ->
                mr.groupValues[2].toIntOrNull(16)
                    ?.let { runCatching { String(Character.toChars(it)) }.getOrNull() }
                    ?: mr.value
            else ->
                NAMED_HTML_ENTITIES[mr.groupValues[3]] ?: mr.value
        }
    }
