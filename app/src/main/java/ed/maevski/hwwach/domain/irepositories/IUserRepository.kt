package ed.maevski.hwwach.domain.irepositories

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.models.RegStatus
import ed.maevski.hwwach.domain.models.Token
import ed.maevski.hwwach.domain.models.User
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

    suspend fun changeTempPassword(login: String, oldPass: String, newPass: String): DomainResult<Unit>
    suspend fun changePassword(oldPass: String, newPass: String): DomainResult<Unit>
}