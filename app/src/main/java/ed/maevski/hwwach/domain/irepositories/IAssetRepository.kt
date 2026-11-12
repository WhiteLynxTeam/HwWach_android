package ed.maevski.hwwach.domain.irepositories

import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.models.Asset
import ed.maevski.hwwach.domain.models.Location
import ed.maevski.hwwach.domain.models.UploadQueueState
import ed.maevski.hwwach.domain.models.UploadRunResult
import kotlinx.coroutines.flow.Flow

interface IAssetRepository {

    suspend fun hasAssets(): Int
    suspend fun getAssets(): DomainResult<List<Asset>>

    /**
     * Сохраняет актив на телефоне и ставит в очередь отправки — не ждёт сеть.
     * chain — место от корня (для PLACE), его узлы вернутся в справочник, если их там нет
     */
    suspend fun addAsset(asset: Asset, chain: List<Location> = emptyList()): DomainResult<Asset>

    /** Актив с id прикреплённых фото */
    suspend fun getAsset(clientId: String): Asset?

    /**
     * Правка карточки. Не отправленный актив правится на телефоне и снова встаёт в очередь;
     * отправленный — сразу на сервере (нужна сеть)
     */
    suspend fun updateAsset(asset: Asset, chain: List<Location> = emptyList()): DomainResult<Asset>

    /** Проход очереди отправки (вызывает фоновая задача). giveUp — последняя попытка: сбои сети → FAILED */
    suspend fun uploadQueue(giveUp: Boolean): UploadRunResult

    /** Не ушедшие активы — снова в очередь */
    suspend fun retryFailed()

    fun observeUploadQueue(): Flow<UploadQueueState>
    fun getAssetsFlow(): Flow<List<Asset>>
    fun getAssetFlow(clientId: String): Flow<Asset?>
}
