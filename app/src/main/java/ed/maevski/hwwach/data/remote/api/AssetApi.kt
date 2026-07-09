package ed.maevski.hwwach.data.remote.api

import ed.maevski.hwwach.data.remote.model.asset.AssetDto
import ed.maevski.hwwach.data.remote.model.asset.AssetRequestDto
import ed.maevski.hwwach.data.remote.model.asset.AssetsResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AssetApi {
    @GET("/assets")
    suspend fun getAssets(): Response<AssetsResponseDto>

    @POST("/assets")
    suspend fun createAsset(@Body request: AssetRequestDto): Response<AssetDto>
}
