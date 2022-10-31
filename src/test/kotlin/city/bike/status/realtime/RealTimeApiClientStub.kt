package city.bike.status.realtime

import city.bike.status.app.ErrorOr

data class RealTimeApiClientStub(
    private val stationInformationFn: () -> ErrorOr<StationInformationResponse>,
    private val stationStatusFn: () -> ErrorOr<StationStatusResponse>
) : RealTimeApiClient {

    override suspend fun stationInformation(): ErrorOr<StationInformationResponse> = stationInformationFn()

    override suspend fun stationStatus(): ErrorOr<StationStatusResponse> = stationStatusFn()
}
