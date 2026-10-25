package ed.maevski.hwwach.data.remote.model.asset

import com.google.gson.annotations.SerializedName

data class AssetDto(
    @SerializedName("admin_comment") val adminComment: String?,
    // учётный статус: in_use / in_stock / repair / lost / decommissioned
    @SerializedName("accounting_status") val accountingStatus: String? = null,
    // техническое состояние: ok / needs_attention / faulty / unusable
    @SerializedName("condition") val condition: String? = null,
    @SerializedName("moderation_status") val moderationStatus: String? = null,
    @SerializedName("category") val category: String?,
    @SerializedName("category_uuid") val categoryUuid: String? = null,
    @SerializedName("client_id") val clientId: String,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("inventory_num") val inventoryNum: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("created_by_person_id") val createdByPersonId: String? = null,
    @SerializedName("uuid") val uuid: String,
    @SerializedName("verified_at") val verifiedAt: String?
)
