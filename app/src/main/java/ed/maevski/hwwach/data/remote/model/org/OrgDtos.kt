package ed.maevski.hwwach.data.remote.model.org

import com.google.gson.annotations.SerializedName

// Поиск организации при регистрации: только название
data class OrgSearchItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("shortName") val shortName: String? = null,
)

data class InvitePersonDto(
    @SerializedName("lastName") val lastName: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("middleName") val middleName: String? = null,
    @SerializedName("position") val position: String? = null,
)

data class InviteResolveDto(
    @SerializedName("organizationId") val organizationId: String,
    @SerializedName("organizationName") val organizationName: String,
    @SerializedName("person") val person: InvitePersonDto? = null,
)

data class SendCodeRequest(
    @SerializedName("email") val email: String,
)

data class VerifyCodeRequest(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String,
)

data class VerifyCodeResponse(
    @SerializedName("registration_token") val registrationToken: String,
)

// Шаг 3 регистрации организации: организация и её первый администратор
data class CompleteOrgRegistrationRequest(
    @SerializedName("registration_token") val registrationToken: String,
    @SerializedName("name") val name: String,
    @SerializedName("shortName") val shortName: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("directorName") val directorName: String? = null,
    @SerializedName("ownerName") val ownerName: String? = null,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("middleName") val middleName: String? = null,
    @SerializedName("position") val position: String? = null,
    @SerializedName("password") val password: String,
)

// Человек организации; участникам — без телефонов
data class PersonBriefDto(
    @SerializedName("id") val id: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("position") val position: String? = null,
    @SerializedName("isMol") val isMol: Boolean = false,
    @SerializedName("isActive") val isActive: Boolean = true,
)
