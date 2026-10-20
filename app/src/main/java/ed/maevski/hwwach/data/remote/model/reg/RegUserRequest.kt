package ed.maevski.hwwach.data.remote.model.reg

import com.google.gson.annotations.SerializedName

data class RegUserRequest(
    @SerializedName("login")
    val login: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("middleName")
    val middleName: String? = null,
    @SerializedName("position")
    val position: String? = null,
)
