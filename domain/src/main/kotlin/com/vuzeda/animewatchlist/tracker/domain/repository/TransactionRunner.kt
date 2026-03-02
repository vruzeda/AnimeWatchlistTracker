package com.vuzeda.animewatchlist.tracker.domain.repository

interface TransactionRunner {

    suspend fun <T> runInTransaction(block: suspend () -> T): T
}
