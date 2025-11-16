package com.gal.api.airline

import com.gal.core.AirlineId
import kotlinx.serialization.Serializable

/**
 * DTO for airline responses.
 */
@Serializable
data class AirlineResponse(
    val id: Long,
    val name: String,
    val createdAt: Long // epoch seconds
)

/**
 * Convert domain Airline to API response.
 */
fun com.gal.core.airline.Airline.toResponse() = AirlineResponse(
    id = id.value,
    name = name,
    createdAt = createdAtEpochSeconds
)
