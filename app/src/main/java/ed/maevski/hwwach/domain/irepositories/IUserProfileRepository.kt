package ed.maevski.hwwach.domain.irepositories

import ed.maevski.hwwach.domain.models.User
import kotlinx.coroutines.flow.Flow

interface IUserProfileRepository {
    val userProfile: Flow<User?>
    suspend fun saveUserProfile(user: User)
    suspend fun clearUserProfile()
}