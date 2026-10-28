package ed.maevski.hwwach.data.remote.model.asset

import com.google.gson.annotations.SerializedName

data class AssetsResponseDto(
    @SerializedName("assets") val assets: List<AssetDto>,
    @SerializedName("person_id") val personId: String? = null
)
