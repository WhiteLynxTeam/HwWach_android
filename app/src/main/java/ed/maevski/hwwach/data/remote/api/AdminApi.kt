package ed.maevski.hwwach.data.remote.api

import ed.maevski.hwwach.data.remote.model.admin.ApprovePasswordResetDto
import ed.maevski.hwwach.data.remote.model.admin.ApprovePasswordResetResponseDto
import ed.maevski.hwwach.data.remote.model.admin.ApproveRegistrationRequestDto
import ed.maevski.hwwach.data.remote.model.admin.PasswordResetRequestDto
import ed.maevski.hwwach.data.remote.model.admin.RegistrationRequestDto
import ed.maevski.hwwach.data.remote.model.admin.RejectRegistrationRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Админские запросы к TS-сервису (:3033), требуют токен с ролью ADMIN */
interface AdminApi {
    @GET("/registrations")
    suspend fun getPendingRegistrations(): Response<List<RegistrationRequestDto>>

    @GET("/registrations/{id}")
    suspend fun getRegistration(
        @Path("id") id: String
    ): Response<RegistrationRequestDto>

    @POST("/registrations/{id}/approve")
    suspend fun approveRegistration(
        @Path("id") id: String,
        @Body request: ApproveRegistrationRequestDto
    ): Response<Unit>

    @POST("/registrations/{id}/reject")
    suspend fun rejectRegistration(
        @Path("id") id: String,
        @Body request: RejectRegistrationRequestDto
    ): Response<Unit>

    // Заявки «Забыли пароль?»: одобрение задаёт временный пароль, при входе его потребуют сменить
    @GET("/reset-pass")
    suspend fun getPasswordResets(): Response<List<PasswordResetRequestDto>>

    @POST("/reset-pass/{id}/approve")
    suspend fun approvePasswordReset(
        @Path("id") id: String,
        @Body request: ApprovePasswordResetDto
    ): Response<ApprovePasswordResetResponseDto>

    @POST("/reset-pass/{id}/reject")
    suspend fun rejectPasswordReset(
        @Path("id") id: String
    ): Response<Unit>
}
