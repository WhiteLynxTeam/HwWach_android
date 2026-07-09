package ed.maevski.hwwach.domain.usecases.user

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IUserProfileRepository
import ed.maevski.hwwach.domain.irepositories.IUserRepository
import ed.maevski.hwwach.domain.mapSuccess
import ed.maevski.hwwach.domain.models.User

class RegApiUseCase(
    private val userRepository: IUserRepository,
    private val userProfileRepository: IUserProfileRepository,
) {

    // Почему возвращаем DomainResult<Unit>, а не Boolean. Ведь данные о юзере мы выдрали из
    // success и во вьюмодель мы данные при успешном результате не отправляем.
    // Но зато мы отправляем результат ошибки с данными - кодом или сообщением,
    // чтобы обработать и выдать сообщение согласно ошибки.

    suspend operator fun invoke(user: User): DomainResult<Unit> {
        return userRepository.reg(user).mapSuccess { user ->
            userProfileRepository.saveUserProfile(user)
            // Unit - Оставим, чтобы было видно что возвращаем Unit. А так
            // результат возврата предыдущей функции Unit
        }
    }
}