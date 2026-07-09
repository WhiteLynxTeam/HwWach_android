package ed.maevski.hwwach.domain.models

data class Token(
    val accessToken: String,
    val refreshToken: String,
)