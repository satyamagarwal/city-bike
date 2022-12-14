package city.bike.status.app

import arrow.core.Nel
import city.bike.status.app.DomainError.InvalidData
import city.bike.status.app.DomainError.LogicalError
import city.bike.status.app.DomainResponse.Companion.domainResponseLog
import city.bike.status.app.DomainResponse.DomainErrorResponse
import city.bike.status.app.DomainResponse.DomainSuccessResponse
import city.bike.status.app.ErrorMessage.Companion.internalServerError
import city.bike.status.app.ErrorMessage.Companion.invalidUserInput
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import org.slf4j.Logger
import org.slf4j.LoggerFactory

sealed interface DomainResponse {
    data class DomainErrorResponse(val status: Int, val message: ErrorMessage?) : DomainResponse {
        constructor(error: HttpStatusCode) : this(error.value, ErrorMessage(error.description, emptyList(), null))
        constructor(error: HttpStatusCode, errorMessage: ErrorMessage) : this(error.value, errorMessage)
    }

    data class DomainSuccessResponse<A : Any>(val status: Int, val message: A, val code: String?) : DomainResponse

    companion object {
        val domainResponseLog: Logger = LoggerFactory.getLogger(DomainResponse::class.java)
    }
}

suspend fun DomainResponse.send(call: ApplicationCall) {
    return when (this) {
        is DomainSuccessResponse<*> -> call.respond(HttpStatusCode.fromValue(this.status), this.message)
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
        { r -> DomainSuccessResponse(status = successStatus, message = r, code = null) }
    )

fun createFailDomainResponse(error: Throwable): DomainErrorResponse =
    when (error) {
        is InvalidData -> DomainErrorResponse(BadRequest, invalidUserInput(error.errors))
        is LogicalError -> DomainErrorResponse(InternalServerError, internalServerError(error.errors))
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
        fun internalServerError(errors: Nel<String>): ErrorMessage =
            ErrorMessage("Internal Server Error", errors = errors.toList(), null)

        fun invalidUserInput(errors: Nel<String>): ErrorMessage =
            ErrorMessage(description = "Invalid user input", errors = errors.toList(), code = null)
    }
}
