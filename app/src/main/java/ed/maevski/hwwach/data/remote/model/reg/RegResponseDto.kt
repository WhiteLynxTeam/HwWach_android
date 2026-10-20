package ed.maevski.hwwach.data.remote.model.reg

import com.google.gson.annotations.SerializedName

data class RegResponseDto(
    @SerializedName("id")
    val uuid: String,
    @SerializedName("login")
    val login: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("middleName")
    val middleName: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("position")
    val position: String,
    @SerializedName("status")
    val status: RegStatusEnumDto,
    @SerializedName("approvedByUser")
    val approvedByUser: String? = null,
    @SerializedName("adminComment")
    val adminComment: String? = null,
    @SerializedName("processedAt")
    val processedAt: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
