package com.whitelynxteam.hwwach.domain.usecases.user

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IUserRepository
import javax.inject.Inject

class ChangeTempPasswordUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(login: String, oldPass: String, newPass: String): DomainResult<Unit> {
        return userRepository.changeTempPassword(login, oldPass, newPass)
    }
}
