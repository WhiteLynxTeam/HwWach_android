package ed.maevski.hwwach.domain.models

data class Category(
    val uuid: String,
    val name: String,
    val level: Int, // 1: Global, 2: Local, 3: User
    val status: String = "approved", // approved, pending, rejected, merged
    val usageCount: Int = 0,
    val createdBy: String? = null,
    val adminComment: String? = null
) {
    val isGlobal: Boolean get() = level == 1
    val isLocal: Boolean get() = level == 2
    val isUserCustom: Boolean get() = level == 3
    val isPending: Boolean get() = status.equals("pending", ignoreCase = true)
}
