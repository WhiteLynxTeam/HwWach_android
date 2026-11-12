package ed.maevski.hwwach.data.remote.api

import ed.maevski.hwwach.data.remote.model.org.CreatePersonRequest
import ed.maevski.hwwach.data.remote.model.org.PersonBriefDto
import ed.maevski.hwwach.data.remote.model.user.UserResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** TS-сервис с токеном: люди организации, профиль, просьба «я МОЛ» */
interface OrgTokensApi {
    @GET("/persons")
    suspend fun getPersons(): Response<List<PersonBriefDto>>

    /** Человек без учётной записи: числится в организации, войти сможет позже (заявка → привязка к нему) */
    @POST("/persons")
    suspend fun createPerson(@Body body: CreatePersonRequest): Response<PersonBriefDto>

    @GET("/auth/me")
    suspend fun me(): Response<UserResponseDto>

    @POST("/persons/me/mol-request")
    suspend fun requestMol(): Response<Unit>
}
