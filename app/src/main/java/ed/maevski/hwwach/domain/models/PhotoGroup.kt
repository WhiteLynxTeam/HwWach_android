package ed.maevski.hwwach.domain.models

/**
 * Группа снимков в галерее: «Ферма 700, кабинет зав. фермы», «Иванов — ноутбук и телефон».
 * Нужна, чтобы большие съёмки не сваливались в кучу. Место и человек группы подставляются
 * в активы, созданные из её снимков. Живёт только на телефоне
 */
data class PhotoGroup(
    val id: String,
    val name: String,
    val note: String? = null,
    val locationKind: AssetLocationKind = AssetLocationKind.UNSET,
    val locationId: String? = null,
    val locationPath: String? = null,
    val personId: String? = null,
    val personName: String? = null,
    val personIsMol: Boolean = false,
    val createdAt: Long,
) {
    // одна строка под названием: «Каленковичи › Ферма 700 · Иванов И. И.»
    val subtitle: String
        get() = listOfNotNull(
            when (locationKind) {
                AssetLocationKind.PLACE -> locationPath?.replace(LOCATION_PATH_SEP, " › ")
                AssetLocationKind.UNSET -> null
                else -> locationKind.title
            },
            personName,
        ).joinToString(" · ")
}

/** Цепочка от корня до места по справочнику в памяти */
fun List<Location>.chainTo(target: Location): List<Location> {
    val byId = associateBy { it.id }
    val chain = ArrayList<Location>()
    var current: Location? = target
    while (current != null && chain.size < 20) {
        chain.add(0, current)
        current = current.parentId?.let { byId[it] }
    }
    return chain
}
