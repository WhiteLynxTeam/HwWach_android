package ed.maevski.hwwach.data.remote.api

import ed.maevski.hwwach.data.remote.model.photo.CompleteUploadRequest
import ed.maevski.hwwach.data.remote.model.photo.PhotoDto
import ed.maevski.hwwach.data.remote.model.photo.PhotosResponseDto
import ed.maevski.hwwach.data.remote.model.photo.UploadUrlRequest
import ed.maevski.hwwach.data.remote.model.photo.UploadUrlResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PhotosApi {
    @GET("/photos/")
    suspend fun photos(): Response<PhotosResponseDto>

    @POST("/photos/upload-url")
    suspend fun getUploadUrl(@Body request: UploadUrlRequest): Response<UploadUrlResponse>

    @POST("/photos/complete-upload")
    suspend fun completeUpload(@Body request: CompleteUploadRequest): Response<PhotoDto>
}
