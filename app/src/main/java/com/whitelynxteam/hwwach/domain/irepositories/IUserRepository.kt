package com.whitelynxteam.hwwach.domain.irepositories

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.models.RegStatus
import com.whitelynxteam.hwwach.domain.models.RegStatusEnum
import com.whitelynxteam.hwwach.domain.models.Token
import com.whitelynxteam.hwwach.domain.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface IUserRepository {
    val uuidTempCache: StateFlow<String?>
    val uuidTemp: Flow<String?>

    suspend fun saveUUIDTemp(uuidTemp: String)
    suspend fun clearUUIDTemp()


    suspend fun reg(user: User): DomainResult<User>
    suspend fun auth(user: User): DomainResult<Pair<Token, User>>

    suspend fun getRegStatus(): DomainResult<RegStatus>

    suspend fun getUserInfo(id: String): DomainResult<User>
}