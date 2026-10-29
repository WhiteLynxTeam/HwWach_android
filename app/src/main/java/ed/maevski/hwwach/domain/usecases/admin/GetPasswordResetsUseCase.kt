package ed.maevski.hwwach.domain.usecases.admin

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IAdminRepository
import ed.maevski.hwwach.domain.models.PasswordResetRequest
import javax.inject.Inject

class GetPasswordResetsUseCase @Inject constructor(
    private val adminRepository: IAdminRepository
) {
    suspend operator fun invoke(): DomainResult<List<PasswordResetRequest>> {
        return adminRepository.getPasswordResets()
    }
}
