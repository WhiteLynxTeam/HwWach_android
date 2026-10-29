package ed.maevski.hwwach.domain.usecases.admin

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IAdminRepository
import javax.inject.Inject

class RejectPasswordResetUseCase @Inject constructor(
    private val adminRepository: IAdminRepository
) {
    suspend operator fun invoke(id: String): DomainResult<Unit> {
        return adminRepository.rejectPasswordReset(id)
    }
}
