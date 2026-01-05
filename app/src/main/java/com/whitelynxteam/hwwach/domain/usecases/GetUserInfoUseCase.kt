package com.whitelynxteam.hwwach.domain.usecases

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IUserRepository
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(): DomainResult<String> {
//        userRepository.getUserInfo()
        return DomainResult.Success("mock")
    }
}