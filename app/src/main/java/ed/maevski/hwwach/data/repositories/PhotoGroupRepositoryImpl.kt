package ed.maevski.hwwach.data.repositories

import ed.maevski.hwwach.data.local.PreferencesDataStore
import ed.maevski.hwwach.data.local.dao.PhotoGroupDao
import ed.maevski.hwwach.data.local.entity.PhotoGroupEntity
import ed.maevski.hwwach.domain.irepositories.IPhotoGroupRepository
import ed.maevski.hwwach.domain.models.AssetLocationKind
import ed.maevski.hwwach.domain.models.PhotoGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PhotoGroupRepositoryImpl @Inject constructor(
    private val dao: PhotoGroupDao,
    private val prefs: PreferencesDataStore,
) : IPhotoGroupRepository {

    override fun observeGroups(): Flow<List<PhotoGroup>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getGroup(id: String): PhotoGroup? = dao.getById(id)?.toDomain()

    override suspend fun saveGroup(group: PhotoGroup) = dao.upsert(group.toEntity())

    override suspend fun deleteGroup(id: String) {
        dao.deleteGroup(id)
        if (prefs.galleryActiveGroup.first() == id) prefs.saveGalleryActiveGroup(null)
    }

    override suspend fun setPhotosGroup(photoIds: List<String>, groupId: String?) {
        if (photoIds.isNotEmpty()) dao.setPhotosGroup(photoIds, groupId)
    }

    override suspend fun setPhotosNote(photoIds: List<String>, note: String?) {
        if (photoIds.isNotEmpty()) dao.setPhotosNote(photoIds, note?.trim()?.ifEmpty { null })
    }

    override fun observeAttachedPhotoIds(): Flow<Set<String>> = dao.observeAttachedPhotoIds().map { it.toSet() }

    override val activeGroupId: Flow<String?> = prefs.galleryActiveGroup

    override suspend fun setActiveGroup(id: String?) = prefs.saveGalleryActiveGroup(id)

    private fun PhotoGroupEntity.toDomain() = PhotoGroup(
        id = id,
        name = name,
        note = note,
        locationKind = AssetLocationKind.fromServer(locationKind),
        locationId = locationId,
        locationPath = locationPath,
        personId = personId,
        personName = personName,
        personIsMol = personIsMol,
        createdAt = createdAt,
    )

    private fun PhotoGroup.toEntity() = PhotoGroupEntity(
        id = id,
        name = name.trim(),
        note = note?.trim()?.ifEmpty { null },
        locationKind = locationKind.server,
        locationId = locationId,
        locationPath = locationPath,
        personId = personId,
        personName = personName,
        personIsMol = personIsMol,
        createdAt = createdAt,
    )
}
