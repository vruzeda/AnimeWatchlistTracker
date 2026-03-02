package com.vuzeda.animewatchlist.tracker.data.local.database

import androidx.room.withTransaction
import com.vuzeda.animewatchlist.tracker.domain.repository.TransactionRunner

class RoomTransactionRunner(
    private val database: AnimeDatabase
) : TransactionRunner {

    override suspend fun <T> runInTransaction(block: suspend () -> T): T =
        database.withTransaction { block() }
}
