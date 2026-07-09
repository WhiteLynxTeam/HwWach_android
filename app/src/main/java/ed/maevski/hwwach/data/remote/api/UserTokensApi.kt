package ed.maevski.hwwach.data.remote.api

import ed.maevski.hwwach.data.remote.model.user.ChangePasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** Запросы к TS-сервису (:3033), требующие авторизации с токеном */
interface UserTokensApi {
    @POST("/users/change-password/")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<Unit>
}
