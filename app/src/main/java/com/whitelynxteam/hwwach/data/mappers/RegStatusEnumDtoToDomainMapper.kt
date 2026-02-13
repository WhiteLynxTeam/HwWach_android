package com.whitelynxteam.hwwach.data.mappers

import com.whitelynxteam.hwwach.data.remote.model.reg.RegStatusEnumDto
import com.whitelynxteam.hwwach.domain.models.RegStatusEnum
import javax.inject.Inject

class RegStatusEnumDtoToDomainMapper @Inject constructor() {
    fun map(statusEnumDto: RegStatusEnumDto): RegStatusEnum {
        return when (statusEnumDto) {
            RegStatusEnumDto.PENDING -> RegStatusEnum.PENDING
            RegStatusEnumDto.APPROVED -> RegStatusEnum.APPROVED
            RegStatusEnumDto.REJECTED -> RegStatusEnum.REJECTED
        }
    }
}