package ed.maevski.hwwach.data.mappers

import ed.maevski.hwwach.data.remote.model.reg.RegStatusResponseDto
import ed.maevski.hwwach.domain.models.RegStatus
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