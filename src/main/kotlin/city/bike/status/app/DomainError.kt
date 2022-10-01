package city.bike.status.app

import arrow.core.Nel

sealed class DomainError(
    msg: String? = null,
    throwable: Throwable? = null
) : Throwable(cause = throwable, message = msg) {

    data class InternalAppError(val reason: String) : DomainError(msg = reason)

    data class BackendBadResponse(val reason: String? = null, val throwable: Throwable? = null) :
        DomainError(msg = reason, throwable = throwable)

    data class BackendInvalidRequest(val throwable: Throwable) : DomainError(throwable = throwable)
    data class BackendFatalError(val throwable: Throwable) : DomainError(throwable = throwable)
    data class BackendResponseParsingFailed(val throwable: Throwable) : DomainError(throwable = throwable)
    data class InvalidData(val errors: Nel<String>) : DomainError(
        msg = "Errors: ${errors.joinToString(", ", "[", "]")}"
    )
    data object BackendCallNotPermitted : DomainError()
    data object BackendServerDown : DomainError()
    data object BackendTimedOut : DomainError()
}
