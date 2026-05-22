package com.whitelynxteam.hwwach.domain.irepositories

import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.models.Asset
import kotlinx.coroutines.flow.Flow

interface IAssetRepository {

    suspend fun hasAssets(): Int
    suspend fun getAssets(): DomainResult<List<Asset>>
    suspend fun addAsset(asset: Asset): DomainResult<Asset>
    fun getAssetsFlow(): Flow<List<Asset>>
    fun getAssetFlow(clientId: String): Flow<Asset?>
}
