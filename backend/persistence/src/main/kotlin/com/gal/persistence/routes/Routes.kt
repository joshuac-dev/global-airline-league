package com.gal.persistence.routes

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * Exposed table definition for routes.
 */
object Routes : Table("routes") {
    val id = integer("id").autoIncrement()
    val airlineId = integer("airline_id")
    val originAirportId = integer("origin_airport_id")
    val destinationAirportId = integer("destination_airport_id")
    val distanceKm = integer("distance_km")
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
