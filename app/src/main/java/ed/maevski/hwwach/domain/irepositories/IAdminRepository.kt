package ed.maevski.hwwach.domain.irepositories

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.models.RegistrationRequest

interface IAdminRepository {
    suspend fun getPendingRegistrations(): DomainResult<List<RegistrationRequest>>
    suspend fun getRegistration(id: String): DomainResult<RegistrationRequest>
    suspend fun approveRegistration(id: String, comment: String): DomainResult<Unit>
    suspend fun rejectRegistration(id: String, reason: String): DomainResult<Unit>
}
