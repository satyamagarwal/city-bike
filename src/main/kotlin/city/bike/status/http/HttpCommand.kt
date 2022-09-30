package city.bike.status.http

import arrow.core.Either
import city.bike.status.app.ErrorOr
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import city.bike.status.http.HttpSetup.circuitBreakerRegistry

data class HttpClientTimeoutInput(
    val connectionTimeout: Long,
    val requestTimeout: Long
)

data class HttpCommand(
    private val commandKey: String,
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
                .catch { circuitBreaker.executeSuspendFunction { HttpSetup.httpClient.request(request).body<A>() } }
                .handleBackendResponse()
        }
    }
}
