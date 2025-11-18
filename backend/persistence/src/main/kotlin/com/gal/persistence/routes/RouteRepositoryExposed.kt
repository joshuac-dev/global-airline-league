package com.gal.persistence.routes

import com.gal.core.AirlineId
import com.gal.core.AirportId
import com.gal.core.RouteId
import com.gal.core.geo.DistanceCalculator
import com.gal.core.route.Route
import com.gal.core.route.validateRouteEndpoints
import com.gal.persistence.airline.Airlines
import com.gal.persistence.airport.Airports
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

/**
 * Exposed-based implementation of RouteRepository.
 */
class RouteRepositoryExposed : RouteRepository {

    override suspend fun create(
        airlineId: AirlineId,
        originAirportId: AirportId,
        destinationAirportId: AirportId
    ): Result<Route> = dbQuery {
        try {
            // Validate endpoints
            val validation = validateRouteEndpoints(originAirportId, destinationAirportId)
            if (!validation.isSuccess()) {
                return@dbQuery Result.failure(IllegalArgumentException("Invalid route endpoints"))
            }
            
            // Check if airline exists
            val airlineExists = Airlines.selectAll()
                .where { Airlines.id eq airlineId.value.toInt() }
                .count() > 0
            
            if (!airlineExists) {
                return@dbQuery Result.failure(IllegalArgumentException("Airline not found"))
            }
            
            // Get origin airport
            val originAirport = Airports.selectAll()
                .where { Airports.id eq originAirportId.value.toInt() }
                .singleOrNull()
            
            if (originAirport == null) {
                return@dbQuery Result.failure(IllegalArgumentException("Origin airport not found"))
            }
            
            // Get destination airport
            val destinationAirport = Airports.selectAll()
                .where { Airports.id eq destinationAirportId.value.toInt() }
                .singleOrNull()
            
            if (destinationAirport == null) {
                return@dbQuery Result.failure(IllegalArgumentException("Destination airport not found"))
            }
            
            // Calculate distance
            val originLat = originAirport[Airports.latitude]
            val originLon = originAirport[Airports.longitude]
            val destLat = destinationAirport[Airports.latitude]
            val destLon = destinationAirport[Airports.longitude]
            
            val distanceKm = DistanceCalculator.greatCircleKm(originLat, originLon, destLat, destLon)
            
            // Insert the route
            val id = Routes.insert {
                it[Routes.airlineId] = airlineId.value.toInt()
                it[Routes.originAirportId] = originAirportId.value.toInt()
                it[Routes.destinationAirportId] = destinationAirportId.value.toInt()
                it[Routes.distanceKm] = distanceKm
                it[Routes.createdAt] = Instant.now()
            } get Routes.id
            
            // Fetch and return the created route
            val route = Routes.selectAll().where { Routes.id eq id }
                .map { toRoute(it) }
                .single()
            
            Result.success(route)
        } catch (e: ExposedSQLException) {
            // Check if it's a unique constraint violation
            if (e.message?.contains("routes_unique_airline_origin_dest") == true) {
                Result.failure(IllegalArgumentException("Route already exists for this airline"))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun get(id: RouteId): Route? = dbQuery {
        Routes.selectAll().where { Routes.id eq id.value.toInt() }
            .map { toRoute(it) }
            .singleOrNull()
    }

    override suspend fun listByAirline(
        airlineId: AirlineId,
        offset: Int,
        limit: Int
    ): List<Route> = dbQuery {
        Routes.selectAll()
            .where { Routes.airlineId eq airlineId.value.toInt() }
            .orderBy(Routes.id)
            .limit(limit, offset.toLong())
            .map { toRoute(it) }
    }

    override suspend fun delete(id: RouteId): Boolean = dbQuery {
        Routes.deleteWhere { Routes.id eq id.value.toInt() } > 0
    }

    private fun toRoute(row: ResultRow): Route {
        return Route(
            id = RouteId(row[Routes.id].toLong()),
            airlineId = AirlineId(row[Routes.airlineId].toLong()),
            originAirportId = AirportId(row[Routes.originAirportId].toLong()),
            destinationAirportId = AirportId(row[Routes.destinationAirportId].toLong()),
            distanceKm = row[Routes.distanceKm],
            createdAtEpochSeconds = row[Routes.createdAt].epochSecond
        )
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
