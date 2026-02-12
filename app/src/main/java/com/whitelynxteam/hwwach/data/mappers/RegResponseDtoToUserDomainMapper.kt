package com.whitelynxteam.hwwach.data.mappers

import com.whitelynxteam.hwwach.data.remote.model.reg.RegResponseDto
import com.whitelynxteam.hwwach.domain.models.User
import javax.inject.Inject

class RegResponseDtoToUserDomainMapper @Inject constructor() {
    fun map(regResponseDto: RegResponseDto): User {
        return User(
            username = regResponseDto.login,
            firstName = regResponseDto.firstName,
            lastName = regResponseDto.lastName,
            middleName = regResponseDto.middleName,
            phone = regResponseDto.phone,
            position = regResponseDto.position,
        )
    }
}