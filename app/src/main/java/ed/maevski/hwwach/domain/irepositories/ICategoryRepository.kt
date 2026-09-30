package ed.maevski.hwwach.domain.irepositories

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.models.Category
import kotlinx.coroutines.flow.Flow

interface ICategoryRepository {
    fun searchLocal(query: String, userUuid: String): Flow<List<Category>>
    fun getTopCategories(userUuid: String): Flow<List<Category>>
    suspend fun searchRemote(query: String): DomainResult<List<Category>>
    suspend fun syncCategories(): DomainResult<Unit>
    suspend fun findExactMatch(name: String, userUuid: String): Category?
    suspend fun saveCategory(category: Category)
}
