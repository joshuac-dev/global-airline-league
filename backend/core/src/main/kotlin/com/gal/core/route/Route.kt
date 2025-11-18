package com.gal.core.route

import com.gal.core.AirlineId
import com.gal.core.AirportId
import com.gal.core.RouteId
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Route domain entity.
 * Represents a direct connection between two airports operated by an airline.
 * 
 * This is a minimal skeleton that establishes route identity and basic constraints.
 * Future enhancements will include:
 * - TODO: Pricing configuration (base price, dynamic pricing rules)
 * - TODO: Capacity allocation (seats per flight, frequency)
 * - TODO: Flight time calculation and scheduling
 * - TODO: Load factor and occupancy metrics
 * - TODO: Revenue and cost tracking
 * - TODO: Seasonal adjustments
 */
@Serializable
data class Route(
    val id: RouteId,
    val airlineId: AirlineId,
    val originAirportId: AirportId,
    val destinationAirportId: AirportId,
    val distanceKm: Int,
    val createdAtEpochSeconds: Long
) {
    init {
        require(originAirportId != destinationAirportId) {
            "Origin and destination airports must be different"
        }
        require(distanceKm > 0) {
            "Distance must be positive"
        }
        require(createdAtEpochSeconds >= 0) {
            "Created at timestamp must be non-negative"
        }
    }
    
    /**
     * Get the createdAt timestamp as an Instant.
     */
    val createdAt: Instant
        get() = Instant.ofEpochSecond(createdAtEpochSeconds)
}

/**
 * Validates that a route can be created between two airports.
 * 
 * @param originId Origin airport ID
 * @param destinationId Destination airport ID
 * @return ValidationResult indicating success or failure
 */
fun validateRouteEndpoints(originId: AirportId, destinationId: AirportId): RouteValidationResult {
    if (originId == destinationId) {
        return RouteValidationResult.Failure("Origin and destination airports must be different")
    }
    return RouteValidationResult.Success
}

/**
 * Result type for route validation operations.
 */
sealed class RouteValidationResult {
    object Success : RouteValidationResult()
    data class Failure(val message: String) : RouteValidationResult()
    
    fun isSuccess(): Boolean = this is Success
    fun getOrThrow(): Unit = when (this) {
        is Success -> Unit
        is Failure -> throw IllegalArgumentException(message)
    }
}
