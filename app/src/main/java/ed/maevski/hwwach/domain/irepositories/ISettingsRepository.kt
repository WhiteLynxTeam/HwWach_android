package ed.maevski.hwwach.domain.irepositories

import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    fun getLastSyncTime(): Flow<Long>
    suspend fun saveLastSyncTime(timestamp: Long)
}
