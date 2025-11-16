package com.gal.persistence.airport

import com.gal.core.airport.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Data class representing a row to be inserted into the airports table.
 * Uses nullable types for optional fields to match the database schema.
 */
data class AirportRow(
    val name: String,
    val iata: String?,
    val icao: String?,
    val city: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    val elevationM: Int?,
    val timezone: String?,
    val size: Int?
)

/**
 * Bulk operations repository for airports.
 * Provides batch insert functionality for efficient data import.
 */
class AirportsBulkRepository {

    /**
     * Insert a batch of airport rows.
     * @param rows List of airport rows to insert
     * @return Number of rows inserted
     */
    suspend fun insertBatch(rows: List<AirportRow>): Int = dbQuery {
        if (rows.isEmpty()) return@dbQuery 0
        
        Airports.batchInsert(rows) { row ->
            this[Airports.name] = row.name
            this[Airports.iata] = row.iata
            this[Airports.icao] = row.icao
            this[Airports.city] = row.city
            this[Airports.countryCode] = row.countryCode
            this[Airports.latitude] = row.latitude
            this[Airports.longitude] = row.longitude
            this[Airports.elevationM] = row.elevationM
            this[Airports.timezone] = row.timezone
            this[Airports.size] = row.size
        }.size
    }

    /**
     * Truncate the airports table (delete all rows).
     * Use with caution - this is a destructive operation.
     */
    suspend fun truncateAirports() = dbQuery {
        Airports.deleteWhere { Airports.id greater 0 }
    }

    /**
     * Check if the airports table is empty.
     * @return true if the table has no rows, false otherwise
     */
    suspend fun isEmpty(): Boolean = dbQuery {
        Airports.selectAll().limit(1).empty()
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
