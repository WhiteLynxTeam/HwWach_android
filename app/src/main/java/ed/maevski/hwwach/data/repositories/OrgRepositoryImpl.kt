package ed.maevski.hwwach.data.repositories

import ed.maevski.hwwach.data.local.PreferencesDataStore
import ed.maevski.hwwach.data.mappers.ResponseErrorMapper
import ed.maevski.hwwach.data.mappers.UserResponseDtoToUserDomainMapper
import ed.maevski.hwwach.data.remote.api.OrgApi
import ed.maevski.hwwach.data.remote.api.OrgTokensApi
import ed.maevski.hwwach.data.remote.model.org.CompleteOrgRegistrationRequest
import ed.maevski.hwwach.data.remote.model.org.SendCodeRequest
import ed.maevski.hwwach.data.remote.model.org.VerifyCodeRequest
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IOrgRepository
import ed.maevski.hwwach.domain.irepositories.ITokensRepository
import ed.maevski.hwwach.domain.models.InviteInfo
import ed.maevski.hwwach.domain.models.OrgSearchItem
import ed.maevski.hwwach.domain.models.PersonBrief
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class OrgRepositoryImpl @Inject constructor(
    @Named("auth") private val orgApi: OrgApi,
    @Named("user_token") private val orgTokensApi: OrgTokensApi,
    private val tokensRepository: ITokensRepository,
    private val preferencesDataStore: PreferencesDataStore,
    private val userMapper: UserResponseDtoToUserDomainMapper,
    private val responseErrorMapper: ResponseErrorMapper,
) : IOrgRepository {

    // людей организации немного, а ФИО нужны на каждом экране с активом
    private var personsCache: List<PersonBrief>? = null
    private var personsLoadedAt = 0L

    private suspend fun <T> call(block: suspend () -> DomainResult<T>): DomainResult<T> =
        try {
            block()
        } catch (e: IOException) {
            DomainResult.NetworkError(e.message ?: "Network error")
        }

    override suspend fun searchOrganizations(query: String): DomainResult<List<OrgSearchItem>> = call {
        val response = orgApi.search(query.trim())
        if (!response.isSuccessful) return@call responseErrorMapper.map(response)
        DomainResult.Success(response.body().orEmpty().map { OrgSearchItem(it.id, it.name, it.shortName) })
    }

    override suspend fun resolveInvite(code: String): DomainResult<InviteInfo> = call {
        val response = orgApi.resolveInvite(code.trim())
        if (!response.isSuccessful) return@call responseErrorMapper.map(response)
        val body = response.body() ?: return@call DomainResult.ValidationError("Приглашение не найдено")
        DomainResult.Success(
            InviteInfo(
                code = code.trim(),
                organizationId = body.organizationId,
                organizationName = body.organizationName,
                lastName = body.person?.lastName,
                firstName = body.person?.firstName,
                middleName = body.person?.middleName,
                position = body.person?.position,
            )
        )
    }

    override suspend fun sendOrgCode(email: String): DomainResult<Unit> = call {
        val response = orgApi.sendCode(SendCodeRequest(email.trim()))
        if (!response.isSuccessful) responseErrorMapper.map(response) else DomainResult.Success(Unit)
    }

    override suspend fun verifyOrgCode(email: String, code: String): DomainResult<String> = call {
        val response = orgApi.verifyCode(VerifyCodeRequest(email.trim(), code.trim()))
        if (!response.isSuccessful) return@call responseErrorMapper.map(response)
        val token = response.body()?.registrationToken
            ?: return@call DomainResult.ValidationError("Не удалось подтвердить почту")
        DomainResult.Success(token)
    }

    override suspend fun completeOrgRegistration(
        registrationToken: String,
        name: String,
        shortName: String?,
        address: String?,
        directorName: String?,
        ownerName: String?,
        lastName: String,
        firstName: String,
        middleName: String?,
        position: String?,
        password: String,
    ): DomainResult<Unit> = call {
        val response = orgApi.complete(
            CompleteOrgRegistrationRequest(
                registrationToken = registrationToken,
                name = name.trim(),
                shortName = shortName?.trim()?.takeIf { it.isNotBlank() },
                address = address?.trim()?.takeIf { it.isNotBlank() },
                directorName = directorName?.trim()?.takeIf { it.isNotBlank() },
                ownerName = ownerName?.trim()?.takeIf { it.isNotBlank() },
                lastName = lastName.trim(),
                firstName = firstName.trim(),
                middleName = middleName?.trim()?.takeIf { it.isNotBlank() },
                position = position?.trim()?.takeIf { it.isNotBlank() },
                password = password,
            )
        )
        if (!response.isSuccessful) return@call responseErrorMapper.map(response)
        val body = response.body() ?: return@call DomainResult.ValidationError("Пустой ответ сервера")
        // организация создана — сразу вход администратором
        tokensRepository.saveTokens(body.accessToken, body.refreshToken)
        preferencesDataStore.saveUserProfile(userMapper.map(body.user))
        DomainResult.Success(Unit)
    }

    override suspend fun getPersons(force: Boolean): DomainResult<List<PersonBrief>> = call {
        val cached = personsCache
        if (!force && cached != null && System.currentTimeMillis() - personsLoadedAt < 60_000) {
            return@call DomainResult.Success(cached)
        }
        val response = orgTokensApi.getPersons()
        if (!response.isSuccessful) return@call responseErrorMapper.map(response)
        val list = response.body().orEmpty().map { PersonBrief(it.id, it.fullName, it.position, it.isMol, it.isActive) }
        personsCache = list
        personsLoadedAt = System.currentTimeMillis()
        DomainResult.Success(list)
    }

    override suspend fun requestMol(): DomainResult<Unit> = call {
        val response = orgTokensApi.requestMol()
        if (!response.isSuccessful) return@call responseErrorMapper.map(response)
        refreshProfile()
        DomainResult.Success(Unit)
    }

    override suspend fun refreshProfile(): DomainResult<Unit> = call {
        val response = orgTokensApi.me()
        if (!response.isSuccessful) return@call responseErrorMapper.map(response)
        response.body()?.let { preferencesDataStore.saveUserProfile(userMapper.map(it)) }
        DomainResult.Success(Unit)
    }
}
