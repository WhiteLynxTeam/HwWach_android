package ed.maevski.hwwach.domain.usecases.admin

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IAdminRepository
import ed.maevski.hwwach.domain.models.RegistrationRequest
import javax.inject.Inject

class GetRegistrationUseCase @Inject constructor(
    private val adminRepository: IAdminRepository
) {
    suspend operator fun invoke(id: String): DomainResult<RegistrationRequest> {
        return adminRepository.getRegistration(id)
    }
}
