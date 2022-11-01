package city.bike.status.realtime

import arrow.core.left
import arrow.core.right
import city.bike.status.app.DomainError.BackendFatalError
import city.bike.status.app.ErrorMessage
import city.bike.status.app.ErrorOr
import city.bike.status.registerPlugins
import city.bike.status.testHttpClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.testing.testApplication
import java.time.Instant

class RealTimeRouteTest : FunSpec({

    val station = Station(
        stationId = "0001",
        name = "Maritimt Museum",
        address = "Bygdøynesveien 37",
        capacity = 21,
        lat = 59.902942924651484,
        lon = 10.698048967006343
    )

    val status = Status(
        stationId = "0001",
        installed = 1,
        renting = 1,
        returning = 1,
        reported = Instant.ofEpochMilli(1667212857),
        numBikesAvailable = 0,
        numDocksAvailable = 18
    )

    test("getStationsWithStatus returns InternalServerError if server returns a fatal error") {
        val apiClient = RealTimeApiClientStub(
            { BackendFatalError(RuntimeException("Internal Server Error")).left() },
            { StationStatusResponse(data = StationStatus(stations = emptyList())).right() }
        )

        testApplication {
            registerPlugins()

            routing { stationStatus(apiClient) }

            val response: HttpResponse = testHttpClient().get("/station-status")
            val content: ErrorMessage = response.body()

            response.status shouldBe InternalServerError
            content shouldBe ErrorMessage("Internal Server Error", errors = emptyList(), null)
        }
    }

    test("getStationsWithStatus returns InternalServerError if server returns empty list") {
        val apiClient: RealTimeApiClient = RealTimeApiClientStub(
            stationInformationFn = {
                StationInformationResponse(data = StationInformation(stations = emptyList())).right()
            },
            stationStatusFn = { StationStatusResponse(data = StationStatus(stations = emptyList())).right() }
        )

        testApplication {
            registerPlugins()

            routing { stationStatus(apiClient) }

            val response: HttpResponse = testHttpClient().get("/station-status")
            val content: ErrorMessage = response.body()

            response.status shouldBe InternalServerError
            content shouldBe ErrorMessage(
                description = "Internal Server Error",
                errors = listOf("Server sent either empty or invalid data"),
                code = null
            )
        }
    }

    test("getStationsWithStatus returns InternalServerError if server returns mismatched stations ids") {
        val apiClient: RealTimeApiClient = RealTimeApiClientStub(
            stationInformationFn = {
                StationInformationResponse(data = StationInformation(stations = listOf(station))).right()
            },
            stationStatusFn = {
                StationStatusResponse(data = StationStatus(stations = listOf(status.copy(stationId = "0002")))).right()
            }
        )

        testApplication {
            registerPlugins()

            routing { stationStatus(apiClient) }

            val response: HttpResponse = testHttpClient().get("/station-status")
            val content: ErrorMessage = response.body()

            response.status shouldBe InternalServerError
            content shouldBe ErrorMessage(
                description = "Internal Server Error",
                errors = listOf("Not all stations: [0001] have statuses: [0002]"),
                code = null
            )
        }
    }

    test("getStationsWithStatus returns OK response with content") {
        val expectedResponse = StationAndStatus(
            id = station.stationId,
            info = StationInfo(
                name = station.name,
                address = station.address,
                capacity = station.capacity,
                location = Location(lat = Latitude(station.lat), Longitude(station.lon))
            ),
            status = StandStatus(
                installed = status.installed,
                renting = status.renting,
                returning = status.returning,
                reported = status.reported,
                numBikesAvailable = status.numBikesAvailable,
                numDocksAvailable = status.numDocksAvailable
            )
        )

        val apiClient: RealTimeApiClient = object : RealTimeApiClient {
            override suspend fun stationInformation(): ErrorOr<StationInformationResponse> =
                StationInformationResponse(data = StationInformation(stations = listOf(station))).right()

            override suspend fun stationStatus(): ErrorOr<StationStatusResponse> =
                StationStatusResponse(data = StationStatus(stations = listOf(status))).right()
        }

        testApplication {
            registerPlugins()

            routing { stationStatus(apiClient) }

            val response: HttpResponse = testHttpClient().get("/station-status")
            val content: List<StationAndStatus> = response.body()

            response.status shouldBe OK
            content shouldBe listOf(expectedResponse)
        }
    }
})
