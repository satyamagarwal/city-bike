package city.bike.status.http

import city.bike.status.app.JacksonObjectMapper.objectMapper
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.utils.io.CancellationException

object HttpSetup {

    private const val failureRateThreshold = 50.0f
    private val circuitBreakerConfig: CircuitBreakerConfig = CircuitBreakerConfig
        .custom()
        .failureRateThreshold(failureRateThreshold)
        .ignoreExceptions(
            ClientRequestException::class.java,
            CancellationException::class.java,
            java.util.concurrent.CancellationException::class.java
        )
        .build()
    val circuitBreakerRegistry: CircuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig)
    val defaultHttpClientTimeoutInput = HttpClientTimeoutInput(connectionTimeout = 4900L, requestTimeout = 4950L)
    val httpClient: HttpClient = HttpClient(CIO) {
        expectSuccess = true
        followRedirects = false
        install(HttpTimeout) {
            requestTimeoutMillis = defaultHttpClientTimeoutInput.requestTimeout
            connectTimeoutMillis = defaultHttpClientTimeoutInput.connectionTimeout
        }
        install(ContentNegotiation) {
            register(ContentType.Application.Json, JacksonConverter(objectmapper = objectMapper))
        }
    }
}
