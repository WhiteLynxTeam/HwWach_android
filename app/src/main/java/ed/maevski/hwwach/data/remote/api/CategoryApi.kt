package ed.maevski.hwwach.data.remote.api

import ed.maevski.hwwach.data.remote.model.category.CategoryDto
import ed.maevski.hwwach.data.remote.model.category.CategorySyncResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CategoryApi {
    @GET("/categories/search")
    suspend fun searchCategories(
        @Query("q") query: String
    ): Response<List<CategoryDto>>

    @GET("/categories/sync")
    suspend fun syncCategories(
        @Query("since") since: String? = null
    ): Response<CategorySyncResponseDto>
}
