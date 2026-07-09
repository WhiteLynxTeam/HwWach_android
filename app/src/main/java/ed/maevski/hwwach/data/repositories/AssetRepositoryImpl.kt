package ed.maevski.hwwach.data.repositories

import ed.maevski.hwwach.data.local.TransactionRunner
import ed.maevski.hwwach.data.local.dao.AssetDao
import ed.maevski.hwwach.data.local.dao.AssetPhotoCrossRefDao
import ed.maevski.hwwach.data.local.entity.AssetPhotoCrossRef
import ed.maevski.hwwach.data.mappers.AssetDomainToEntityMapper
import ed.maevski.hwwach.data.mappers.AssetDomainToRequestDtoMapper
import ed.maevski.hwwach.data.mappers.AssetDtoToDomainMapper
import ed.maevski.hwwach.data.mappers.AssetEntityToDomainMapper
import ed.maevski.hwwach.data.mappers.ResponseErrorMapper
import ed.maevski.hwwach.data.remote.api.AssetApi
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IAssetRepository
import ed.maevski.hwwach.domain.irepositories.IPhotoRepository
import ed.maevski.hwwach.domain.models.Asset
import ed.maevski.hwwach.domain.models.ModerationStatusEnum
import ed.maevski.hwwach.domain.models.UploadStatusEnum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

class AssetRepositoryImpl @Inject constructor(
    private val transactionRunner: TransactionRunner,
    private val assetDao: AssetDao,
    private val assetEntityToDomainMapper: AssetEntityToDomainMapper,
    private val assetDomainToEntityMapper: AssetDomainToEntityMapper,
    private val assetDtoToDomainMapper: AssetDtoToDomainMapper,
    private val assetDomainToRequestDtoMapper: AssetDomainToRequestDtoMapper,
    private val responseErrorMapper: ResponseErrorMapper,
    private val assetPhotoCrossRefDao: AssetPhotoCrossRefDao,
    private val photoRepository: IPhotoRepository,
    @Named("api") private val assetApi: AssetApi
) : IAssetRepository {

    override suspend fun hasAssets(): Int {
        return assetDao.hasAssets()
    }

    override suspend fun getAssets(): DomainResult<List<Asset>> {
        return try {
            val response = assetApi.getAssets()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val assets = body.assets.map { assetDtoToDomainMapper.map(it) }
                    upsertAssets(assets)
                    DomainResult.Success(assets)
                } else {
                    DomainResult.NetworkError("Empty body")
                }
            } else {
                responseErrorMapper.map(response)
            }
        } catch (e: Exception) {
            DomainResult.NetworkError(e.message ?: "Unknown error")
        }
    }

    override suspend fun addAsset(asset: Asset): DomainResult<Asset> {
        // 1. Транзакция: сохраняем asset (PENDING) + cross-refs в БД
        val assetEntity = assetDomainToEntityMapper.map(asset)
        val crossRefs = asset.photoClientIds.map { photoClientId ->
            AssetPhotoCrossRef(
                assetClientId = asset.clientId,
                photoClientId = photoClientId
            )
        }

        try {
            transactionRunner {
                assetDao.insertAsset(assetEntity)
                assetPhotoCrossRefDao.insertCrossRefs(crossRefs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Транзакция не удалась — запросы на API не производим
            return DomainResult.NetworkError("Ошибка записи в БД: ${e.message}")
        }

        // 2. Загружаем только фотографии этого актива на сервер
        //    При любой ошибке — помечаем asset как FAILED
        try {
            photoRepository.syncPhotosByClientIds(asset.photoClientIds)
        } catch (e: Exception) {
            e.printStackTrace()
            assetDao.updateAssetStatus(asset.clientId, UploadStatusEnum.FAILED.name)
            return DomainResult.NetworkError("Ошибка загрузки фото: ${e.message}")
        }

        // 3. Все фото синхронизированы → переводим asset в UPLOADING
        assetDao.updateAssetStatus(asset.clientId, UploadStatusEnum.UPLOADING.name)

        // 4. Отправляем asset JSON на сервер с photoClientIds
        return try {
            val requestDto = assetDomainToRequestDtoMapper.map(asset)
            val response = assetApi.createAsset(requestDto)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val mappedAsset = assetDtoToDomainMapper.map(body)
                    val createdAsset = if (mappedAsset.clientId.isBlank()) {
                        mappedAsset.copy(clientId = asset.clientId)
                    } else {
                        mappedAsset
                    }
                    // Успех — сохраняем данные с сервера и ставим SYNCED через частичный апдейт
                    // чтобы не затереть каскадным удалением локальные связи с фото
                    assetDao.updateAssetSyncInfo(
                        clientId = createdAsset.clientId,
                        serverUuid = createdAsset.serverUuid,
                        status = UploadStatusEnum.SYNCED.name,
                        moderationStatus = (createdAsset.moderationStatus ?: ModerationStatusEnum.SYNCED).name,
                        createdAt = createdAsset.createdAt,
                        updatedAt = createdAsset.updatedAt
                    )
                    DomainResult.Success(createdAsset)
                } else {
                    assetDao.updateAssetStatus(asset.clientId, UploadStatusEnum.FAILED.name)
                    DomainResult.NetworkError("Empty body")
                }
            } else {
                assetDao.updateAssetStatus(asset.clientId, UploadStatusEnum.FAILED.name)
                responseErrorMapper.map(response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            assetDao.updateAssetStatus(asset.clientId, UploadStatusEnum.FAILED.name)
            DomainResult.NetworkError(e.message ?: "Unknown error")
        }
    }

    override fun getAssetsFlow(): Flow<List<Asset>> {
        return assetDao.getAllAssets().map { entities ->
            entities.map { assetEntityToDomainMapper.map(it) }
        }
    }

    override fun getAssetFlow(clientId: String): Flow<Asset?> {
        return assetDao.getAssetByClientId(clientId).map { entity ->
            entity?.let { assetEntityToDomainMapper.map(it) }
        }
    }

    suspend fun saveAsset(asset: Asset) {
        val entity = assetDomainToEntityMapper.map(asset)
        assetDao.insertAsset(entity)
    }

    suspend fun saveAssets(assets: List<Asset>) {
        val entities = assets.map { assetDomainToEntityMapper.map(it) }
        assetDao.insertAssets(entities)
    }

    suspend fun upsertAssets(assets: List<Asset>) {
        val entities = assets.map { assetDomainToEntityMapper.map(it) }
        assetDao.upsertAssets(entities)
    }
}
