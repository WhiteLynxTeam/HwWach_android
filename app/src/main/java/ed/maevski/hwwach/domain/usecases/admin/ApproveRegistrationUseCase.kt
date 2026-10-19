package ed.maevski.hwwach.domain.usecases.admin

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IAdminRepository
import javax.inject.Inject

class ApproveRegistrationUseCase @Inject constructor(
    private val adminRepository: IAdminRepository
) {
    suspend operator fun invoke(id: String): DomainResult<Unit> {
        // Комментарий пока фиксированный — как в Wails
        return adminRepository.approveRegistration(id, DEFAULT_APPROVE_COMMENT)
    }

    private companion object {
        const val DEFAULT_APPROVE_COMMENT = "Одобрено администратором"
    }
}
