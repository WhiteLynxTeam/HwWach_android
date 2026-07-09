package ed.maevski.hwwach.domain.models

enum class AssetStatusEnum {
    ACTIVE,         // AssetStatus = "active"         // В эксплуатации
    INACTIVE,       // AssetStatus = "inactive"       // Не используется
    MAINTENANCE,    // AssetStatus = "maintenance"    // На обслуживании
    REPAIR,         // AssetStatus = "repair"         // В ремонте
    DECOMMISSIONED, // AssetStatus = "decommissioned" // Списан
    LOST,           // AssetStatus = "lost"           // Утерян

}
