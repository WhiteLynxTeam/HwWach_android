package ed.maevski.hwwach.data.remote.model.asset

import com.google.gson.annotations.SerializedName

data class AssetRequestDto(
    @SerializedName("client_id") val clientId: String,
    @SerializedName("inventory_num") val inventoryNum: String?,
    @SerializedName("description") val description: String?,
    // in_use / in_stock; МОЛ не передаём — сервер запишет актив за создателем, если он МОЛ
    @SerializedName("accounting_status") val accountingStatus: String,
    @SerializedName("category") val category: String? = null,
    @SerializedName("category_uuid") val categoryUuid: String? = null,
    @SerializedName("category_text") val categoryText: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("photo_client_ids") val photoClientIds: List<String>,
    // МОЛ: null — по умолчанию (автор, если он МОЛ), "" — без МОЛ, id — этот человек
    @SerializedName("mol_person_id") val molPersonId: String? = null,
    @SerializedName("location_kind") val locationKind: String? = null,
    // место от корня к листу: сервер найдёт готовые узлы по id или заведёт недостающие
    @SerializedName("location") val location: List<LocationNodeDto>? = null,
)

data class LocationNodeDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("kind") val kind: String,
)

// Правка карточки: null — поле не меняется (Gson не пишет null)
data class UpdateAssetRequestDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("inventory_num") val inventoryNum: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("category_uuid") val categoryUuid: String? = null,
    @SerializedName("category_text") val categoryText: String? = null,
    @SerializedName("location_kind") val locationKind: String? = null,
    @SerializedName("location") val location: List<LocationNodeDto>? = null,
    @SerializedName("photo_client_ids") val photoClientIds: List<String>? = null,
)
