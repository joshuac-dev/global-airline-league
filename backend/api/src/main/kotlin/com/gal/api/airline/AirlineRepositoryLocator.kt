package com.gal.api.airline

import com.gal.persistence.airline.AirlineRepository
import com.gal.persistence.airline.AirlineRepositoryExposed

/**
 * Simple repository locator for airline dependency wiring.
 * This can be replaced with a DI framework if needed in the future.
 */
object AirlineRepositoryLocator {
    private var _airlineRepository: AirlineRepository? = null
    
    /**
     * Get the airline repository instance.
     * Returns null if database is not initialized.
     */
    fun getAirlineRepository(): AirlineRepository? = _airlineRepository
    
    /**
     * Initialize repositories after database is ready.
     */
    fun initialize() {
        _airlineRepository = AirlineRepositoryExposed()
    }
    
    /**
     * Clear repositories (for testing).
     */
    fun clear() {
        _airlineRepository = null
    }
    
    /**
     * Set a custom airline repository (for testing).
     */
    fun setAirlineRepository(repository: AirlineRepository) {
        _airlineRepository = repository
    }
}
