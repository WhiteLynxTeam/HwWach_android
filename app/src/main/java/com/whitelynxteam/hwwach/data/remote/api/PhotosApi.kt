package com.whitelynxteam.hwwach.data.remote.api

import com.whitelynxteam.hwwach.data.remote.model.photo.PhotosResponseDto
import retrofit2.Response
import retrofit2.http.GET

interface PhotosApi {
    @GET("/photos/")
    suspend fun photos(): Response<PhotosResponseDto>

}