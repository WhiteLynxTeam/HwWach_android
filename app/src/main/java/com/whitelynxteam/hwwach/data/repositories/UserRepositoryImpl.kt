package com.whitelynxteam.hwwach.data.repositories

import com.whitelynxteam.hwwach.data.local.PreferencesDataStore
import com.whitelynxteam.hwwach.data.mappers.RegResponseDtoToUserDomainMapper
import com.whitelynxteam.hwwach.data.mappers.RegStatusResponseDtoToRegStatusDomainMapper
import com.whitelynxteam.hwwach.data.mappers.ResponseErrorMapper
import com.whitelynxteam.hwwach.data.mappers.UserDomainToAuthUserRequestMapper
import com.whitelynxteam.hwwach.data.mappers.UserDomainToRegUserRequestMapper
import com.whitelynxteam.hwwach.data.remote.api.UserApi
import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IUserRepository
import com.whitelynxteam.hwwach.domain.models.RegStatus
import com.whitelynxteam.hwwach.domain.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class UserRepositoryImpl @Inject constructor(
    @Named("auth") private val userApi: UserApi,
    private val userDomainToAuthUserRequestMapper: UserDomainToAuthUserRequestMapper,
    private val userDomainToRegUserRequestMapper: UserDomainToRegUserRequestMapper,
    private val regResponseDtoToUserDomainMapper: RegResponseDtoToUserDomainMapper,
    private val regStatusResponseDtoToRegStatusDomainMapper: RegStatusResponseDtoToRegStatusDomainMapper,
    private val responseErrorMapper: ResponseErrorMapper,
    private val preferencesDataStore: PreferencesDataStore,
) : IUserRepository {

    private val _uuidTempCache = MutableStateFlow<String?>(null)
    override val uuidTempCache: StateFlow<String?> = _uuidTempCache.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            preferencesDataStore.uuidTemp.collect {
                _uuidTempCache.value = it
            }
        }
    }

    override val uuidTemp: Flow<String?> = preferencesDataStore.uuidTemp

    override suspend fun saveUUIDTemp(uuidTemp: String) {
        preferencesDataStore.saveUUIDTemp(uuidTemp)
    }

    override suspend fun clearUUIDTemp() {
        preferencesDataStore.clearUUIDTemp()
    }

    override suspend fun reg(user: User): DomainResult<User> {
        val userRegRequest = userDomainToRegUserRequestMapper.map(user)
            ?: return DomainResult.ValidationError(
                when {
                    user.username.isNullOrBlank() -> "Login is empty"
                    user.password.isNullOrBlank() -> "Password is empty"
                    user.firstName.isNullOrBlank() -> "Имя is empty"
                    user.lastName.isNullOrBlank() -> "Фамилия is empty"
                    else -> "Invalid credentials"
                }
            )

        val response = userApi.reg(userRegRequest)

        return when {
            !response.isSuccessful -> responseErrorMapper.map(response)
            else -> {
                val regResponseDto = response.body() ?: return DomainResult.ValidationError("Registration response not found")

                // сохраняем временный uuid локально после регистрации во временной таблицы регистрации
                // перед одобрением администратора
                saveUUIDTemp(regResponseDto.uuid)

                val mappedUser = regResponseDtoToUserDomainMapper.map(regResponseDto)
                DomainResult.Success(mappedUser)
            }
        }
    }

    override suspend fun auth(user: User): DomainResult<String> {
        val userAuthRequest = userDomainToAuthUserRequestMapper.map(user)
            ?: return DomainResult.ValidationError(
                when {
                    user.username.isNullOrBlank() -> "Login is empty"
                    user.password.isNullOrBlank() -> "Password is empty"
                    else -> "Invalid credentials"
                }
            )

        val response = userApi.auth(userAuthRequest)

        return when {
            !response.isSuccessful -> responseErrorMapper.map(response)
            else -> {
                val token = response.body()?.accessToken ?: return DomainResult.ValidationError("Token not found")
                
                // Очищаем временный uuid после успешной аутентификации
                clearUUIDTemp()
                
                DomainResult.Success(token)
            }
        }
    }

    override suspend fun getRegStatus(): DomainResult<RegStatus> {
        val tempUuid = preferencesDataStore.uuidTemp.firstOrNull()
            ?: return DomainResult.Success(RegStatus())

        val response = userApi.statusReg(tempUuid)

        return when {
            !response.isSuccessful -> responseErrorMapper.map(response)
            else -> {
                val status = response.body() ?: return DomainResult.ValidationError("RegStatus not found")
                val mappedStatus = regStatusResponseDtoToRegStatusDomainMapper.map(status)
                DomainResult.Success(mappedStatus)
            }
        }
    }
}