package ed.maevski.hwwach.domain.irepositories

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.models.AssetServerInfo
import ed.maevski.hwwach.domain.models.Assignment

// Ответственные за актив и назначения (go)
interface IAssetRolesRepository {
    suspend fun getAssetInfo(serverUuid: String): DomainResult<AssetServerInfo>
    suspend fun myAssignments(): DomainResult<List<Assignment>>

    // подтвердить назначение или отказаться с причиной — актив остаётся, где поставили
    suspend fun ack(roleId: String, accept: Boolean, reason: String?): DomainResult<Unit>

    // МОЛ передаёт свой актив другому МОЛ / админ — кому угодно; применяется сразу
    suspend fun transferMol(serverUuid: String, personId: String, comment: String?): DomainResult<Unit>
    suspend fun addUser(serverUuid: String, personId: String): DomainResult<Unit>

    // заявка на ремонт: актив сразу отмечается «неисправен»
    suspend fun reportFault(serverUuid: String, message: String): DomainResult<Unit>
}
