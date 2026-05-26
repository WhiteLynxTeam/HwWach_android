package com.whitelynxteam.hwwach.domain.exception

open class SyncException(
    message: String,
    val entityId: String? = null,
    val entityType: SyncEntityType? = null
) : DomainException(message)

enum class SyncEntityType {
    PHOTO, ASSET, CATEGORY
}