package city.bike.status

import city.bike.status.app.Environment.Local
import city.bike.status.app.Environment.Prod
import city.bike.status.app.JacksonObjectMapper.objectMapper
import city.bike.status.app.sendIndexHtml
import city.bike.status.realtime.stationStatus
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.routing

fun Application.appModule() {
    if (ENV is Local) {
        install(CORS) {
            anyHost()
        }
    }

    install(CallLogging)
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(objectmapper = objectMapper))
    }
    routing {
        if (ENV is Prod) {
            static("/") {
                resources("dist")
                sendIndexHtml()
            }
            static("dist") {
                resources("assets")
            }
        }
        stationStatus()
    }
}
