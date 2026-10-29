package ed.maevski.hwwach.domain.usecases.admin

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IAdminRepository
import ed.maevski.hwwach.domain.models.PasswordResetResult
import javax.inject.Inject

class ApprovePasswordResetUseCase @Inject constructor(
    private val adminRepository: IAdminRepository
) {
    // Пустой пароль — временный пароль сгенерирует сервер
    suspend operator fun invoke(id: String, password: String): DomainResult<PasswordResetResult> {
        val trimmed = password.trim()
        if (trimmed.isNotEmpty() && trimmed.length < 4) {
            return DomainResult.ValidationError("Пароль — не меньше 4 символов")
        }
        return adminRepository.approvePasswordReset(id, trimmed.ifEmpty { null })
    }
}
