package com.whitelynxteam.hwwach.data.mappers

import com.whitelynxteam.hwwach.data.remote.model.reg.RegStatusResponseDto
import com.whitelynxteam.hwwach.domain.models.RegStatus
import javax.inject.Inject

class RegStatusResponseDtoToRegStatusDomainMapper @Inject constructor(
    private val regStatusEnumDtoToDomainMapper: RegStatusEnumDtoToDomainMapper,
) {
    fun map(regStatusResponseDto: RegStatusResponseDto): RegStatus {
        return RegStatus(
            uuid = regStatusResponseDto.uuid,
            login = regStatusResponseDto.login,
            status = regStatusEnumDtoToDomainMapper.map(regStatusResponseDto.status),
        )
    }
}