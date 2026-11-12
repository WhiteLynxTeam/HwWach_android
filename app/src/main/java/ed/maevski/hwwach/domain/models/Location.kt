package ed.maevski.hwwach.domain.models

// Место из справочника организации. Дерево: населённый пункт → объект/подразделение → помещение,
// глубже — уточнения. id заводится на телефоне, сервер принимает его как есть (место можно завести без сети)
data class Location(
    val id: String,
    val parentId: String?,
    val name: String,
    val kind: LocationKind,
    val path: String,       // «Каленковичи / Ферма 700 / Кабинет зав. фермы»
    val synced: Boolean,    // false — заведено на телефоне, на сервер уйдёт вместе с активом
) {
    val depth: Int get() = path.split(LOCATION_PATH_SEP).size - 1
}

const val LOCATION_PATH_SEP = " / "

// Уровень места — подсказка для подписи поля. Значения server хранятся в Room — не менять
enum class LocationKind(val server: String, val title: String) {
    SETTLEMENT("settlement", "Населённый пункт"),
    SITE("site", "Объект / подразделение"),
    ROOM("room", "Помещение"),
    OTHER("other", "Уточнение");

    companion object {
        fun fromServer(value: String?): LocationKind = entries.firstOrNull { it.server == value } ?: OTHER

        fun forDepth(depth: Int): LocationKind = when (depth) {
            0 -> SETTLEMENT
            1 -> SITE
            2 -> ROOM
            else -> OTHER
        }
    }
}

// Где находится актив. Значения server хранятся в Room — не менять
enum class AssetLocationKind(val server: String, val title: String) {
    UNSET("unset", "Место не указано"),
    PLACE("place", "В месте"),
    NONE("none", "Без места (почта, лицензия…)"),
    PORTABLE("portable", "Носимое — у сотрудника");

    companion object {
        fun fromServer(value: String?): AssetLocationKind = entries.firstOrNull { it.server == value } ?: UNSET
    }
}

// МОЛ нового актива: по умолчанию (автор, если он МОЛ), без МОЛ, выбранный человек
enum class MolMode { DEFAULT, NONE, PERSON }
