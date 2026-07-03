package com.vuzeda.animewatchlist.tracker.module.domain

enum class AiringStatus(val displayName: String) {
    NOT_YET_AIRED("Not yet aired"),
    CURRENTLY_AIRING("Currently Airing"),
    FINISHED_AIRING("Finished Airing");

    companion object {
        fun fromDisplayName(name: String?): AiringStatus? = values().find { it.displayName == name }
    }
}
