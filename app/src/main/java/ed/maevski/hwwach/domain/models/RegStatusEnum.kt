package ed.maevski.hwwach.domain.models

enum class RegStatusEnum {
    PENDING,
    APPROVED,
    REJECTED,
    // админ не рассмотрел заявку за 30 дней
    EXPIRED;

    fun toDisplayString() = when (this) {
        PENDING -> "на проверке"
        APPROVED -> "одобрен"
        REJECTED -> "отказан"
        EXPIRED -> "заявка устарела — подайте заново"
    }
}