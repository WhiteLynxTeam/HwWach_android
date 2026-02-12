package com.whitelynxteam.hwwach.domain.models

enum class RegStatusEnum {
    PENDING,
    APPROVED,
    REJECTED;

    fun toDisplayString() = when (this) {
        PENDING -> "на проверке"
        APPROVED -> "одобрена"
        REJECTED -> "отклонена"
    }
}