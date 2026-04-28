package com.whitelynxteam.hwwach.data.remote.api

import com.whitelynxteam.hwwach.data.remote.model.photo.CompleteUploadRequest
import com.whitelynxteam.hwwach.data.remote.model.photo.PhotoDto
import com.whitelynxteam.hwwach.data.remote.model.photo.PhotosResponseDto
import com.whitelynxteam.hwwach.data.remote.model.photo.UploadUrlRequest
import com.whitelynxteam.hwwach.data.remote.model.photo.UploadUrlResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

interface PhotosApi {
    @GET("/photos/")
    suspend fun photos(): Response<PhotosResponseDto>

    @POST("/photos/upload-url")
    suspend fun getUploadUrl(@Body request: UploadUrlRequest): Response<UploadUrlResponse>

    @PUT
    @Headers("Content-Type: application/octet-stream")
    suspend fun uploadPhotoToUrl(
        @Url uploadUrl: String,
        @Body file: RequestBody,
    ): Response<Void>

    @POST("/photos/complete-upload")
    suspend fun completeUpload(@Body request: CompleteUploadRequest): Response<PhotoDto>
}
