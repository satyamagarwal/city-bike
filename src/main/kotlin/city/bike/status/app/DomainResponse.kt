package city.bike.status.app

import arrow.core.Nel
import city.bike.status.app.DomainError.InvalidData
import city.bike.status.app.DomainError.NotFound
import city.bike.status.app.DomainResponse.Companion.domainResponseLog
import city.bike.status.app.DomainResponse.DomainErrorResponse
import city.bike.status.app.DomainResponse.DomainSuccessResponse
import city.bike.status.app.ErrorMessage.Companion.invalidUserInput
import city.bike.status.app.ErrorMessage.Companion.notFound
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import org.slf4j.Logger
import org.slf4j.LoggerFactory

sealed class DomainResponse {
    data class DomainErrorResponse(val status: Int, val message: ErrorMessage?) : DomainResponse() {
        constructor(error: HttpStatusCode) : this(error.value, ErrorMessage(error.description, emptyList(), null))
        constructor(error: HttpStatusCode, errorMessage: ErrorMessage) : this(error.value, errorMessage)
    }

    data class DomainSuccessResponse(val status: Int, val message: Any, val code: String?) : DomainResponse()

    companion object {
        val domainResponseLog: Logger = LoggerFactory.getLogger(DomainResponse::class.java)
    }
}

suspend fun DomainResponse.send(call: ApplicationCall) {
    return when (this) {
        is DomainSuccessResponse -> call.respond(HttpStatusCode.fromValue(this.status), this.message)
        is DomainErrorResponse ->
            when (this.message) {
                is ErrorMessage -> call.respond(HttpStatusCode.fromValue(this.status), this.message)
                else -> call.respond(HttpStatusCode.fromValue(this.status), this)
            }
    }
}

inline fun <reified A : Any> ErrorOr<A>.toResponse(successStatus: Int = OK.value): DomainResponse =
    this.fold(
        { e -> createFailDomainResponse(e) },
        { r -> DomainSuccessResponse(successStatus, r, null) }
    )

fun createFailDomainResponse(error: Throwable): DomainResponse =
    when (error) {
        is InvalidData -> DomainErrorResponse(BadRequest, invalidUserInput(error.errors))
        is NotFound -> DomainErrorResponse(NotFound, notFound(error.message))
        else -> {
            domainResponseLog.error("Unexpected failure", error)

            DomainErrorResponse(InternalServerError)
        }
    }

data class ErrorMessage(
    val description: String,
    val errors: List<String>,
    val code: String?
) {
    companion object {
        fun notFound(msg: String?) =
            ErrorMessage("Not found", listOfNotNull(msg), null)

        fun internalServerError(msg: String?) =
            ErrorMessage("Internal Server Error", listOfNotNull(msg), null)

        fun invalidUserInput(errors: Nel<String>) =
            ErrorMessage("Invalid user input", errors.toList(), null)
    }
}
