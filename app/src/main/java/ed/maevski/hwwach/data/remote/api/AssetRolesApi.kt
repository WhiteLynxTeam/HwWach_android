package ed.maevski.hwwach.data.remote.api

import ed.maevski.hwwach.data.remote.model.asset.AckRequest
import ed.maevski.hwwach.data.remote.model.asset.AssetDetailDto
import ed.maevski.hwwach.data.remote.model.asset.AssignPersonRequest
import ed.maevski.hwwach.data.remote.model.asset.AssignmentDto
import ed.maevski.hwwach.data.remote.model.asset.CreateRepairRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Go-сервис: ответственные за актив, «Мне назначено», заявка на ремонт */
interface AssetRolesApi {
    @GET("/assets/{uuid}")
    suspend fun getAsset(@Path("uuid") uuid: String): Response<AssetDetailDto>

    @GET("/me/assignments")
    suspend fun myAssignments(): Response<List<AssignmentDto>>

    @POST("/asset-roles/{id}/ack")
    suspend fun ack(@Path("id") roleId: String, @Body request: AckRequest): Response<AssignmentDto>

    @POST("/assets/{uuid}/mol")
    suspend fun assignMol(@Path("uuid") uuid: String, @Body request: AssignPersonRequest): Response<Unit>

    @POST("/assets/{uuid}/users")
    suspend fun addUser(@Path("uuid") uuid: String, @Body request: AssignPersonRequest): Response<Unit>

    @POST("/requests")
    suspend fun createRepairRequest(@Body request: CreateRepairRequestDto): Response<Unit>
}
