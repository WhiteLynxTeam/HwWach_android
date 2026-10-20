package ed.maevski.hwwach.data.remote.model.admin

import com.google.gson.annotations.SerializedName

data class RejectRegistrationRequestDto(
    @SerializedName("reason")
    val reason: String
)
