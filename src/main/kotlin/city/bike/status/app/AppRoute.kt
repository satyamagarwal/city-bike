package city.bike.status.app

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.leftIfNull
import city.bike.status.ENV
import city.bike.status.app.DomainError.InternalAppError
import city.bike.status.app.DomainResponse.DomainSuccessResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Base64

@Suppress("MagicNumber")
fun Route.sendIndexHtml(
    path: String = "/dist/index.html"
): Route = get("/") {
    AppRoute
        .indexHtmlResponse(path)
        .map { DomainSuccessResponse(200, it, null) }
        .mapLeft { createFailDomainResponse(it) }
        .fold(
            { call.respond(HttpStatusCode.fromValue(it.status), it.message ?: "") },
            {
                call.respondTextWriter(
                    contentType = ContentType.Text.Html,
                    status = HttpStatusCode.fromValue(it.status)
                ) { withContext(Dispatchers.IO) { this@respondTextWriter.write(it.message) } }
            }
        )
}

object AppRoute {

    private val configToClient: String = when (ENV) {
        Environment.Local -> "{ isProd: false }"
        Environment.Prod -> "{ isProd: true }"
    }
    private val encodedConfig: String = Base64.getEncoder().encodeToString(configToClient.toByteArray())

    fun indexHtmlResponse(path: String): ErrorOr<String> {
        return Either
            .catch { this.javaClass.getResourceAsStream(path) }
            .leftIfNull { InternalAppError("Cannot locate main file!") }
            .flatMap { Either.catch { it.readAllBytes() } }
            .map { String(it) }
            .map { it.replace("configString", "data-config='$encodedConfig'") }
    }
}
