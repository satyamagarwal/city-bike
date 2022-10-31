package city.bike.status.realtime

import arrow.core.Nel
import arrow.core.left
import arrow.core.nel
import arrow.core.right
import city.bike.status.app.DomainError.BackendFatalError
import city.bike.status.app.DomainError.LogicalError
import city.bike.status.app.ErrorOr
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.Instant

class RealTimeDomainTest : FunSpec({

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

    test("getStationsWithStatus returns BackendFatalError if server returns a fatal error") {
        val backendFatalError = BackendFatalError(RuntimeException("Internal Server Error"))

        RealTimeApiRoute
            .getStationsWithStatus(
                RealTimeApiClientStub(
                    stationInformationFn = { backendFatalError.left() },
                    stationStatusFn = { StationStatusResponse(data = StationStatus(stations = emptyList())).right() }
                )
            )
            .shouldBe(backendFatalError.left())

        RealTimeApiRoute
            .getStationsWithStatus(
                RealTimeApiClientStub(
                    stationInformationFn = {
                        StationInformationResponse(data = StationInformation(stations = emptyList())).right()
                    },
                    stationStatusFn = { backendFatalError.left() }
                )
            )
            .shouldBe(backendFatalError.left())
    }

    test("getStationsWithStatus returns LogicalError if server returns empty list") {
        val apiClient: RealTimeApiClient = RealTimeApiClientStub(
            stationInformationFn = {
                StationInformationResponse(data = StationInformation(stations = emptyList())).right()
            },
            stationStatusFn = { StationStatusResponse(data = StationStatus(stations = emptyList())).right() }
        )

        val response: ErrorOr<Nel<StationAndStatus>> = RealTimeApiRoute.getStationsWithStatus(apiClient)

        response shouldBe LogicalError("Server sent either empty or invalid data".nel()).left()
    }

    test("getStationsWithStatus returns LogicalError if server returns mismatched stations ids") {
        RealTimeApiRoute
            .getStationsWithStatus(
                RealTimeApiClientStub(
                    stationInformationFn = {
                        StationInformationResponse(data = StationInformation(stations = listOf(station))).right()
                    },
                    stationStatusFn = {
                        StationStatusResponse(data = StationStatus(stations = listOf(status.copy(stationId = "0002"))))
                            .right()
                    }
                )
            )
            .shouldBe(LogicalError("Not all stations: [0001] have statuses: [0002]".nel()).left())

        RealTimeApiRoute
            .getStationsWithStatus(
                RealTimeApiClientStub(
                    stationInformationFn = {
                        StationInformationResponse(
                            data = StationInformation(stations = listOf(station, station.copy(stationId = "0002")))
                        ).right()
                    },
                    stationStatusFn = { StationStatusResponse(data = StationStatus(stations = listOf(status))).right() }
                )
            )
            .shouldBe(LogicalError("Not all stations: [0001, 0002] have statuses: [0001]".nel()).left())
    }

    test("getStationsWithStatus returns proper response") {
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

        val apiClient: RealTimeApiClient = RealTimeApiClientStub(
            stationInformationFn = {
                StationInformationResponse(data = StationInformation(stations = listOf(station))).right()
            },
            stationStatusFn = { StationStatusResponse(data = StationStatus(stations = listOf(status))).right() }
        )

        RealTimeApiRoute.getStationsWithStatus(apiClient) shouldBe expectedResponse.nel().right()
    }
})
