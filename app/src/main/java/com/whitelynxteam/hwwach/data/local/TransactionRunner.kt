package com.whitelynxteam.hwwach.data.local

interface TransactionRunner {
    suspend operator fun <T> invoke(block: suspend () -> T): T
}
