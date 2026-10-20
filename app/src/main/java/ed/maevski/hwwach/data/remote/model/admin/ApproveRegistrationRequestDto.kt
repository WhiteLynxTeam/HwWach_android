package ed.maevski.hwwach.data.remote.model.admin

import com.google.gson.annotations.SerializedName

data class ApproveRegistrationRequestDto(
    @SerializedName("comment")
    val comment: String
)
