package ed.maevski.hwwach.data.remote.model.reg

import com.google.gson.annotations.SerializedName

enum class RegStatusEnumDto{
    @SerializedName("PENDING")
    PENDING,
    @SerializedName("APPROVED")
    APPROVED,
    @SerializedName("REJECTED")
    REJECTED,
    @SerializedName("EXPIRED")
    EXPIRED,
}
