package city.bike.status.app

import arrow.core.Nel
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException

sealed class DomainError(
    msg: String? = null,
    throwable: Throwable? = null
) : Throwable(cause = throwable, message = msg) {

    data class BackendBadResponse(val reason: String? = null, val throwable: ServerResponseException? = null) :
        DomainError(msg = reason, throwable = throwable)
    data class BackendInvalidRequest(val throwable: ClientRequestException) : DomainError(throwable = throwable)
    data class BackendFatalError(val throwable: Throwable) : DomainError(throwable = throwable)
    data class InternalAppError(val reason: String) : DomainError(msg = reason)
    data class InvalidData(val errors: Nel<String>) : DomainError(
        msg = "Errors: ${errors.joinToString(", ", "[", "]")}"
    )
    data class LogicalError(val errors: Nel<String>) : DomainError(
        msg = "Errors: ${errors.joinToString(", ", "[", "]")}"
    )
    data object BackendCallNotPermitted : DomainError()
    data object BackendServerDown : DomainError()
}
