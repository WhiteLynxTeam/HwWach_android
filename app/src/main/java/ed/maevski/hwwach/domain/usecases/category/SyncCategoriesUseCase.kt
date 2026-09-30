package ed.maevski.hwwach.domain.usecases.category

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.ICategoryRepository
import javax.inject.Inject

class SyncCategoriesUseCase @Inject constructor(
    private val categoryRepository: ICategoryRepository
) {
    suspend operator fun invoke(): DomainResult<Unit> {
        return categoryRepository.syncCategories()
    }
}
