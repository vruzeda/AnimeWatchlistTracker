package com.vuzeda.animewatchlist.tracker.module.scheduler

interface AnimeUpdateScheduler {
    fun schedulePeriodicUpdate()
    fun scheduleImmediateUpdate()
}
