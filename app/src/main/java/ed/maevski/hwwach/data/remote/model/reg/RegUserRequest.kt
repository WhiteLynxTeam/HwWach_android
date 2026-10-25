package ed.maevski.hwwach.data.remote.model.reg

import com.google.gson.annotations.SerializedName

data class RegUserRequest(
    @SerializedName("login")
    val login: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("phone")
    val phone: String? = null,
    // по приглашению из карточки ФИО уже есть на сервере — можно не передавать
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("middleName")
    val middleName: String? = null,
    @SerializedName("position")
    val position: String? = null,
    // организация, найденная поиском, или код приглашения — одно из двух
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("inviteCode")
    val inviteCode: String? = null,
)
