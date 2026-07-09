package ed.maevski.hwwach.domain.usecases.user

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IUserRepository
import ed.maevski.hwwach.domain.models.RegStatus

class CheckRegistrationUseCase(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(): DomainResult<RegStatus> {
        return userRepository.getRegStatus()
    }
}