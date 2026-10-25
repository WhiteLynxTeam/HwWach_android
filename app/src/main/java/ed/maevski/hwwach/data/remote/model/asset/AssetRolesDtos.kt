package ed.maevski.hwwach.data.remote.model.asset

import com.google.gson.annotations.SerializedName

// Бизнес-роль на активе: mol — «числится за», user — «работает с активом»
data class AssetRoleDto(
    @SerializedName("id") val id: String,
    @SerializedName("person_id") val personId: String,
    @SerializedName("role") val role: String,
    @SerializedName("started_at") val startedAt: String? = null,
    // pending — «без подтверждения», accepted, disputed — отказ
    @SerializedName("ack_status") val ackStatus: String,
    @SerializedName("ack_reason") val ackReason: String? = null,
)

data class RepairDto(
    @SerializedName("id") val id: String,
    @SerializedName("kind") val kind: String,
    @SerializedName("started_at") val startedAt: String? = null,
    @SerializedName("expected_return_at") val expectedReturnAt: String? = null,
    @SerializedName("overdue") val overdue: Boolean = false,
)

// Карточка с сервера: состояние и ответственные (локально в Room их нет)
data class AssetDetailDto(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("inventory_num") val inventoryNum: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("accounting_status") val accountingStatus: String? = null,
    @SerializedName("condition") val condition: String? = null,
    @SerializedName("moderation_status") val moderationStatus: String? = null,
    @SerializedName("mol") val mol: AssetRoleDto? = null,
    @SerializedName("users") val users: List<AssetRoleDto>? = null,
    @SerializedName("open_repair") val openRepair: RepairDto? = null,
)

// «Мне назначено»
data class AssignmentDto(
    @SerializedName("id") val id: String,
    @SerializedName("person_id") val personId: String,
    @SerializedName("role") val role: String,
    @SerializedName("started_at") val startedAt: String? = null,
    @SerializedName("ack_status") val ackStatus: String,
    @SerializedName("ack_reason") val ackReason: String? = null,
    @SerializedName("asset") val asset: AssetDetailDto? = null,
)

data class AckRequest(
    @SerializedName("action") val action: String, // accept / dispute
    @SerializedName("reason") val reason: String? = null,
)

data class AssignPersonRequest(
    @SerializedName("person_id") val personId: String,
    @SerializedName("comment") val comment: String? = null,
)

data class CreateRepairRequestDto(
    @SerializedName("asset_uuid") val assetUuid: String,
    @SerializedName("message") val message: String,
    @SerializedName("mark_faulty") val markFaulty: Boolean = true,
)
