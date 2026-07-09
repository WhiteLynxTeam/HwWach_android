package ed.maevski.hwwach.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "asset_photo_cross_ref",
    primaryKeys = ["assetClientId", "photoClientId"],
    foreignKeys = [
        ForeignKey(
            entity = AssetEntity::class,
            parentColumns = ["clientId"],
            childColumns = ["assetClientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["clientId"],
            childColumns = ["photoClientId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["assetClientId"]),
        Index(value = ["photoClientId"])
    ]
)
data class AssetPhotoCrossRef(
    val assetClientId: String,
    val photoClientId: String,
    val status: String = "PENDING",
)
