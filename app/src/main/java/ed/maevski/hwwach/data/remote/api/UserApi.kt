package ed.maevski.hwwach.data.remote.api

import ed.maevski.hwwach.data.remote.model.auth.AuthResponseDto
import ed.maevski.hwwach.data.remote.model.auth.AuthUserRequest
import ed.maevski.hwwach.data.remote.model.auth.ChangeTempPasswordRequest
import ed.maevski.hwwach.data.remote.model.auth.RefreshTokenRequest
import ed.maevski.hwwach.data.remote.model.auth.RefreshTokenResponse
import ed.maevski.hwwach.data.remote.model.reg.RegResponseDto
import ed.maevski.hwwach.data.remote.model.reg.RegStatusResponseDto
import ed.maevski.hwwach.data.remote.model.reg.RegUserRequest
import ed.maevski.hwwach.data.remote.model.user.UserResponseDto
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApi {
    @POST("/auth/login/")
    suspend fun auth(
        @Body authUserRequest: AuthUserRequest
    ): Response<AuthResponseDto>

    @POST("/auth/register/")
    suspend fun reg(
        @Body regUserRequest: RegUserRequest
    ): Response<RegResponseDto>

    @GET("/registration-status/{id}")
    suspend fun statusReg(
        @Path("id") uuid: String
    ): Response<RegStatusResponseDto>

    @GET("/users/{id}")
    suspend fun getUserInfo(
        @Path("id") id: String
    ): Response<UserResponseDto>

    @POST("/auth/refresh/")
    fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Call<RefreshTokenResponse>

    @POST("/auth/change-temp-password/")
    suspend fun changeTempPassword(
        @Body request: ChangeTempPasswordRequest
    ): Response<Unit>
}