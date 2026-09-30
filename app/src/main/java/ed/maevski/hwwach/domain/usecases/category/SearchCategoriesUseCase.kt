package ed.maevski.hwwach.domain.usecases.category

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.ICategoryRepository
import ed.maevski.hwwach.domain.models.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchCategoriesUseCase @Inject constructor(
    private val categoryRepository: ICategoryRepository
) {
    fun getLocal(query: String, userUuid: String): Flow<List<Category>> {
        return if (query.isBlank()) {
            categoryRepository.getTopCategories(userUuid)
        } else {
            categoryRepository.searchLocal(query, userUuid)
        }
    }

    suspend fun fetchRemote(query: String): DomainResult<List<Category>> {
        val trimmed = query.trim()
        if (trimmed.length < 3) {
            return DomainResult.Success(emptyList())
        }
        return categoryRepository.searchRemote(trimmed)
    }

    suspend fun findExactMatch(name: String, userUuid: String): Category? {
        return categoryRepository.findExactMatch(name, userUuid)
    }
}
