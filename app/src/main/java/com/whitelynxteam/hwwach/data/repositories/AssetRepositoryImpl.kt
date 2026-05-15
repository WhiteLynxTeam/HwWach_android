package com.whitelynxteam.hwwach.data.repositories

import com.whitelynxteam.hwwach.data.local.dao.AssetDao
import com.whitelynxteam.hwwach.data.mappers.AssetDomainToEntityMapper
import com.whitelynxteam.hwwach.data.mappers.AssetEntityToDomainMapper
import com.whitelynxteam.hwwach.domain.DomainResult
import com.whitelynxteam.hwwach.domain.irepositories.IAssetRepository
import com.whitelynxteam.hwwach.domain.models.Asset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AssetRepositoryImpl @Inject constructor(
    private val assetDao: AssetDao,
    private val assetEntityToDomainMapper: AssetEntityToDomainMapper,
    private val assetDomainToEntityMapper: AssetDomainToEntityMapper,
) : IAssetRepository {

    override suspend fun hasAssets(): Int {
        return assetDao.hasAssets()
    }

    override suspend fun getAssets(): DomainResult<List<Asset>> {
        // TODO: реализовать получение из API
        return DomainResult.Success(emptyList())
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
