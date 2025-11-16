package com.globalairline.persistence.repository

import com.globalairline.core.domain.Airport

/**
 * Repository interface for Airport operations.
 */
interface AirportRepository {
    suspend fun findAll(): List<Airport>
    suspend fun findById(id: Int): Airport?
    suspend fun findByIata(iata: String): Airport?
}

/**
 * Default implementation of AirportRepository using Exposed.
 */
class AirportRepositoryImpl : AirportRepository {
    override suspend fun findAll(): List<Airport> {
        // TODO: Implement using Exposed queries
        return emptyList()
    }

    override suspend fun findById(id: Int): Airport? {
        // TODO: Implement using Exposed queries
        return null
    }

    override suspend fun findByIata(iata: String): Airport? {
        // TODO: Implement using Exposed queries
        return null
    }
}
