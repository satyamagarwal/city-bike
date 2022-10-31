package city.bike.status.http

import arrow.core.handleErrorWith
import arrow.core.left
import city.bike.status.app.DomainError
import city.bike.status.app.DomainError.BackendBadResponse
import city.bike.status.app.DomainError.BackendCallNotPermitted
import city.bike.status.app.DomainError.BackendFatalError
import city.bike.status.app.DomainError.BackendInvalidRequest
import city.bike.status.app.DomainError.BackendServerDown
import city.bike.status.app.ErrorOr
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode.Companion.ServiceUnavailable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object HttpClientRequestResponse

val requestResponseLog: Logger = LoggerFactory.getLogger(HttpClientRequestResponse::class.java)

inline fun <reified A> ErrorOr<A>.handleBackendResponse(): ErrorOr<A> {
    return this.handleErrorWith { e ->
        val errorToThrow: DomainError = when (e) {
            is CallNotPermittedException -> BackendCallNotPermitted
            is ClientRequestException -> BackendInvalidRequest(e)
            is ServerResponseException -> when (e.response.status) {
                ServiceUnavailable -> BackendServerDown
                else -> BackendBadResponse(throwable = e)
            }
            else -> BackendFatalError(e)
        }

        when (errorToThrow) {
            is BackendFatalError ->
                errorToThrow
                    .left()
                    .tap { requestResponseLog.error("Failed at Backend http client.", e) }

            else -> errorToThrow.left()
        }
    }
}
