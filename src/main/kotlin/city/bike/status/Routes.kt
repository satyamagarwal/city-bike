package city.bike.status

import arrow.core.right
import city.bike.status.app.send
import city.bike.status.app.toResponse
import city.bike.status.realtime.RealTimeApiClientInstance
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.livelinessProbe(): Route = get("/liveness") {
    "ok".right().toResponse().send(call)
}

fun Route.getStationsWithStatus(): Route = get("/station-status") {
    RealTimeApiClientInstance.instance.getStationInfoAndStatus().toResponse().send(call)
}
