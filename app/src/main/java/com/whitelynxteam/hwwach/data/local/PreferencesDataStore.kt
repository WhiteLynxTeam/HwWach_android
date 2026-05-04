package com.whitelynxteam.hwwach.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.whitelynxteam.hwwach.domain.models.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hwwach_preferences")

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /*** Типобезопасный с явными методами для каждой сущности */
    private object PreferencesKeys {
        // uuid регистрации во временной таблице для не явной проверки результата регистрации
        val UUID_TEMP = stringPreferencesKey("uuid_temp")

        // Ключи для токена
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")

        // Ключи для профиля пользователя
        val USER_USERNAME_KEY = stringPreferencesKey("user_username")
        val USER_LAST_NAME_KEY = stringPreferencesKey("user_last_name")
        val USER_FIRST_NAME_KEY = stringPreferencesKey("user_first_name")
        val USER_MIDDLE_NAME_KEY = stringPreferencesKey("user_middle_name")
        val USER_PHONE_KEY = stringPreferencesKey("user_phone")
        val USER_POSITION_KEY = stringPreferencesKey("user_position")
        val USER_OFFICE_NAME_KEY = stringPreferencesKey("user_office_name")
        val USER_OFFICE_LOCATION_KEY = stringPreferencesKey("user_office_location")

        // Ключ для времени последней синхронизации (rate limiting)
        val LAST_SYNC_TIME_KEY = longPreferencesKey("last_sync_time")
    }

    // Методы для работы с uuid временной регистрации
    val uuidTemp: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.UUID_TEMP]
        }

    suspend fun saveUUIDTemp(uuidTemp: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.UUID_TEMP] = uuidTemp
        }
    }

    suspend fun clearUUIDTemp() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.UUID_TEMP)
        }
    }

    // Методы для работы с токеном
    val accessToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ACCESS_TOKEN_KEY]
        }

    val refreshToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.REFRESH_TOKEN_KEY]
        }

    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCESS_TOKEN_KEY] = token
        }
    }

    suspend fun saveRefreshToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REFRESH_TOKEN_KEY] = token
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.ACCESS_TOKEN_KEY)
            preferences.remove(PreferencesKeys.REFRESH_TOKEN_KEY)
        }
    }

    // Методы для работы с профилем пользователя
    val userProfile: Flow<User?> = context.dataStore.data
        .map { preferences ->
            val username = preferences[PreferencesKeys.USER_USERNAME_KEY]
            val lastName = preferences[PreferencesKeys.USER_LAST_NAME_KEY]
            val firstName = preferences[PreferencesKeys.USER_FIRST_NAME_KEY]
            val middleName = preferences[PreferencesKeys.USER_MIDDLE_NAME_KEY]
            val phone = preferences[PreferencesKeys.USER_PHONE_KEY]
            val position = preferences[PreferencesKeys.USER_POSITION_KEY]
            val officeName = preferences[PreferencesKeys.USER_OFFICE_NAME_KEY]
            val officeLocation = preferences[PreferencesKeys.USER_OFFICE_LOCATION_KEY]

            if (username != null) {
                User(
                    username = username,
                    lastName = lastName,
                    firstName = firstName,
                    middleName = middleName,
                    phone = phone,
                    position = position,
                    officeName = officeName,
                    officeLocation = officeLocation,
                )
            } else {
                null
            }
        }

    suspend fun saveUserProfile(user: User) {
        context.dataStore.edit { preferences ->
            user.username?.let { preferences[PreferencesKeys.USER_USERNAME_KEY] = it }
            user.lastName?.let { preferences[PreferencesKeys.USER_LAST_NAME_KEY] = it }
            user.firstName?.let { preferences[PreferencesKeys.USER_FIRST_NAME_KEY] = it }
            user.middleName?.let { preferences[PreferencesKeys.USER_MIDDLE_NAME_KEY] = it }
            user.phone?.let { preferences[PreferencesKeys.USER_PHONE_KEY] = it }
            user.position?.let { preferences[PreferencesKeys.USER_POSITION_KEY] = it }
            user.officeName?.let { preferences[PreferencesKeys.USER_OFFICE_NAME_KEY] = it }
            user.officeLocation?.let { preferences[PreferencesKeys.USER_OFFICE_LOCATION_KEY] = it }
        }
    }

    suspend fun clearUserProfile() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_USERNAME_KEY)
            preferences.remove(PreferencesKeys.USER_LAST_NAME_KEY)
            preferences.remove(PreferencesKeys.USER_FIRST_NAME_KEY)
            preferences.remove(PreferencesKeys.USER_MIDDLE_NAME_KEY)
            preferences.remove(PreferencesKeys.USER_PHONE_KEY)
            preferences.remove(PreferencesKeys.USER_POSITION_KEY)
            preferences.remove(PreferencesKeys.USER_OFFICE_NAME_KEY)
            preferences.remove(PreferencesKeys.USER_OFFICE_LOCATION_KEY)
        }
    }

    // Методы для работы с временем последней синхронизации
    val lastSyncTime: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIME_KEY] ?: 0L
        }

    suspend fun saveLastSyncTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIME_KEY] = timestamp
        }
    }
}
