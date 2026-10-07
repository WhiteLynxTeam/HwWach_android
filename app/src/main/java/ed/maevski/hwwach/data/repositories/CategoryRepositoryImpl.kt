package ed.maevski.hwwach.data.repositories

import ed.maevski.hwwach.data.local.PreferencesDataStore
import ed.maevski.hwwach.data.local.TransactionRunner
import ed.maevski.hwwach.data.local.dao.CategoryDao
import ed.maevski.hwwach.data.local.entity.CategoryEntity
import ed.maevski.hwwach.data.mappers.ResponseErrorMapper
import ed.maevski.hwwach.data.remote.api.CategoryApi
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.ICategoryRepository
import ed.maevski.hwwach.domain.models.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    @Named("api") private val categoryApi: CategoryApi,
    private val transactionRunner: TransactionRunner,
    private val preferencesDataStore: PreferencesDataStore,
    private val responseErrorMapper: ResponseErrorMapper
) : ICategoryRepository {

    override fun searchLocal(query: String, userUuid: String): Flow<List<Category>> {
        val normQuery = query.trim().lowercase()
        return categoryDao.search(normQuery, userUuid).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTopCategories(userUuid: String): Flow<List<Category>> {
        return categoryDao.getTopCategories(userUuid).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun searchRemote(query: String): DomainResult<List<Category>> {
        return try {
            val response = categoryApi.searchCategories(query)
            if (response.isSuccessful) {
                val body = response.body() ?: emptyList()
                val domainList = body.map { it.toDomain() }
                // Кэшируем полученные с сервера категории в локальную базу Room
                if (domainList.isNotEmpty()) {
                    categoryDao.upsertAll(domainList.map { CategoryEntity.fromDomain(it) })
                }
                DomainResult.Success(domainList)
            } else {
                responseErrorMapper.map(response)
            }
        } catch (e: Exception) {
            DomainResult.NetworkError(e.message ?: "Unknown error")
        }
    }

    override suspend fun syncCategories(): DomainResult<Unit> {
        return try {
            val since = preferencesDataStore.lastCategoriesSyncAt.firstOrNull()
            val response = categoryApi.syncCategories(since)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    transactionRunner {
                        if (body.categories.isNotEmpty()) {
                            val entities = body.categories.map { CategoryEntity.fromDomain(it.toDomain()) }
                            categoryDao.upsertAll(entities)
                        }
                        if (body.deletedIds.isNotEmpty()) {
                            categoryDao.deleteByIds(body.deletedIds)
                        }
                    }
                    preferencesDataStore.saveLastCategoriesSyncAt(body.syncedAt)
                    DomainResult.Success(Unit)
                } else {
                    DomainResult.NetworkError("Empty sync response")
                }
            } else {
                responseErrorMapper.map(response)
            }
        } catch (e: Exception) {
            DomainResult.NetworkError(e.message ?: "Unknown error")
        }
    }

    override suspend fun findExactMatch(name: String, userUuid: String): Category? {
        val norm = name.trim().lowercase()
        return categoryDao.findExactMatch(norm, userUuid)?.toDomain()
    }

    override suspend fun saveCategory(category: Category) {
        categoryDao.insert(CategoryEntity.fromDomain(category))
    }
}
