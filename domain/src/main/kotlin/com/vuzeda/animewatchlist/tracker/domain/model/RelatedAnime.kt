package com.vuzeda.animewatchlist.tracker.domain.model

data class RelatedAnime(
    val malId: Int,
    val title: String,
    val relationType: RelationType
)

enum class RelationType {
    PREQUEL,
    SEQUEL
}
