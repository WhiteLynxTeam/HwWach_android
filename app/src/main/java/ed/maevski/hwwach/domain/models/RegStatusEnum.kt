package ed.maevski.hwwach.domain.models

enum class RegStatusEnum {
    PENDING,
    APPROVED,
    REJECTED;

    fun toDisplayString() = when (this) {
        PENDING -> "на проверке"
        APPROVED -> "одобрен"
        REJECTED -> "отказан"
    }
}