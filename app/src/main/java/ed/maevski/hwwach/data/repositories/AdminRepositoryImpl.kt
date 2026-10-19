package ed.maevski.hwwach.data.repositories

import ed.maevski.hwwach.data.mappers.RegistrationRequestDtoToDomainMapper
import ed.maevski.hwwach.data.mappers.ResponseErrorMapper
import ed.maevski.hwwach.data.remote.api.AdminApi
import ed.maevski.hwwach.data.remote.model.admin.ApproveRegistrationRequestDto
import ed.maevski.hwwach.data.remote.model.admin.RejectRegistrationRequestDto
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IAdminRepository
import ed.maevski.hwwach.domain.models.RegistrationRequest
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class AdminRepositoryImpl @Inject constructor(
    @Named("user_token") private val adminApi: AdminApi,
    private val registrationRequestDtoToDomainMapper: RegistrationRequestDtoToDomainMapper,
    private val responseErrorMapper: ResponseErrorMapper,
) : IAdminRepository {

    override suspend fun getPendingRegistrations(): DomainResult<List<RegistrationRequest>> {
        return try {
            val response = adminApi.getPendingRegistrations()
            if (response.isSuccessful) {
                val registrations = response.body().orEmpty()
                    .map { registrationRequestDtoToDomainMapper.map(it) }
                    .sortedByDescending { it.createdAt }
                DomainResult.Success(registrations)
            } else {
                responseErrorMapper.map(response)
            }
        } catch (e: IOException) {
            DomainResult.NetworkError(e.message ?: "Network error")
        }
    }

    override suspend fun getRegistration(id: String): DomainResult<RegistrationRequest> {
        return try {
            val response = adminApi.getRegistration(id)
            if (response.isSuccessful) {
                val registration = response.body()
                    ?: return DomainResult.ValidationError("Заявка не найдена")
                DomainResult.Success(registrationRequestDtoToDomainMapper.map(registration))
            } else {
                responseErrorMapper.map(response)
            }
        } catch (e: IOException) {
            DomainResult.NetworkError(e.message ?: "Network error")
        }
    }

    override suspend fun approveRegistration(id: String, comment: String): DomainResult<Unit> {
        return try {
            val response = adminApi.approveRegistration(id, ApproveRegistrationRequestDto(comment))
            if (response.isSuccessful) {
                DomainResult.Success(Unit)
            } else {
                responseErrorMapper.map(response)
            }
        } catch (e: IOException) {
            DomainResult.NetworkError(e.message ?: "Network error")
        }
    }

    override suspend fun rejectRegistration(id: String, reason: String): DomainResult<Unit> {
        return try {
            val response = adminApi.rejectRegistration(id, RejectRegistrationRequestDto(reason))
            if (response.isSuccessful) {
                DomainResult.Success(Unit)
            } else {
                responseErrorMapper.map(response)
            }
        } catch (e: IOException) {
            DomainResult.NetworkError(e.message ?: "Network error")
        }
    }
}
