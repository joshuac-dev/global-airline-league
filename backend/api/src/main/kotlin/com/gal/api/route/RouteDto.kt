package com.gal.api.route

import com.gal.core.route.Route
import kotlinx.serialization.Serializable

/**
 * Request DTO for creating a route.
 */
@Serializable
data class RouteCreateRequest(
    val airlineId: Int,
    val originAirportId: Int,
    val destinationAirportId: Int
)

/**
 * Response DTO for route data.
 */
@Serializable
data class RouteResponse(
    val id: Long,
    val airlineId: Long,
    val originAirportId: Long,
    val destinationAirportId: Long,
    val distanceKm: Int,
    val createdAt: Long
)

/**
 * Error response for consistent error handling.
 */
@Serializable
data class ErrorResponse(
    val error: ErrorDetails
)

@Serializable
data class ErrorDetails(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
)

/**
 * Map domain Route to RouteResponse DTO.
 */
fun Route.toResponse(): RouteResponse = RouteResponse(
    id = id.value,
    airlineId = airlineId.value,
    originAirportId = originAirportId.value,
    destinationAirportId = destinationAirportId.value,
    distanceKm = distanceKm,
    createdAt = createdAtEpochSeconds
)
