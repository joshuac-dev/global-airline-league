package com.gal.api.airport

import com.gal.persistence.airport.AirportRepository
import com.gal.persistence.airport.AirportRepositoryExposed

/**
 * Simple repository locator for dependency wiring.
 * This can be replaced with a DI framework if needed in the future.
 */
object RepositoryLocator {
    private var _airportRepository: AirportRepository? = null
    
    /**
     * Get the airport repository instance.
     * Returns null if database is not initialized.
     */
    fun getAirportRepository(): AirportRepository? = _airportRepository
    
    /**
     * Initialize repositories after database is ready.
     */
    fun initialize() {
        _airportRepository = AirportRepositoryExposed()
    }
    
    /**
     * Clear repositories (for testing).
     */
    fun clear() {
        _airportRepository = null
    }
    
    /**
     * Set a custom airport repository (for testing).
     */
    fun setAirportRepository(repository: AirportRepository) {
        _airportRepository = repository
    }
}
