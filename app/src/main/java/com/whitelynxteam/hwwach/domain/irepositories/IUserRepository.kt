package com.whitelynxteam.hwwach.domain.irepositories

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.models.User


/**
 * Repository interface that defines the contract for user data operations
 */
interface IUserRepository {
    suspend fun auth(user: User): DomainResult<String>
    suspend fun getUserInfo(): DomainResult<User>
}