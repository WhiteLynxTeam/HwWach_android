package ed.maevski.hwwach.data.remote.api

import ed.maevski.hwwach.data.remote.model.asset.AssetDto
import ed.maevski.hwwach.data.remote.model.asset.AssetRequestDto
import ed.maevski.hwwach.data.remote.model.asset.AssetsResponseDto
import ed.maevski.hwwach.data.remote.model.asset.UpdateAssetRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AssetApi {
    @GET("/assets")
    suspend fun getAssets(): Response<AssetsResponseDto>

    @POST("/assets")
    suspend fun createAsset(@Body request: AssetRequestDto): Response<AssetDto>

    @PUT("/assets/{id}")
    suspend fun updateAsset(@Path("id") uuid: String, @Body request: UpdateAssetRequestDto): Response<AssetDto>
}
