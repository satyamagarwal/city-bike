package city.bike.status.realtime

import arrow.core.Nel
import arrow.core.filterOrOther
import arrow.core.flatMap
import arrow.core.left
import arrow.core.nel
import arrow.core.right
import arrow.core.toNonEmptyListOrNull
import arrow.fx.coroutines.parZip
import city.bike.status.app.DomainError.LogicalError
import city.bike.status.app.ErrorOr
import city.bike.status.app.send
import city.bike.status.app.toResponse
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get

fun Routing.stationStatus(
    realTimeApiClient: RealTimeApiClient = RealTimeApiClientInstance.instance
): Route = get("/station-status") {
    RealTimeApiRoute
        .getStationsWithStatus(realTimeApiClient)
        .toResponse()
        .send(call)
}

object RealTimeApiRoute {

    suspend fun getStationsWithStatus(apiClient: RealTimeApiClient): ErrorOr<Nel<StationAndStatus>> = parZip(
        { apiClient.stationInformation() },
        { apiClient.stationStatus() },
        { info, status ->
            info.flatMap { i -> status.map { s -> i to s } }
        }
    )
        .map { (info, status) ->
            val infoById: Map<String, Station> = info.data.stations.associateBy { it.stationId }
            val statusById: Map<String, Status> = status.data.stations.associateBy { it.stationId }

            infoById to statusById
        }
        .filterOrOther(
            { (infoById, statusById) ->
                infoById.keys.containsAll(statusById.keys) && infoById.size == statusById.size
            },
            { (infoById, statusById) ->
                LogicalError("Not all stations: ${infoById.keys} have statuses: ${statusById.keys}".nel())
            }
        )
        .flatMap { (infoById, statusById) ->
            infoById
                .mapNotNull { (id, station) ->
                    statusById[id]?.let { StationAndStatus(id, station.toStationInfo(), it.toStandStatus()) }
                }
                .toNonEmptyListOrNull()
                ?.right()
                ?: LogicalError("Server sent either empty or invalid data".nel()).left()
        }
}
