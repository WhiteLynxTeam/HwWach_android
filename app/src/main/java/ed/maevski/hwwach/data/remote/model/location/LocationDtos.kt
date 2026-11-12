package ed.maevski.hwwach.data.remote.model.location

import com.google.gson.annotations.SerializedName
import ed.maevski.hwwach.data.remote.model.asset.LocationNodeDto

data class LocationDto(
    @SerializedName("id") val id: String,
    @SerializedName("parent_id") val parentId: String?,
    @SerializedName("name") val name: String,
    @SerializedName("kind") val kind: String?,
    @SerializedName("path") val path: String,
)

// Завести цепочку мест (готовые узлы сервер найдёт по id или имени)
data class CreateLocationRequest(
    @SerializedName("chain") val chain: List<LocationNodeDto>,
)
