package ed.maevski.hwwach.domain.models

// Организации, люди и бизнес-роли (org-roles-plan.md)

data class OrgSearchItem(
    val id: String,
    val name: String,
    val shortName: String?,
)

data class InviteInfo(
    val code: String,
    val organizationId: String,
    val organizationName: String,
    val lastName: String?,
    val firstName: String?,
    val middleName: String?,
    val position: String?,
)

data class PersonBrief(
    val id: String,
    val fullName: String,
    val position: String?,
    val isMol: Boolean,
    val isActive: Boolean,
)

// Подтверждение назначения: всё применяется сразу, получатель потом подтверждает или отказывается
enum class AckStatus {
    PENDING, ACCEPTED, DISPUTED;

    companion object {
        fun fromServer(value: String?): AckStatus = when (value) {
            "accepted" -> ACCEPTED
            "disputed" -> DISPUTED
            else -> PENDING
        }
    }
}

data class AssetRoleInfo(
    val id: String,
    val personId: String,
    val isMol: Boolean, // true — «числится за», false — «работает с активом»
    val ackStatus: AckStatus,
    val ackReason: String?,
)

// Состояние актива с сервера: учётный статус, техническое состояние, ответственные
data class AssetServerInfo(
    val uuid: String,
    val name: String,
    val inventoryNum: String?,
    val accountingStatus: String?,
    val condition: String?,
    val moderationStatus: String?,
    val mol: AssetRoleInfo?,
    val users: List<AssetRoleInfo>,
    val inRepairOverdue: Boolean,
)

// «Мне назначено»
data class Assignment(
    val role: AssetRoleInfo,
    val asset: AssetServerInfo?,
)

object AssetLabels {
    fun accounting(code: String?): String = when (code) {
        "in_use" -> "В эксплуатации"
        "in_stock" -> "На складе"
        "repair" -> "В ремонте"
        "lost" -> "Утерян"
        "decommissioned" -> "Списан"
        else -> "Не указан"
    }

    fun condition(code: String?): String = when (code) {
        "ok" -> "Исправен"
        "needs_attention" -> "Требует внимания"
        "faulty" -> "Неисправен"
        "unusable" -> "Не подлежит ремонту"
        else -> "Не указано"
    }

    fun ack(status: AckStatus): String = when (status) {
        AckStatus.PENDING -> "без подтверждения"
        AckStatus.ACCEPTED -> "подтверждено"
        AckStatus.DISPUTED -> "отказ"
    }
}
