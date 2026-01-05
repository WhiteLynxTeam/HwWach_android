package com.whitelynxteam.hwwach.data.remote.api

import com.whitelynxteam.hwwach.data.remote.model.auth.AuthResponseDto
import com.whitelynxteam.hwwach.data.remote.model.auth.AuthUserRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {
    @POST("/auth/login/")
    suspend fun auth(
        @Body authUserRequest: AuthUserRequest
    ): Response<AuthResponseDto>
}