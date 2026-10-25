package ed.maevski.hwwach.data.remote.model.auth

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    // сервер обновляет и refresh: права перечитаны, срок сессии продлён
    @SerializedName("refresh_token")
    val refreshToken: String? = null,
)
