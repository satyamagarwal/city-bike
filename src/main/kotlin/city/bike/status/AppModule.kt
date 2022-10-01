package city.bike.status

import city.bike.status.app.Environment
import city.bike.status.app.Environment.Local
import city.bike.status.app.Environment.Prod
import city.bike.status.app.JacksonObjectMapper.objectMapper
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
import java.util.Base64

fun Application.appModule() {
    val environment: Environment = System.getProperty("env")?.takeIf { it == "prod" }?.let { Prod } ?: Local
    val configToClient: String = when (environment) {
        Local -> "{ isProd: false }"
        Prod -> "{ isProd: true }"
    }
    val encodedConfig: String = Base64.getEncoder().encodeToString(configToClient.toByteArray())

    if (environment is Local) {
        install(CORS) {
            anyHost()
        }
    }

    install(CallLogging)
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(objectmapper = objectMapper))
    }
    routing {
        if (environment is Prod) {
            static("/") {
                resources("dist")
                sendIndexHtml(encodedConfig)
            }
            static("dist") {
                resources("assets")
            }
        }
        livelinessProbe()
        getStationsWithStatus()
    }
}
