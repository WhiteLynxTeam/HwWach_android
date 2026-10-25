package ed.maevski.hwwach.domain.irepositories

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.models.InviteInfo
import ed.maevski.hwwach.domain.models.OrgSearchItem
import ed.maevski.hwwach.domain.models.PersonBrief

// Организации, люди и профиль (ts)
interface IOrgRepository {
    // поиск организации при регистрации: от 4 символов, без подсказок
    suspend fun searchOrganizations(query: String): DomainResult<List<OrgSearchItem>>
    suspend fun resolveInvite(code: String): DomainResult<InviteInfo>

    // регистрация организации (8.1): код на почту → токен → организация и админ
    suspend fun sendOrgCode(email: String): DomainResult<Unit>
    suspend fun verifyOrgCode(email: String, code: String): DomainResult<String>
    suspend fun completeOrgRegistration(
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
    ): DomainResult<Unit>

    // люди организации (ФИО и признак МОЛ) — go отдаёт только person_id
    suspend fun getPersons(force: Boolean = false): DomainResult<List<PersonBrief>>
    suspend fun requestMol(): DomainResult<Unit>

    // перечитать профиль и права (после смены роли, одобрения МОЛ)
    suspend fun refreshProfile(): DomainResult<Unit>
}
