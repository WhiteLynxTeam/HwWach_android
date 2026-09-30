package ed.maevski.hwwach.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ed.maevski.hwwach.domain.models.Category

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["normalizedName"]),
        Index(value = ["level"]),
        Index(value = ["status"])
    ]
)
data class CategoryEntity(
    @PrimaryKey val uuid: String,
    val name: String,
    val normalizedName: String,
    val level: Int,
    val status: String,
    val usageCount: Int = 0,
    val createdBy: String? = null,
    val adminComment: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
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

    companion object {
        fun fromDomain(category: Category): CategoryEntity = CategoryEntity(
            uuid = category.uuid,
            name = category.name,
            normalizedName = category.name.trim().lowercase(),
            level = category.level,
            status = category.status,
            usageCount = category.usageCount,
            createdBy = category.createdBy,
            adminComment = category.adminComment
        )
    }
}
