package com.whitelynxteam.hwwach.domain.usecases

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IUserRepository
import com.whitelynxteam.hwwach.domain.models.User
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(): DomainResult<User> {
        return userRepository.getUserInfo()
    }
}