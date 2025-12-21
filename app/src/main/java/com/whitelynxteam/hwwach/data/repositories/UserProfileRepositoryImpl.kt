package com.whitelynxteam.hwwach.data.repositories

import com.whitelynxteam.hwwach.data.local.PreferencesDataStore
import com.whitelynxteam.hwwach.domain.irepositories.IUserProfileRepository
import com.whitelynxteam.hwwach.domain.models.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) : IUserProfileRepository {
    
    override val userProfile: Flow<User?> = preferencesDataStore.userProfile

    override suspend fun saveUserProfile(user: User) {
        preferencesDataStore.saveUserProfile(user)
    }

    override suspend fun clearUserProfile() {
        preferencesDataStore.clearUserProfile()
    }
}