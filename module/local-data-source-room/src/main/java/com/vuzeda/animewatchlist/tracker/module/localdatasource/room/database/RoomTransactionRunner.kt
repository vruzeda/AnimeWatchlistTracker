package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database

import androidx.room.withTransaction
import com.vuzeda.animewatchlist.tracker.module.repository.TransactionRunner

class RoomTransactionRunner(
    private val database: AnimeDatabase
) : TransactionRunner {

    override suspend fun <T> runInTransaction(block: suspend () -> T): T =
        database.withTransaction { block() }
}
