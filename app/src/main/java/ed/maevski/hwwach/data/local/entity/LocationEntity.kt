package ed.maevski.hwwach.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Справочник мест организации (копия с сервера + заведённые на телефоне, synced = false)
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val id: String,
    val parentId: String?,
    val name: String,
    val kind: String,   // LocationKind.server
    val path: String,
    val synced: Boolean,
)
