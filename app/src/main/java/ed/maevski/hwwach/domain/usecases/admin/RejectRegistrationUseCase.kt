package ed.maevski.hwwach.domain.usecases.admin

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IAdminRepository
import javax.inject.Inject

class RejectRegistrationUseCase @Inject constructor(
    private val adminRepository: IAdminRepository
) {
    suspend operator fun invoke(id: String, reason: String): DomainResult<Unit> {
        val trimmedReason = reason.trim()
        if (trimmedReason.isEmpty()) {
            return DomainResult.ValidationError("Укажите причину отклонения")
        }
        return adminRepository.rejectRegistration(id, trimmedReason)
    }
}
