package ed.maevski.hwwach.domain.models

// Учётный статус актива. Имена констант хранятся в Room — не переименовывать.
// Соответствие серверу (accounting_status): см. fromServer / toServer
enum class AssetStatusEnum {
    ACTIVE,         // in_use         — В эксплуатации
    INACTIVE,       // in_stock       — На складе
    MAINTENANCE,    // устарело: обслуживание теперь — «В ремонте» с видом работ
    REPAIR,         // repair         — В ремонте
    DECOMMISSIONED, // decommissioned — Списан
    LOST;           // lost           — Утерян

    fun toServer(): String = when (this) {
        ACTIVE -> "in_use"
        INACTIVE -> "in_stock"
        MAINTENANCE, REPAIR -> "repair"
        DECOMMISSIONED -> "decommissioned"
        LOST -> "lost"
    }

    companion object {
        fun fromServer(value: String?): AssetStatusEnum? = when (value) {
            "in_use" -> ACTIVE
            "in_stock" -> INACTIVE
            "repair" -> REPAIR
            "decommissioned" -> DECOMMISSIONED
            "lost" -> LOST
            else -> null
        }
    }
}
