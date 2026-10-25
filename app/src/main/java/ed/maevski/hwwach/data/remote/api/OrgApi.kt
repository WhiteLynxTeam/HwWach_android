package ed.maevski.hwwach.data.remote.api

import ed.maevski.hwwach.data.remote.model.auth.AuthResponseDto
import ed.maevski.hwwach.data.remote.model.org.CompleteOrgRegistrationRequest
import ed.maevski.hwwach.data.remote.model.org.InviteResolveDto
import ed.maevski.hwwach.data.remote.model.org.OrgSearchItemDto
import ed.maevski.hwwach.data.remote.model.org.SendCodeRequest
import ed.maevski.hwwach.data.remote.model.org.VerifyCodeRequest
import ed.maevski.hwwach.data.remote.model.org.VerifyCodeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** TS-сервис без токена: поиск организации, приглашения, регистрация организации */
interface OrgApi {
    @GET("/organizations/search")
    suspend fun search(@Query("q") query: String): Response<List<OrgSearchItemDto>>

    @GET("/invites/resolve/{code}")
    suspend fun resolveInvite(@Path("code") code: String): Response<InviteResolveDto>

    @POST("/org-registration/send-code")
    suspend fun sendCode(@Body request: SendCodeRequest): Response<Unit>

    @POST("/org-registration/verify-code")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): Response<VerifyCodeResponse>

    @POST("/org-registration/complete")
    suspend fun complete(@Body request: CompleteOrgRegistrationRequest): Response<AuthResponseDto>
}
