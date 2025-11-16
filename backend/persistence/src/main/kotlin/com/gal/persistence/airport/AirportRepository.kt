package com.gal.persistence.airport

import com.gal.core.AirportId
import com.gal.core.airport.Airport
import com.gal.core.airport.CountryCode

/**
 * Repository interface for airport data access.
 */
interface AirportRepository {
    /**
     * Get an airport by its ID.
     * @return Airport if found, null otherwise
     */
    suspend fun get(id: AirportId): Airport?

    /**
     * List airports with pagination and optional country filter.
     * @param offset Starting position (0-based)
     * @param limit Maximum number of results
     * @param country Optional country code filter
     * @return List of airports
     */
    suspend fun list(
        offset: Int = 0,
        limit: Int = 50,
        country: CountryCode? = null
    ): List<Airport>

    /**
     * Search airports using full-text search or ILIKE fallback.
     * @param query Search query string
     * @param limit Maximum number of results (default 20)
     * @return List of airports ordered by relevance
     */
    suspend fun search(
        query: String,
        limit: Int = 20
    ): List<Airport>
}
