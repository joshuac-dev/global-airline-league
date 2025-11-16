package com.gal.persistence.airport

import com.gal.core.AirportId
import com.gal.core.airport.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Exposed-based implementation of AirportRepository.
 * Uses ILIKE for search (FTS implementation deferred to avoid complexity in initial implementation).
 */
class AirportRepositoryExposed : AirportRepository {

    override suspend fun get(id: AirportId): Airport? = dbQuery {
        Airports.selectAll().where { Airports.id eq id.value.toInt() }
            .map { toAirport(it) }
            .singleOrNull()
    }

    override suspend fun list(
        offset: Int,
        limit: Int,
        country: CountryCode?
    ): List<Airport> = dbQuery {
        val query = if (country != null) {
            Airports.selectAll().where { Airports.countryCode eq country.value }
        } else {
            Airports.selectAll()
        }
        
        query.orderBy(Airports.id)
            .limit(limit, offset.toLong())
            .map { toAirport(it) }
    }

    override suspend fun search(query: String, limit: Int): List<Airport> = dbQuery {
        searchWithILike(query, limit)
    }

    private fun searchWithILike(query: String, limit: Int): List<Airport> {
        val pattern = "%$query%"
        val lowerPattern = pattern.lowercase()
        
        return Airports.selectAll().where {
            (Airports.name.lowerCase() like lowerPattern) or
            (Airports.city.lowerCase() like lowerPattern) or
            (Airports.iata.isNotNull() and (Airports.iata.lowerCase() like lowerPattern)) or
            (Airports.icao.isNotNull() and (Airports.icao.lowerCase() like lowerPattern))
        }
            .orderBy(Airports.name)
            .limit(limit)
            .map { toAirport(it) }
    }

    private fun toAirport(row: ResultRow): Airport {
        return Airport(
            id = AirportId(row[Airports.id].toLong()),
            name = row[Airports.name],
            iata = row[Airports.iata]?.trim()?.takeIf { it.isNotBlank() }?.let { IATA(it) },
            icao = row[Airports.icao]?.trim()?.takeIf { it.isNotBlank() }?.let { ICAO(it) },
            city = row[Airports.city],
            countryCode = CountryCode(row[Airports.countryCode].trim()),
            latitude = row[Airports.latitude],
            longitude = row[Airports.longitude],
            elevationM = row[Airports.elevationM],
            timezone = row[Airports.timezone],
            size = row[Airports.size]
        )
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
