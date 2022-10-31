package city.bike.status

import city.bike.status.app.JacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.ApplicationTestBuilder

fun ApplicationTestBuilder.registerPlugins() {
    install(CallLogging)
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(objectmapper = JacksonObjectMapper.objectMapper))
    }
}

fun ApplicationTestBuilder.testHttpClient(): HttpClient = createClient {
    install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(objectmapper = JacksonObjectMapper.objectMapper))
    }
}
