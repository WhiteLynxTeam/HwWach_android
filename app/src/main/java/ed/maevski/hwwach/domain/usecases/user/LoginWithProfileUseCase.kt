package ed.maevski.hwwach.domain.usecases.user

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IOrgRepository
import ed.maevski.hwwach.domain.irepositories.IUserProfileRepository
import ed.maevski.hwwach.domain.mapSuccess
import ed.maevski.hwwach.domain.models.User
import javax.inject.Inject

class LoginWithProfileUseCase @Inject constructor(
    private val userProfileRepository: IUserProfileRepository,
    private val authApiUseCase: AuthApiUseCase,
    private val orgRepository: IOrgRepository,
) {
    /**
     * organizationNumber (9.2) — где искать логин; null — вход по почте или по логину, если он один во всей
     * системе. Номер, с которым вошли, запоминается вместе с названием организации: дальше вводить не нужно.
     */
    suspend operator fun invoke(login: String, password: String, organizationNumber: String? = null): DomainResult<Unit> {
        return authApiUseCase(
            User(
                username = login,
                password = password,
                organizationNumber = organizationNumber,
            )
        ).mapSuccess { user ->
            // Сохраняем профиль пользователя в локальное хранилище
            userProfileRepository.saveUserProfile(user)
            orgRepository.saveLastOrganization(organizationNumber, user.organizationName)
        }
    }
}
