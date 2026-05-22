package com.whitelynxteam.hwwach.domain.usecases.user

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IUserRepository
import com.whitelynxteam.hwwach.domain.models.RegStatus

class CheckRegistrationUseCase(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(): DomainResult<RegStatus> {
        return userRepository.getRegStatus()
    }
}