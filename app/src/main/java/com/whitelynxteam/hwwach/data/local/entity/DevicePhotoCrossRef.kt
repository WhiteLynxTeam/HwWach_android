package com.whitelynxteam.hwwach.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "device_photo_cross_ref",
    primaryKeys = ["deviceClientId", "photoClientId"],
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["clientId"],
            childColumns = ["deviceClientId"],
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
        Index(value = ["deviceClientId"]),
        Index(value = ["photoClientId"])
    ]
)
data class DevicePhotoCrossRef(
    val deviceClientId: String,
    val photoClientId: String,
    val status: String = "PENDING",
)
