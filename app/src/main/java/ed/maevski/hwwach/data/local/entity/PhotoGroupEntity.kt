package ed.maevski.hwwach.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Группа снимков: живёт только на телефоне. Место и человек — подсказка для активов из группы
@Entity(tableName = "photo_groups")
data class PhotoGroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val note: String? = null,
    val locationKind: String = "unset",
    val locationId: String? = null,
    val locationPath: String? = null,
    val personId: String? = null,
    val personName: String? = null,
    val personIsMol: Boolean = false,
    val createdAt: Long,
)
