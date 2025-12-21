package com.whitelynxteam.hwwach.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.whitelynxteam.hwwach.domain.models.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "miel_preferences")

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /*** Типобезопасный с явными методами для каждой сущности */
    private object PreferencesKeys {
        // Ключи для токена
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")

        // Ключи для профиля пользователя
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_FULL_NAME_KEY = stringPreferencesKey("user_full_name")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        val USER_PHONE_KEY = stringPreferencesKey("user_phone")
        val USER_PHOTO_KEY = stringPreferencesKey("user_photo")
        val USER_OFFICE_NAME_KEY = stringPreferencesKey("user_office_name")
        val USER_OFFICE_LOCATION_KEY = stringPreferencesKey("user_office_location")
        val USER_DEPARTMENT_KEY = stringPreferencesKey("user_department")
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
            val username = preferences[PreferencesKeys.USER_NAME_KEY]
            val fullName = preferences[PreferencesKeys.USER_FULL_NAME_KEY]
            val email = preferences[PreferencesKeys.USER_EMAIL_KEY]
            val phone = preferences[PreferencesKeys.USER_PHONE_KEY]
            val photo = preferences[PreferencesKeys.USER_PHOTO_KEY]
            val officeName = preferences[PreferencesKeys.USER_OFFICE_NAME_KEY]
            val officeLocation = preferences[PreferencesKeys.USER_OFFICE_LOCATION_KEY]
            val department = preferences[PreferencesKeys.USER_DEPARTMENT_KEY]

            if (username != null) {
                User(
                    username = username,
                    fullName = fullName,
                    phone = phone,
                    officeName = officeName,
                    officeLocation = officeLocation,
                )
            } else {
                null
            }
        }

    suspend fun saveUserProfile(user: User) {
        context.dataStore.edit { preferences ->
            user.username?.let { preferences[PreferencesKeys.USER_NAME_KEY] = it }
            user.fullName?.let { preferences[PreferencesKeys.USER_FULL_NAME_KEY] = it }
            user.phone?.let { preferences[PreferencesKeys.USER_PHONE_KEY] = it }
            user.officeName?.let { preferences[PreferencesKeys.USER_OFFICE_NAME_KEY] = it }
            user.officeLocation?.let { preferences[PreferencesKeys.USER_OFFICE_LOCATION_KEY] = it }
        }
    }

    suspend fun clearUserProfile() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_NAME_KEY)
            preferences.remove(PreferencesKeys.USER_FULL_NAME_KEY)
            preferences.remove(PreferencesKeys.USER_EMAIL_KEY)
            preferences.remove(PreferencesKeys.USER_PHONE_KEY)
            preferences.remove(PreferencesKeys.USER_PHOTO_KEY)
            preferences.remove(PreferencesKeys.USER_OFFICE_NAME_KEY)
            preferences.remove(PreferencesKeys.USER_OFFICE_LOCATION_KEY)
            preferences.remove(PreferencesKeys.USER_DEPARTMENT_KEY)
        }
    }
}
