package city.bike.status

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.leftIfNull
import arrow.core.right
import city.bike.status.app.DomainResponse.DomainSuccessResponse
import city.bike.status.app.ErrorOr
import city.bike.status.app.createFailDomainResponse
import city.bike.status.app.send
import city.bike.status.app.toResponse
import city.bike.status.realtime.RealTimeApiClientInstance
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Routes {

    fun indexHtmlResponse(): ErrorOr<String> {
        return Either
            .catch { this.javaClass.getResourceAsStream("/dist/index.html") }
            .leftIfNull { InternalError("Cannot locate main file!", null) }
            .flatMap { Either.catch { it.readAllBytes() } }
            .map { String(it) }
    }
}

fun Route.livelinessProbe(): Route = get("/liveness") {
    "ok".right().toResponse().send(call)
}

fun Route.getStationsWithStatus(): Route = get("/station-status") {
    RealTimeApiClientInstance.instance.getStationInfoAndStatus().toResponse().send(call)
}

@Suppress("MagicNumber")
fun Route.sendIndexHtml(encodedConfig: String): Route = get("/") {
    Routes
        .indexHtmlResponse()
        .map { it.replace("configString", "data-config='$encodedConfig'") }
        .map { DomainSuccessResponse(200, it, null) }
        .mapLeft { createFailDomainResponse(it) }
        .fold(
            { call.respond(HttpStatusCode.fromValue(it.status), it.message ?: "") },
            {
                call.respondTextWriter(
                    contentType = ContentType.Text.Html,
                    status = HttpStatusCode.fromValue(it.status)
                ) {
                    withContext(Dispatchers.IO) { this@respondTextWriter.write(it.message) }
                }
            }
        )
}
