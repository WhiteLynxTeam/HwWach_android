package ed.maevski.hwwach.domain.usecases.user

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IUserRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(oldPass: String, newPass: String): DomainResult<Unit> {

        return userRepository.changePassword(oldPass, newPass)
    }
}
