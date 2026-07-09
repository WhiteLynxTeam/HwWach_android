package ed.maevski.hwwach.domain.usecases.user

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IUserProfileRepository
import ed.maevski.hwwach.domain.mapSuccess
import ed.maevski.hwwach.domain.models.User
import javax.inject.Inject

class LoginWithProfileUseCase @Inject constructor(
    private val userProfileRepository: IUserProfileRepository,
    private val authApiUseCase: AuthApiUseCase
) {
    suspend operator fun invoke(login: String, password: String): DomainResult<Unit> {
        return authApiUseCase(
            User(
                username = login,
                password = password,
            )
        ).mapSuccess { user ->
            // Сохраняем профиль пользователя в локальное хранилище
            userProfileRepository.saveUserProfile(user)
        }
    }
}