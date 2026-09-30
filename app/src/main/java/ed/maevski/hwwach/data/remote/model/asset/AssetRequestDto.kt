package ed.maevski.hwwach.data.remote.model.asset

import com.google.gson.annotations.SerializedName

data class AssetRequestDto(
    @SerializedName("client_id") val clientId: String,
    @SerializedName("inventory_num") val inventoryNum: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("asset_status") val assetStatus: String,
    @SerializedName("category") val category: String? = null,
    @SerializedName("category_uuid") val categoryUuid: String? = null,
    @SerializedName("category_text") val categoryText: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("photo_client_ids") val photoClientIds: List<String>
)
