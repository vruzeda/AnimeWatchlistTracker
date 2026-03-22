package com.vuzeda.animewatchlist.tracker.module.scheduler

interface Scheduler {
    fun schedulePeriodicAnimeUpdate()
    fun scheduleImmediateAnimeUpdate()
}
