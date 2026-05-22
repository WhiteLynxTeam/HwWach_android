package com.whitelynxteam.hwwach.domain.usecases.user

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IUserRepository
import javax.inject.Inject

import com.whitelynxteam.hwwach.domain.models.User

class GetUserInfoUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(id: String): DomainResult<User> {
        return userRepository.getUserInfo(id)
    }
}