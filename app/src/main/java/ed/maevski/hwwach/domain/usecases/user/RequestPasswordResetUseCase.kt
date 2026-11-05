package ed.maevski.hwwach.domain.usecases.user

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IUserRepository
import javax.inject.Inject

class RequestPasswordResetUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(login: String, organizationNumber: String? = null): DomainResult<Unit> {
        return userRepository.requestPasswordReset(login, organizationNumber)
    }
}
