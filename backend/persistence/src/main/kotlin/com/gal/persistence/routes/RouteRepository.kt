package com.gal.persistence.routes

import com.gal.core.AirlineId
import com.gal.core.AirportId
import com.gal.core.RouteId
import com.gal.core.route.Route

/**
 * Repository interface for route data access.
 */
interface RouteRepository {
    /**
     * Create a new route connecting two airports for an airline.
     * 
     * @param airlineId The airline operating the route
     * @param originAirportId The origin airport
     * @param destinationAirportId The destination airport
     * @return Result containing the created Route or an error
     * 
     * Note: The distance is calculated automatically using airport coordinates.
     * Validates that:
     * - The airline exists
     * - Both airports exist
     * - Origin and destination are different
     * - No duplicate route exists for this airline on this origin→destination pair
     */
    suspend fun create(
        airlineId: AirlineId,
        originAirportId: AirportId,
        destinationAirportId: AirportId
    ): Result<Route>
    
    /**
     * Get a route by its ID.
     * @return Route if found, null otherwise
     */
    suspend fun get(id: RouteId): Route?
    
    /**
     * List all routes for a specific airline with pagination.
     * @param airlineId The airline ID
     * @param offset Starting position (0-based)
     * @param limit Maximum number of results
     * @return List of routes
     */
    suspend fun listByAirline(
        airlineId: AirlineId,
        offset: Int = 0,
        limit: Int = 50
    ): List<Route>
    
    /**
     * Delete a route by its ID.
     * @return true if the route was deleted, false if it didn't exist
     */
    suspend fun delete(id: RouteId): Boolean
    
    /**
     * List all routes with pagination.
     * @param offset Starting position (0-based)
     * @param limit Maximum number of results
     * @return List of routes
     */
    suspend fun listAll(
        offset: Int = 0,
        limit: Int = 50
    ): List<Route>
    
    /**
     * List all routes that touch a specific airport (as origin or destination).
     * @param airportId The airport ID
     * @param offset Starting position (0-based)
     * @param limit Maximum number of results
     * @return List of routes
     */
    suspend fun listByAirport(
        airportId: AirportId,
        offset: Int = 0,
        limit: Int = 50
    ): List<Route>
}
