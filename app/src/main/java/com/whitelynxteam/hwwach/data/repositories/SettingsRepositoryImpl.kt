package com.whitelynxteam.hwwach.data.repositories

import com.whitelynxteam.hwwach.data.local.PreferencesDataStore
import com.whitelynxteam.hwwach.domain.irepositories.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) : ISettingsRepository {

    override fun getLastSyncTime(): Flow<Long> = preferencesDataStore.lastSyncTime

    override suspend fun saveLastSyncTime(timestamp: Long) {
        preferencesDataStore.saveLastSyncTime(timestamp)
    }
}
