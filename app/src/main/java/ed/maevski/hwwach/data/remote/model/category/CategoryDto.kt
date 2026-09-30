package ed.maevski.hwwach.data.remote.model.category

import com.google.gson.annotations.SerializedName
import ed.maevski.hwwach.domain.models.Category

data class CategoryDto(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("name") val name: String,
    @SerializedName("level") val level: Int,
    @SerializedName("status") val status: String,
    @SerializedName("usage_count") val usageCount: Int = 0,
    @SerializedName("created_by") val createdBy: String? = null,
    @SerializedName("admin_comment") val adminComment: String? = null
) {
    fun toDomain(): Category = Category(
        uuid = uuid,
        name = name,
        level = level,
        status = status,
        usageCount = usageCount,
        createdBy = createdBy,
        adminComment = adminComment
    )
}

data class CategorySyncResponseDto(
    @SerializedName("categories") val categories: List<CategoryDto>,
    @SerializedName("deleted_ids") val deletedIds: List<String>,
    @SerializedName("synced_at") val syncedAt: String
)
