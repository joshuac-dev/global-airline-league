package com.gal.persistence.airport

import com.gal.core.AirportId
import com.gal.core.airport.CountryCode
import org.jetbrains.exposed.sql.Table

/**
 * Exposed table definition for airports.
 */
object Airports : Table("airports") {
    val id = integer("id").autoIncrement()
    val iata = char("iata", 3).nullable()
    val icao = char("icao", 4).nullable()
    val name = text("name")
    val city = text("city")
    val countryCode = char("country_code", 2)
    val latitude = double("latitude")
    val longitude = double("longitude")
    val elevationM = integer("elevation_m").nullable()
    val timezone = text("timezone").nullable()
    val size = integer("size").nullable()

    override val primaryKey = PrimaryKey(id)
}
