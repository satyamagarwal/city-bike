package city.bike.status.http

import arrow.core.getOrElse
import arrow.core.right
import city.bike.status.app.DomainError
import city.bike.status.app.JacksonObjectMapper
import city.bike.status.realtime.Station
import city.bike.status.realtime.StationInformation
import city.bike.status.realtime.StationInformationResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.ServiceUnavailable
import io.ktor.http.headersOf
import io.ktor.serialization.jackson.JacksonConverter
import kotlinx.coroutines.delay

class HttpCommandTest : FunSpec({

    val httpCommand = HttpCommand(commandKey = "TestApiName")
    val httpClient: (MockRequestHandler) -> HttpClient = { handler ->
        HttpClient(MockEngine) {
            engine {
                addHandler(handler)
            }
            expectSuccess = true
            followRedirects = false
            install(HttpTimeout) {
                requestTimeoutMillis = HttpSetup.defaultHttpClientTimeoutInput.requestTimeout
                connectTimeoutMillis = HttpSetup.defaultHttpClientTimeoutInput.connectionTimeout
            }
            install(ContentNegotiation) {
                register(Json, JacksonConverter(objectmapper = JacksonObjectMapper.objectMapper))
            }
        }
    }
    val stationResponse: String =
        """
            {
              "last_updated": 1667204016,
              "ttl": 10,
              "version": "2.2",
              "data": {
                "stations": [
                  {
                    "station_id": "2349",
                    "name": "Maritimt Museum",
                    "address": "Bygd\u00f8ynesveien 37",
                    "rental_uris": {
                      "android": "oslobysykkel://stations/2349",
                      "ios": "oslobysykkel://stations/2349"
                    },
                    "lat": 59.902942924651484,
                    "lon": 10.698048967006343,
                    "capacity": 21
                  }
                ]
              }
            }
        """.trimIndent()

    test("execute should return success response from http client as expected") {
        httpCommand
            .copy(httpClient = httpClient { respond(content = "OK") })
            .execute<String>(HttpRequestBuilder())
            .shouldBe("OK".right())

        httpCommand
            .copy(
                httpClient = httpClient {
                    respond(content = stationResponse, headers = headersOf(ContentType, "application/json"))
                }
            )
            .execute<StationInformationResponse>(HttpRequestBuilder())
            .shouldBe(
                StationInformationResponse(
                    data = StationInformation(
                        listOf(
                            Station(
                                stationId = "2349",
                                name = "Maritimt Museum",
                                address = "Bygdøynesveien 37",
                                capacity = 21,
                                lat = 59.902942924651484,
                                lon = 10.698048967006343
                            )
                        )
                    )
                ).right()
            )
    }

    test("handleBackendResponse should map errored responses from http client as expected") {
        httpCommand
            .copy(httpClient = httpClient { respond(content = BadRequest.description, status = BadRequest) })
            .execute<String>(HttpRequestBuilder())
            .swap()
            .getOrElse { null }
            .shouldBeTypeOf<DomainError.BackendInvalidRequest>()

        httpCommand
            .copy(
                httpClient = httpClient {
                    respond(content = ServiceUnavailable.description, status = ServiceUnavailable)
                }
            )
            .execute<String>(HttpRequestBuilder())
            .swap()
            .getOrElse { null }
            .shouldBeTypeOf<DomainError.BackendServerDown>()

        httpCommand
            .copy(
                httpClient = httpClient { respond(content = """{ "some": "json" }""") }
            )
            .execute<Station>(HttpRequestBuilder())
            .swap()
            .getOrElse { null }
            .shouldBeTypeOf<DomainError.BackendFatalError>()

        httpCommand
            .copy(
                httpClient = httpClient {
                    respond(content = InternalServerError.description, status = InternalServerError)
                }
            )
            .execute<Station>(HttpRequestBuilder())
            .swap()
            .getOrElse { null }
            .shouldBeTypeOf<DomainError.BackendBadResponse>()

        httpCommand
            .copy(
                httpClient = httpClient { delay(100); respond(content = "OK") },
                httpClientTimeoutInput = HttpClientTimeoutInput(connectionTimeout = 10L, requestTimeout = 10L)
            )
            .execute<Station>(HttpRequestBuilder())
            .swap()
            .getOrElse { null }
            .shouldBeTypeOf<DomainError.BackendFatalError>()
    }
})
