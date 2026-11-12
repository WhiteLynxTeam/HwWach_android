package ed.maevski.hwwach.domain.irepositories

import ed.maevski.hwwach.domain.models.PhotoGroup
import kotlinx.coroutines.flow.Flow

/** Разбор снимков на телефоне: группы, подписи, текущая группа для новых снимков */
interface IPhotoGroupRepository {
    fun observeGroups(): Flow<List<PhotoGroup>>
    suspend fun getGroup(id: String): PhotoGroup?
    suspend fun saveGroup(group: PhotoGroup)
    /** Снимки группы остаются в галерее без группы */
    suspend fun deleteGroup(id: String)

    suspend fun setPhotosGroup(photoIds: List<String>, groupId: String?)
    suspend fun setPhotosNote(photoIds: List<String>, note: String?)

    /** Снимки, уже прикреплённые к активам на телефоне */
    fun observeAttachedPhotoIds(): Flow<Set<String>>

    /** Группа, открытая в галерее: в неё попадают новые снимки. null — «Все» / «Без группы» */
    val activeGroupId: Flow<String?>
    suspend fun setActiveGroup(id: String?)
}
