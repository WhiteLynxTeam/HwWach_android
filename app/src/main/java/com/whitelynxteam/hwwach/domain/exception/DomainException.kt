package com.whitelynxteam.hwwach.domain.exception

open class DomainException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)