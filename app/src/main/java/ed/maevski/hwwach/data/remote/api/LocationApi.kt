package ed.maevski.hwwach.data.remote.api

import ed.maevski.hwwach.data.remote.model.location.CreateLocationRequest
import ed.maevski.hwwach.data.remote.model.location.LocationDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LocationApi {
    @GET("/locations")
    suspend fun getLocations(): Response<List<LocationDto>>

    @POST("/locations")
    suspend fun createLocation(@Body request: CreateLocationRequest): Response<LocationDto>
}
