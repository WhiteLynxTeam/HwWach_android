package ed.maevski.hwwach.domain.usecases.user

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IUserRepository
import ed.maevski.hwwach.domain.models.User
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(id: String): DomainResult<User> {
        return userRepository.getUserInfo(id)
    }
}