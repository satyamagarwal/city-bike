package city.bike.status.realtime

import arrow.core.Nel
import arrow.core.flatMap
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import arrow.core.toNonEmptyListOrNull
import arrow.fx.coroutines.parZip
import city.bike.status.app.DomainError.InvalidData
import city.bike.status.app.ErrorOr
import city.bike.status.http.HttpClientTimeoutInput
import city.bike.status.http.HttpCommand
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType

interface RealTimeApiClient {

    suspend fun getStationInfoAndStatus(): ErrorOr<Nel<StationAndStatus>>
}

class RealTimeApiClientInstance(
    private val commandStationStatus: HttpCommand,
    private val commandStationInfo: HttpCommand
) : RealTimeApiClient {

    override suspend fun getStationInfoAndStatus(): ErrorOr<Nel<StationAndStatus>> {
        return parZip(
            { stationInformation() },
            { stationStatus() }
        ) { info, status -> info.flatMap { i -> status.map { s -> i to s } } }
            .flatMap { (info, status) ->
                val infoById: Map<String, Station> = info.data.stations.associateBy { it.stationId }
                val statusById: Map<String, Status> = status.data.stations.associateBy { it.stationId }

                infoById
                    .mapNotNull { (id, station) ->
                        statusById[id]?.let { StationAndStatus(id, station.toStationInfo(), it.toStandStatus()) }
                    }
                    .toNonEmptyListOrNull()
                    ?.right()
                    ?: InvalidData(nonEmptyListOf("Server sent either empty or invalid data")).left()
            }
    }

    private suspend fun stationInformation(): ErrorOr<StationInformationResponse> {
        val request: HttpRequestBuilder = HttpRequestBuilder()
            .apply {
                url("$apiBaseUrl$stationInfo")
                method = HttpMethod.Get
                contentType(ContentType.Application.Json)
            }

        return commandStationInfo.execute(request)
    }

    private suspend fun stationStatus(): ErrorOr<StationStatusResponse> {
        val request: HttpRequestBuilder = HttpRequestBuilder()
            .apply {
                url("$apiBaseUrl$stationStatus")
                method = HttpMethod.Get
                contentType(ContentType.Application.Json)
            }

        return commandStationStatus.execute(request)
    }

    companion object {
        private const val apiBaseUrl = "https://gbfs.urbansharing.com/oslobysykkel.no"
        private const val stationInfo = "/station_information.json"
        private const val stationStatus = "/station_status.json"

        val instance: RealTimeApiClient = RealTimeApiClientInstance(
            commandStationInfo = HttpCommand(
                commandKey = "StationInformation",
                HttpClientTimeoutInput(connectionTimeout = 1000, requestTimeout = 2000)
            ),
            commandStationStatus = HttpCommand(
                commandKey = "StationInformation",
                HttpClientTimeoutInput(connectionTimeout = 1000, requestTimeout = 2000)
            )
        )
    }
}
