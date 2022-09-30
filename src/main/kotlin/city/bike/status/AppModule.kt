package city.bike.status

import city.bike.status.app.JacksonObjectMapper.objectMapper
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing

fun Application.appModule() {

    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(objectmapper = objectMapper))
    }
    routing {
        livelinessProbe()
        getStationsWithStatus()
    }
}
