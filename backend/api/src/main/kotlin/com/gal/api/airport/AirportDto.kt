package com.gal.api.airport

import com.gal.core.AirportId
import kotlinx.serialization.Serializable

/**
 * DTO for airport responses.
 */
@Serializable
data class AirportResponse(
    val id: Long,
    val name: String,
    val iata: String?,
    val icao: String?,
    val city: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * Convert domain Airport to API response.
 */
fun com.gal.core.airport.Airport.toResponse() = AirportResponse(
    id = id.value,
    name = name,
    iata = iata?.value,
    icao = icao?.value,
    city = city,
    countryCode = countryCode.value,
    latitude = latitude,
    longitude = longitude
)
