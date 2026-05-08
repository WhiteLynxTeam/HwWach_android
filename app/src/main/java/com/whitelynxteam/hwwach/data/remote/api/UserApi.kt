package com.whitelynxteam.hwwach.data.remote.api

import com.whitelynxteam.hwwach.data.remote.model.auth.AuthResponseDto
import com.whitelynxteam.hwwach.data.remote.model.auth.AuthUserRequest
import com.whitelynxteam.hwwach.data.remote.model.auth.RefreshTokenRequest
import com.whitelynxteam.hwwach.data.remote.model.auth.RefreshTokenResponse
import com.whitelynxteam.hwwach.data.remote.model.reg.RegResponseDto
import com.whitelynxteam.hwwach.data.remote.model.reg.RegStatusResponseDto
import com.whitelynxteam.hwwach.data.remote.model.reg.RegUserRequest
import com.whitelynxteam.hwwach.data.remote.model.user.UserResponseDto
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
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>
}