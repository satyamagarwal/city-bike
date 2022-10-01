package city.bike.status.realtime

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

@JvmInline
value class Latitude(private val lat: Double)

@JvmInline
value class Longitude(private val lon: Double)

data class Location(val lat: Latitude, val long: Longitude)

data class StandStatus(
    val installed: Int,
    val renting: Int,
    val returning: Int,
    val reported: Instant,
    val numBikesAvailable: Int,
    val numDocksAvailable: Int
)

data class StationInfo(
    val name: String,
    val address: String,
    val capacity: Int,
    val location: Location
)

data class Station(
    @JsonProperty("station_id")
    val stationId: String,
    val name: String,
    val address: String,
    val capacity: Int,
    val lat: Double,
    val lon: Double
) {

    fun toStationInfo(): StationInfo = StationInfo(
        name = name,
        address = address,
        capacity = capacity,
        location = Location(Latitude(lat), Longitude(lon))
    )
}

data class Status(
    @JsonProperty("station_id")
    val stationId: String,
    @JsonProperty("is_installed")
    val installed: Int,
    @JsonProperty("is_renting")
    val renting: Int,
    @JsonProperty("is_returning")
    val returning: Int,
    @JsonProperty("last_reported")
    val reported: Instant,
    @JsonProperty("num_bikes_available")
    val numBikesAvailable: Int,
    @JsonProperty("num_docks_available")
    val numDocksAvailable: Int
) {

    fun toStandStatus(): StandStatus = StandStatus(
        installed = installed,
        renting = renting,
        returning = returning,
        reported = reported,
        numBikesAvailable = numBikesAvailable,
        numDocksAvailable = numDocksAvailable
    )
}

data class StationInformation(val stations: List<Station>)

data class StationStatus(val stations: List<Status>)

data class StationInformationResponse(val data: StationInformation)

data class StationStatusResponse(val data: StationStatus)

data class StationAndStatus(
    val id: String,
    val info: StationInfo,
    val status: StandStatus
)
