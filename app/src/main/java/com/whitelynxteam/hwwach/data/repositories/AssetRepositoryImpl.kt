package com.whitelynxteam.hwwach.data.repositories

import com.whitelynxteam.hwwach.data.local.AppDatabase
import com.whitelynxteam.hwwach.data.local.dao.AssetDao
import com.whitelynxteam.hwwach.data.local.dao.AssetPhotoCrossRefDao
import com.whitelynxteam.hwwach.data.local.entity.AssetPhotoCrossRef
import com.whitelynxteam.hwwach.data.mappers.AssetDomainToEntityMapper
import com.whitelynxteam.hwwach.data.mappers.AssetDomainToRequestDtoMapper
import com.whitelynxteam.hwwach.data.mappers.AssetDtoToDomainMapper
import com.whitelynxteam.hwwach.data.mappers.AssetEntityToDomainMapper
import com.whitelynxteam.hwwach.data.mappers.ResponseErrorMapper
import com.whitelynxteam.hwwach.data.remote.api.AssetApi
import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IAssetRepository
import com.whitelynxteam.hwwach.domain.irepositories.IPhotoRepository
import com.whitelynxteam.hwwach.domain.models.Asset
import com.whitelynxteam.hwwach.domain.models.UploadStatusEnum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Named

class AssetRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
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
                    saveAssets(assets)
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
        // 1. Транзакция: сохраняем asset (UPLOADING) + cross-refs в БД
        val assetEntity = assetDomainToEntityMapper.map(asset)
        val crossRefs = asset.photoClientIds.map { photoClientId ->
            AssetPhotoCrossRef(
                assetClientId = asset.clientId,
                photoClientId = photoClientId
            )
        }

        try {
            db.runInTransaction {
                runBlocking {
                    assetDao.insertAsset(assetEntity)
                    assetPhotoCrossRefDao.insertCrossRefs(crossRefs)
                }
            }
        } catch (e: Exception) {
            // Транзакция не удалась — запросы на API не производим
            return DomainResult.NetworkError("Ошибка записи в БД: ${e.message}")
        }

        // 2. Отправляем фотографии на сервер (аналогично Gallery)
        try {
            photoRepository.syncPendingPhotos()
        } catch (e: Exception) {
            assetDao.updateAssetStatus(asset.clientId, UploadStatusEnum.FAILED.name)
            return DomainResult.NetworkError("Ошибка загрузки фото: ${e.message}")
        }

        // 3. Отправляем asset на сервер с photoClientIds
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
                    // Успех — сохраняем данные с сервера и ставим PENDING
                    saveAsset(createdAsset.copy(status = UploadStatusEnum.PENDING))
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
}
