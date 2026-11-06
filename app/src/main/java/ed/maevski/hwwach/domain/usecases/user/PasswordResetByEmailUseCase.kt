package ed.maevski.hwwach.domain.usecases.user

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IOrgRepository
import ed.maevski.hwwach.domain.irepositories.IUserRepository
import ed.maevski.hwwach.domain.models.EmailPasswordResetResult
import javax.inject.Inject

/** Сброс пароля кодом на почту (8.2): без админа, в том числе для самого админа. */
class PasswordResetByEmailUseCase @Inject constructor(
    private val userRepository: IUserRepository,
    private val orgRepository: IOrgRepository,
) {
    suspend fun sendCode(email: String): DomainResult<Unit> =
        userRepository.sendPasswordResetCode(email)

    // Пароль сменён — запоминаем организацию, чтобы на входе был подставлен её номер
    suspend fun confirm(email: String, code: String, newPassword: String): DomainResult<EmailPasswordResetResult> {
        val result = userRepository.confirmPasswordReset(email, code, newPassword)
        if (result is DomainResult.Success && result.data.organizationNumber != null) {
            orgRepository.saveLastOrganization(result.data.organizationNumber, result.data.organizationName)
        }
        return result
    }
}
