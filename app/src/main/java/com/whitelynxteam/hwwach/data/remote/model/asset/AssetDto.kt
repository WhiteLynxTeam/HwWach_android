package com.whitelynxteam.hwwach.data.remote.model.asset

import com.google.gson.annotations.SerializedName

data class AssetDto(
    @SerializedName("admin_comment") val adminComment: String?,
    @SerializedName("asset_status") val assetStatus: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("client_id") val clientId: String,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("inventory_num") val inventoryNum: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("user_uuid") val userUuid: String?,
    @SerializedName("uuid") val uuid: String,
    @SerializedName("verified_at") val verifiedAt: String?
)
