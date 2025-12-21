package com.whitelynxteam.hwwach.domain

// Domain слой — бизнес-ошибки
sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()

    data object UnauthorizedError : DomainResult<Nothing>()
    data class ServerError(val code: Int) : DomainResult<Nothing>()
    data class NetworkError(val message: String) : DomainResult<Nothing>()

    data class ValidationError(val message: String) : DomainResult<Nothing>()
}