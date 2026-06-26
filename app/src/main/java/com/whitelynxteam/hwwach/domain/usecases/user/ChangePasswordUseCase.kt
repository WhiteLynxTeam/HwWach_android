package com.whitelynxteam.hwwach.domain.usecases.user

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IUserRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(oldPass: String, newPass: String): DomainResult<Unit> {

        return userRepository.changePassword(oldPass, newPass)
    }
}
