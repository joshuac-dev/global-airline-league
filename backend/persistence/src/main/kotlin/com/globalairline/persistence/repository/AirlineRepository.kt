package com.globalairline.persistence.repository

import com.globalairline.core.domain.Airline

/**
 * Repository interface for Airline operations.
 */
interface AirlineRepository {
    suspend fun findAll(): List<Airline>
    suspend fun findById(id: Int): Airline?
}

/**
 * Default implementation of AirlineRepository using Exposed.
 */
class AirlineRepositoryImpl : AirlineRepository {
    override suspend fun findAll(): List<Airline> {
        // TODO: Implement using Exposed queries
        return emptyList()
    }

    override suspend fun findById(id: Int): Airline? {
        // TODO: Implement using Exposed queries
        return null
    }
}
