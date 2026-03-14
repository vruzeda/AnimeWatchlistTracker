package com.vuzeda.animewatchlist.tracker.module.repository

interface TransactionRunner {

    suspend fun <T> runInTransaction(block: suspend () -> T): T
}
