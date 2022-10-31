package city.bike.status.realtime

import city.bike.status.app.ErrorOr
import city.bike.status.http.HttpClientTimeoutInput
import city.bike.status.http.HttpCommand
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.contentType

interface RealTimeApiClient {

    suspend fun stationInformation(): ErrorOr<StationInformationResponse>

    suspend fun stationStatus(): ErrorOr<StationStatusResponse>
}

class RealTimeApiClientInstance(
    private val commandStationStatus: HttpCommand,
    private val commandStationInfo: HttpCommand
) : RealTimeApiClient {

    override suspend fun stationInformation(): ErrorOr<StationInformationResponse> {
        val request: HttpRequestBuilder = HttpRequestBuilder()
            .apply {
                url("$apiBaseUrl$stationInfo")
                method = Get
                contentType(Json)
            }

        return commandStationInfo.execute(request)
    }

    override suspend fun stationStatus(): ErrorOr<StationStatusResponse> {
        val request: HttpRequestBuilder = HttpRequestBuilder()
            .apply {
                url("$apiBaseUrl$stationStatus")
                method = Get
                contentType(Json)
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
                httpClientTimeoutInput = HttpClientTimeoutInput(connectionTimeout = 1000, requestTimeout = 2000)
            ),
            commandStationStatus = HttpCommand(
                commandKey = "StationInformation",
                httpClientTimeoutInput = HttpClientTimeoutInput(connectionTimeout = 1000, requestTimeout = 2000)
            )
        )
    }
}
