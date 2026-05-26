package com.whitelynxteam.hwwach.data.local

import androidx.room.withTransaction
import javax.inject.Inject

class RoomTransactionRunner @Inject constructor(
    private val db: AppDatabase
) : TransactionRunner {
    override suspend fun <T> invoke(block: suspend () -> T): T =
        db.withTransaction(block)
}
