package com.whitelynxteam.hwwach.domain.usecases

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IUserRepository
import com.whitelynxteam.hwwach.domain.models.RegStatus
import com.whitelynxteam.hwwach.domain.models.RegStatusEnum

class CheckRegistrationUseCase(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(): DomainResult<RegStatus> {
        return userRepository.getRegStatus()
    }
}