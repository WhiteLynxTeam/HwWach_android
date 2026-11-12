package ed.maevski.hwwach.domain.irepositories

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.models.AssetLocationKind
import ed.maevski.hwwach.domain.models.Location
import ed.maevski.hwwach.domain.models.MolMode
import kotlinx.coroutines.flow.Flow

// Закреплённые место и МОЛ: подставляются в каждый следующий актив, пока не открепят
data class PinnedLocation(val kind: AssetLocationKind, val chain: List<Location>)
data class PinnedMol(val mode: MolMode, val personId: String?, val personName: String?)

interface ILocationRepository {
    fun observeAll(): Flow<List<Location>>
    /** Последние места, куда заводили активы */
    fun observeRecent(limit: Int = 5): Flow<List<Location>>
    /** Справочник с сервера; без сети остаётся прежняя копия */
    suspend fun refresh(): DomainResult<Unit>

    /** Новое место на телефоне (или готовое с тем же именем под тем же родителем) */
    suspend fun createLocal(parent: Location?, name: String): Location
    /** Цепочка от корня до места по локальному справочнику; пусто — места нет */
    suspend fun chainFor(id: String): List<Location>
    /** Вернуть в справочник узлы цепочки, если их там нет (форма держит цепочку, пока актив не сохранён) */
    suspend fun ensureChain(chain: List<Location>)
    /** Убрать заведённые на телефоне места, которые больше никому не нужны (после отправки активов) */
    suspend fun cleanupLocal()

    val pinnedLocation: Flow<PinnedLocation?>
    val pinnedMol: Flow<PinnedMol?>
    suspend fun pinLocation(value: PinnedLocation?)
    suspend fun pinMol(value: PinnedMol?)
}
