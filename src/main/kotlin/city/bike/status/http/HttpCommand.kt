package city.bike.status.http

import arrow.core.Either
import city.bike.status.app.ErrorOr
import city.bike.status.app.JacksonObjectMapper
import city.bike.status.http.HttpSetup.circuitBreakerRegistry
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

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
            register(ContentType.Application.Json, JacksonConverter(objectmapper = JacksonObjectMapper.objectMapper))
        }
    }
}

data class HttpClientTimeoutInput(
    val connectionTimeout: Long,
    val requestTimeout: Long
)

data class HttpCommand(
    private val commandKey: String,
    val httpClient: HttpClient = HttpSetup.httpClient,
    val httpClientTimeoutInput: HttpClientTimeoutInput = HttpSetup.defaultHttpClientTimeoutInput
) {

    val circuitBreaker: CircuitBreaker = circuitBreakerRegistry.circuitBreaker(commandKey)

    suspend inline fun <reified A> execute(request: HttpRequestBuilder): ErrorOr<A> {
        request.timeout {
            requestTimeoutMillis = httpClientTimeoutInput.requestTimeout
            connectTimeoutMillis = httpClientTimeoutInput.connectionTimeout
        }

        return withContext(IO) {
            Either
                .catch { circuitBreaker.executeSuspendFunction { httpClient.request(request).body<A>() } }
                .handleBackendResponse()
        }
    }
}
