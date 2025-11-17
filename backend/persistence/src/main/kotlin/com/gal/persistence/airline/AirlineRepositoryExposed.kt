package com.gal.persistence.airline

import com.gal.core.AirlineId
import com.gal.core.airline.Airline
import com.gal.core.airline.validateAirlineName
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

/**
 * Exposed-based implementation of AirlineRepository.
 */
class AirlineRepositoryExposed : AirlineRepository {

    override suspend fun create(name: String): Result<Airline> = dbQuery {
        try {
            // Validate name
            val validationResult = validateAirlineName(name)
            if (!validationResult.isSuccess()) {
                return@dbQuery Result.failure(IllegalArgumentException("Invalid airline name"))
            }
            
            // Insert the airline
            val id = Airlines.insert {
                it[Airlines.name] = name
                it[Airlines.createdAt] = Instant.now()
            } get Airlines.id
            
            // Fetch and return the created airline
            val airline = Airlines.selectAll().where { Airlines.id eq id }
                .map { toAirline(it) }
                .single()
            
            Result.success(airline)
        } catch (e: ExposedSQLException) {
            // Check if it's a unique constraint violation
            if (e.message?.contains("airlines_name_lower_uniq") == true) {
                Result.failure(IllegalArgumentException("Airline name already exists"))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun get(id: AirlineId): Airline? = dbQuery {
        Airlines.selectAll().where { Airlines.id eq id.value.toInt() }
            .map { toAirline(it) }
            .singleOrNull()
    }

    override suspend fun list(
        offset: Int,
        limit: Int
    ): List<Airline> = dbQuery {
        Airlines.selectAll()
            .orderBy(Airlines.id)
            .limit(limit, offset.toLong())
            .map { toAirline(it) }
    }

    private fun toAirline(row: ResultRow): Airline {
        return Airline(
            id = AirlineId(row[Airlines.id].toLong()),
            name = row[Airlines.name],
            createdAtEpochSeconds = row[Airlines.createdAt].epochSecond
        )
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
