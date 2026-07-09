package ed.maevski.hwwach.data.mappers

import ed.maevski.hwwach.data.remote.model.reg.RegStatusEnumDto
import ed.maevski.hwwach.domain.models.RegStatusEnum
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