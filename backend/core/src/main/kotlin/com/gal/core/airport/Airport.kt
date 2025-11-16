package com.gal.core.airport

import com.gal.core.AirportId
import kotlinx.serialization.Serializable

/**
 * ISO 3166-1 alpha-2 country code.
 */
@Serializable
@JvmInline
value class CountryCode(val value: String) {
    init {
        require(value.length == 2 && value.all { it.isUpperCase() }) {
            "Country code must be 2 uppercase letters"
        }
    }
}

/**
 * IATA airport code (3 letters).
 */
@Serializable
@JvmInline
value class IATA(val value: String) {
    init {
        require(value.length == 3 && value.all { it.isUpperCase() }) {
            "IATA code must be 3 uppercase letters"
        }
    }
}

/**
 * ICAO airport code (4 letters/digits).
 */
@Serializable
@JvmInline
value class ICAO(val value: String) {
    init {
        require(value.length == 4 && value.all { it.isUpperCase() || it.isDigit() }) {
            "ICAO code must be 4 uppercase alphanumeric characters"
        }
    }
}

/**
 * Airport domain entity.
 * Represents a physical airport with geographic and operational characteristics.
 */
@Serializable
data class Airport(
    val id: AirportId,
    val name: String,
    val iata: IATA?,
    val icao: ICAO?,
    val city: String,
    val countryCode: CountryCode,
    val latitude: Double,
    val longitude: Double,
    val elevationM: Int? = null,
    val timezone: String? = null,
    val size: Int? = null
) {
    init {
        require(name.isNotBlank()) { "Airport name cannot be blank" }
        require(city.isNotBlank()) { "City name cannot be blank" }
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180" }
    }
}
