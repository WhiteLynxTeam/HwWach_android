package ed.maevski.hwwach.data.repositories

import ed.maevski.hwwach.data.local.PreferencesDataStore
import ed.maevski.hwwach.data.local.dao.LocationDao
import ed.maevski.hwwach.data.local.entity.LocationEntity
import ed.maevski.hwwach.data.mappers.ResponseErrorMapper
import ed.maevski.hwwach.data.remote.api.LocationApi
import ed.maevski.hwwach.domain.DomainResult
import ed.maevski.hwwach.domain.irepositories.ILocationRepository
import ed.maevski.hwwach.domain.irepositories.PinnedLocation
import ed.maevski.hwwach.domain.irepositories.PinnedMol
import ed.maevski.hwwach.domain.models.AssetLocationKind
import ed.maevski.hwwach.domain.models.LOCATION_PATH_SEP
import ed.maevski.hwwach.domain.models.Location
import ed.maevski.hwwach.domain.models.LocationKind
import ed.maevski.hwwach.domain.models.MolMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class LocationRepositoryImpl @Inject constructor(
    private val dao: LocationDao,
    @Named("api") private val api: LocationApi,
    private val prefs: PreferencesDataStore,
    private val responseErrorMapper: ResponseErrorMapper,
) : ILocationRepository {

    override fun observeAll(): Flow<List<Location>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeRecent(limit: Int): Flow<List<Location>> =
        combine(dao.observeRecentIds(limit), dao.observeAll()) { ids, all ->
            val byId = all.associateBy { it.id }
            ids.mapNotNull { byId[it]?.toDomain() }
        }

    override suspend fun refresh(): DomainResult<Unit> = try {
        val response = api.getLocations()
        if (response.isSuccessful) {
            val list = response.body().orEmpty().map {
                LocationEntity(it.id, it.parentId, it.name, LocationKind.fromServer(it.kind).server, it.path, synced = true)
            }
            dao.replaceSynced(list)
            DomainResult.Success(Unit)
        } else {
            responseErrorMapper.map(response)
        }
    } catch (e: Exception) {
        if (e is kotlin.coroutines.cancellation.CancellationException) throw e
        DomainResult.NetworkError(e.message ?: "Network error")
    }

    override suspend fun createLocal(parent: Location?, name: String): Location {
        val clean = name.trim().split(WHITESPACE).joinToString(" ")
        require(clean.isNotEmpty()) { "Пустое название места" }
        val norm = normalize(clean)
        // то же имя под тем же родителем — то же место (сервер решит так же)
        dao.observeAll().first()
            .firstOrNull { it.parentId == parent?.id && normalize(it.name) == norm }
            ?.let { return it.toDomain() }
        val depth = parent?.let { it.depth + 1 } ?: 0
        val entity = LocationEntity(
            id = UUID.randomUUID().toString(),
            parentId = parent?.id,
            name = clean,
            kind = LocationKind.forDepth(depth).server,
            path = parent?.let { it.path + LOCATION_PATH_SEP + clean } ?: clean,
            synced = false,
        )
        dao.insert(entity)
        return entity.toDomain()
    }

    override suspend fun chainFor(id: String): List<Location> {
        val chain = ArrayList<Location>()
        var current: String? = id
        while (current != null && chain.size < 20) {
            val e = dao.getById(current) ?: return emptyList()
            chain.add(0, e.toDomain())
            current = e.parentId
        }
        return chain
    }

    override suspend fun ensureChain(chain: List<Location>) {
        for (loc in chain) {
            if (dao.getById(loc.id) == null) dao.insert(loc.toEntity())
        }
    }

    override suspend fun cleanupLocal() {
        // за проход удаляются только листья — повторяем, пока есть что удалять
        var guard = 0
        while (dao.deleteOrphanLocal() > 0 && guard++ < 20) Unit
    }

    // ===== Закреплённые значения: один JSON в DataStore =====

    private val pinnedJson: Flow<JSONObject?> = prefs.assetPinnedJson.map { raw ->
        raw?.let { runCatching { JSONObject(it) }.getOrNull() }
    }

    override val pinnedLocation: Flow<PinnedLocation?> = pinnedJson.map { json ->
        val obj = json?.optJSONObject("location") ?: return@map null
        val chainJson = obj.optJSONArray("chain") ?: JSONArray()
        val chain = (0 until chainJson.length()).map { i ->
            val n = chainJson.getJSONObject(i)
            Location(
                id = n.getString("id"),
                parentId = n.optString("parentId").takeIf { it.isNotEmpty() },
                name = n.getString("name"),
                kind = LocationKind.fromServer(n.optString("kind")),
                path = n.getString("path"),
                synced = n.optBoolean("synced"),
            )
        }
        PinnedLocation(AssetLocationKind.fromServer(obj.optString("kind")), chain)
    }

    override val pinnedMol: Flow<PinnedMol?> = pinnedJson.map { json ->
        val obj = json?.optJSONObject("mol") ?: return@map null
        PinnedMol(
            mode = runCatching { MolMode.valueOf(obj.getString("mode")) }.getOrDefault(MolMode.DEFAULT),
            personId = obj.optString("personId").takeIf { it.isNotEmpty() },
            personName = obj.optString("personName").takeIf { it.isNotEmpty() },
        )
    }

    override suspend fun pinLocation(value: PinnedLocation?) = updatePinned { root ->
        if (value == null) {
            root.remove("location")
        } else {
            val chain = JSONArray()
            value.chain.forEach { l ->
                chain.put(
                    JSONObject()
                        .put("id", l.id).put("parentId", l.parentId ?: "").put("name", l.name)
                        .put("kind", l.kind.server).put("path", l.path).put("synced", l.synced)
                )
            }
            root.put("location", JSONObject().put("kind", value.kind.server).put("chain", chain))
        }
    }

    override suspend fun pinMol(value: PinnedMol?) = updatePinned { root ->
        if (value == null) {
            root.remove("mol")
        } else {
            root.put(
                "mol",
                JSONObject().put("mode", value.mode.name).put("personId", value.personId ?: "")
                    .put("personName", value.personName ?: "")
            )
        }
    }

    private suspend fun updatePinned(block: (JSONObject) -> Unit) {
        val root = pinnedJson.first() ?: JSONObject()
        block(root)
        prefs.saveAssetPinnedJson(if (root.length() == 0) null else root.toString())
    }

    private fun normalize(s: String) = s.trim().lowercase().replace('ё', 'е').split(WHITESPACE).joinToString(" ")

    private fun LocationEntity.toDomain() =
        Location(id, parentId, name, LocationKind.fromServer(kind), path, synced)

    private fun Location.toEntity() = LocationEntity(id, parentId, name, kind.server, path, synced)

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
