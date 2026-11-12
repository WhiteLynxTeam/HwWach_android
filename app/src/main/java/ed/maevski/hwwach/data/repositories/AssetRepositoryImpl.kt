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
import ed.maevski.hwwach.data.mappers.toNodes
import ed.maevski.hwwach.data.remote.api.AssetApi
import ed.maevski.hwwach.data.remote.model.asset.AssetDto
import ed.maevski.hwwach.data.remote.model.asset.UpdateAssetRequestDto
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.IAssetRepository
import ed.maevski.hwwach.domain.irepositories.IAssetUploadScheduler
import ed.maevski.hwwach.domain.irepositories.ILocationRepository
import ed.maevski.hwwach.domain.irepositories.IPhotoRepository
import ed.maevski.hwwach.domain.models.Asset
import ed.maevski.hwwach.domain.models.AssetLocationKind
import ed.maevski.hwwach.domain.models.FailedUpload
import ed.maevski.hwwach.domain.models.Location
import ed.maevski.hwwach.domain.models.ModerationStatusEnum
import ed.maevski.hwwach.domain.models.UploadQueueState
import ed.maevski.hwwach.domain.models.UploadRunResult
import ed.maevski.hwwach.domain.models.UploadStatusEnum
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.cancellation.CancellationException

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
    private val locationRepository: ILocationRepository,
    private val uploadScheduler: IAssetUploadScheduler,
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
                    // удалённые на сервере (или ставшие не видны) убираем; не отправленные не трогаем
                    val onServer = assets.mapNotNull { it.serverUuid }.toSet()
                    val gone = assetDao.getSyncedServerUuids().filter { it !in onServer }
                    transactionRunner {
                        gone.chunked(500).forEach { assetDao.deleteByServerUuids(it) }
                        upsertAssets(assets)
                    }
                    DomainResult.Success(assets)
                } else {
                    DomainResult.NetworkError("Empty body")
                }
            } else {
                responseErrorMapper.map(response)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DomainResult.NetworkError(e.message ?: "Unknown error")
        }
    }

    // Актив и связи с фото — в одной транзакции; отправка — в фоне (AssetUploadWorker)
    override suspend fun addAsset(asset: Asset, chain: List<Location>): DomainResult<Asset> {
        val pending = asset.copy(status = UploadStatusEnum.PENDING, errorMessage = null)
        try {
            if (pending.locationKind == AssetLocationKind.PLACE) locationRepository.ensureChain(chain)
            transactionRunner {
                assetDao.insertAsset(assetDomainToEntityMapper.map(pending))
                assetPhotoCrossRefDao.insertCrossRefs(pending.photoClientIds.map { AssetPhotoCrossRef(pending.clientId, it) })
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            return DomainResult.ValidationError("Не удалось сохранить на телефоне: ${e.message}")
        }
        uploadScheduler.schedule()
        return DomainResult.Success(pending)
    }

    override suspend fun getAsset(clientId: String): Asset? {
        val entity = assetDao.getByClientId(clientId) ?: return null
        return assetEntityToDomainMapper.map(entity)
            .copy(photoClientIds = assetPhotoCrossRefDao.getPhotoIdsByAsset(clientId))
    }

    override suspend fun updateAsset(asset: Asset, chain: List<Location>): DomainResult<Asset> {
        val current = assetDao.getByClientId(asset.clientId)
            ?: return DomainResult.ValidationError("Актив не найден на телефоне")
        if (asset.locationKind == AssetLocationKind.PLACE) locationRepository.ensureChain(chain)

        // ещё не на сервере — правим на телефоне и отправляем заново
        if (current.serverUuid == null) {
            val edited = asset.copy(
                serverUuid = null,
                status = UploadStatusEnum.PENDING,
                errorMessage = null,
                lastUpdatedLocally = System.currentTimeMillis(),
            )
            saveLocally(edited)
            uploadScheduler.schedule()
            return DomainResult.Success(edited)
        }

        return try {
            // новые снимки сначала на сервер — иначе он их не найдёт
            photoRepository.syncPhotosByClientIds(asset.photoClientIds)
            val request = UpdateAssetRequestDto(
                name = asset.name,
                inventoryNum = asset.inventoryNum ?: "",
                description = asset.description ?: "",
                category = asset.category,
                categoryUuid = asset.categoryUuid,
                categoryText = if (asset.categoryUuid == null) asset.category else null,
                locationKind = asset.locationKind.server,
                location = if (asset.locationKind == AssetLocationKind.PLACE) chain.toNodes() else null,
                photoClientIds = asset.photoClientIds,
            )
            val response = assetApi.updateAsset(current.serverUuid, request)
            if (!response.isSuccessful) return responseErrorMapper.map(response)
            val body = response.body() ?: return DomainResult.NetworkError("Пустой ответ сервера")
            val fromServer = assetDtoToDomainMapper.map(body)
            val saved = asset.copy(
                serverUuid = current.serverUuid,
                category = fromServer.category,
                categoryUuid = fromServer.categoryUuid,
                locationKind = fromServer.locationKind,
                locationId = fromServer.locationId,
                locationPath = fromServer.locationPath,
                updatedAt = fromServer.updatedAt,
                status = UploadStatusEnum.SYNCED,
                errorMessage = null,
                lastUpdatedLocally = System.currentTimeMillis(),
            )
            saveLocally(saved)
            locationRepository.refresh()
            DomainResult.Success(saved)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DomainResult.NetworkError(e.message ?: "Нет связи с сервером")
        }
    }

    private suspend fun saveLocally(asset: Asset) {
        transactionRunner {
            assetDao.updateAsset(assetDomainToEntityMapper.map(asset))
            assetPhotoCrossRefDao.deleteCrossRefsByAsset(asset.clientId)
            assetPhotoCrossRefDao.insertCrossRefs(asset.photoClientIds.map { AssetPhotoCrossRef(asset.clientId, it) })
        }
    }

    /**
     * Проход очереди по порядку создания. Ошибка данных (409 — номер занят, 400) — актив в FAILED
     * с причиной, очередь идёт дальше. Нет сети, 5xx, 401 — остановка, повтор позже:
     * следующие активы всё равно упрутся в ту же сеть
     */
    override suspend fun uploadQueue(giveUp: Boolean): UploadRunResult {
        var result = UploadRunResult.DONE
        for (entity in assetDao.getUploadQueue()) {
            currentCoroutineContext().ensureActive()
            val asset = assetEntityToDomainMapper.map(entity)
                .copy(photoClientIds = assetPhotoCrossRefDao.getPhotoIdsByAsset(entity.clientId))
            assetDao.updateAssetStatus(asset.clientId, UploadStatusEnum.UPLOADING.name)

            val outcome = uploadOne(asset)
            when (outcome) {
                is Outcome.Synced -> Unit
                is Outcome.Failed -> assetDao.markFailed(asset.clientId, outcome.message)
                is Outcome.Transient -> {
                    if (giveUp) {
                        assetDao.markFailed(asset.clientId, outcome.message)
                    } else {
                        assetDao.updateAssetStatus(asset.clientId, UploadStatusEnum.PENDING.name)
                        result = UploadRunResult.RETRY_LATER
                    }
                }
            }
            if (result == UploadRunResult.RETRY_LATER) break
        }
        if (result == UploadRunResult.DONE) {
            // места, заведённые на телефоне, теперь есть на сервере — обновляем справочник
            if (locationRepository.refresh() is DomainResult.Success) locationRepository.cleanupLocal()
        }
        return result
    }

    private sealed class Outcome {
        data object Synced : Outcome()
        data class Failed(val message: String) : Outcome()
        data class Transient(val message: String) : Outcome()
    }

    private suspend fun uploadOne(asset: Asset): Outcome {
        try {
            photoRepository.syncPhotosByClientIds(asset.photoClientIds)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            return Outcome.Transient("Фото не загрузились: ${e.message}")
        }

        val chain = if (asset.locationKind == AssetLocationKind.PLACE) {
            asset.locationId?.let { locationRepository.chainFor(it) }.orEmpty()
        } else {
            emptyList()
        }
        if (asset.locationKind == AssetLocationKind.PLACE && chain.isEmpty()) {
            return Outcome.Failed("Место не найдено на телефоне — откройте актив и укажите место заново")
        }

        val response: Response<AssetDto> = try {
            assetApi.createAsset(assetDomainToRequestDtoMapper.map(asset, chain))
        } catch (e: IOException) {
            return Outcome.Transient(e.message ?: "Нет связи с сервером")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            return Outcome.Transient(e.message ?: "Ошибка отправки")
        }

        if (!response.isSuccessful) {
            val code = response.code()
            val message = when (val mapped = responseErrorMapper.map<Unit>(response)) {
                is DomainResult.ValidationError -> mapped.message
                is DomainResult.UnauthorizedError -> "Нужно войти заново"
                else -> "Ошибка сервера ($code)"
            }
            return if (code in 400..499 && code != 401 && code != 408 && code != 429) {
                Outcome.Failed(message)
            } else {
                Outcome.Transient(message)
            }
        }

        val body = response.body() ?: return Outcome.Transient("Пустой ответ сервера")
        val created = assetDtoToDomainMapper.map(body)
        assetDao.markSynced(
            clientId = asset.clientId,
            serverUuid = created.serverUuid,
            status = UploadStatusEnum.SYNCED.name,
            moderationStatus = (created.moderationStatus ?: ModerationStatusEnum.SYNCED).name,
            createdAt = created.createdAt,
            updatedAt = created.updatedAt,
            locationKind = created.locationKind.server,
            locationId = created.locationId,
            locationPath = created.locationPath,
        )
        return Outcome.Synced
    }

    override suspend fun retryFailed() {
        if (assetDao.requeueFailed() > 0) uploadScheduler.schedule()
    }

    override fun observeUploadQueue(): Flow<UploadQueueState> =
        combine(assetDao.observeQueuedCount(), assetDao.observeFailed()) { queued, failed ->
            UploadQueueState(
                queued = queued,
                failed = failed.map { FailedUpload(it.clientId, it.name, it.errorMessage ?: "Не удалось отправить") },
            )
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

    suspend fun upsertAssets(assets: List<Asset>) {
        val entities = assets.map { assetDomainToEntityMapper.map(it) }
        assetDao.upsertAssets(entities)
    }
}
