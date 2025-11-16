package com.gal.persistence.airline

import com.gal.core.AirlineId
import com.gal.core.airline.Airline

/**
 * Repository interface for airline data access.
 */
interface AirlineRepository {
    /**
     * Create a new airline with the given name.
     * @param name The airline name (must be valid and unique)
     * @return Result containing the created Airline or an error
     */
    suspend fun create(name: String): Result<Airline>
    
    /**
     * Get an airline by its ID.
     * @return Airline if found, null otherwise
     */
    suspend fun get(id: AirlineId): Airline?
    
    /**
     * List all airlines with pagination.
     * @param offset Starting position (0-based)
     * @param limit Maximum number of results
     * @return List of airlines
     */
    suspend fun list(
        offset: Int = 0,
        limit: Int = 50
    ): List<Airline>
}
