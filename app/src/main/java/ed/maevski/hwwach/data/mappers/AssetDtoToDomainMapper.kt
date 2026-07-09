package ed.maevski.hwwach.data.mappers

import ed.maevski.hwwach.data.remote.model.asset.AssetDto
import ed.maevski.hwwach.domain.models.Asset
import ed.maevski.hwwach.domain.models.AssetStatusEnum
import ed.maevski.hwwach.domain.models.ModerationStatusEnum
import ed.maevski.hwwach.domain.models.UploadStatusEnum
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class AssetDtoToDomainMapper @Inject constructor() {
    fun map(dto: AssetDto): Asset {
        return Asset(
            clientId = dto.clientId,
            serverUuid = dto.uuid,
            name = dto.name ?: "",
            category = dto.category,
            inventoryNum = dto.inventoryNum,
            description = dto.description,
            assetStatus = dto.assetStatus?.let { statusStr ->
                try {
                    AssetStatusEnum.valueOf(statusStr.uppercase(Locale.ROOT))
                } catch (e: IllegalArgumentException) {
                    null
                }
            },
            moderationStatus = ModerationStatusEnum.SYNCED,
            adminComment = dto.adminComment,
            createdAt = parseIsoDate(dto.createdAt),
            updatedAt = parseIsoDate(dto.updatedAt),
            localCreatedAt = System.currentTimeMillis(),
            lastUpdatedLocally = System.currentTimeMillis(),
            status = UploadStatusEnum.SYNCED,
        )
    }

    private fun parseIsoDate(dateString: String?): Long? {
        if (dateString.isNullOrBlank()) return null
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(dateString)?.time
        } catch (e: Exception) {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                format.timeZone = TimeZone.getTimeZone("UTC")
                format.parse(dateString)?.time
            } catch (e2: Exception) {
                null
            }
        }
    }
}
