package ed.maevski.hwwach.data.local

interface TransactionRunner {
    suspend operator fun <T> invoke(block: suspend () -> T): T
}
