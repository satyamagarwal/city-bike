package city.bike.status.http

import arrow.core.handleErrorWith
import arrow.core.left
import city.bike.status.app.DomainError
import city.bike.status.app.DomainError.BackendBadResponse
import city.bike.status.app.DomainError.BackendCallNotPermitted
import city.bike.status.app.DomainError.BackendFatalError
import city.bike.status.app.DomainError.BackendInvalidRequest
import city.bike.status.app.DomainError.BackendResponseParsingFailed
import city.bike.status.app.DomainError.BackendServerDown
import city.bike.status.app.DomainError.BackendTimedOut
import city.bike.status.app.ErrorOr
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode.Companion.ServiceUnavailable
import kotlinx.coroutines.TimeoutCancellationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object HttpClientRequestResponse

val requestResponseLog: Logger = LoggerFactory.getLogger(HttpClientRequestResponse::class.java)

inline fun <reified A> ErrorOr<A>.handleBackendResponse(): ErrorOr<A> {
    return this.handleErrorWith { e ->
        val errorToThrow: DomainError = when (e) {
            is CallNotPermittedException -> BackendCallNotPermitted
            is ClientRequestException -> BackendInvalidRequest(e)
            is JsonMappingException -> BackendResponseParsingFailed(e)
            is JsonProcessingException -> BackendResponseParsingFailed(e)
            is ServerResponseException -> when (e.response.status) {
                ServiceUnavailable -> BackendServerDown
                else -> BackendBadResponse(throwable = e)
            }

            is TimeoutCancellationException -> BackendTimedOut
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
