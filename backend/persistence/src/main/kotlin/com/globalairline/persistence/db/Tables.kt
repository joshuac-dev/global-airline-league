package com.globalairline.persistence.db

import org.jetbrains.exposed.sql.Table

/**
 * Exposed table definition for airports.
 */
object Airports : Table("airports") {
    val id = integer("id").autoIncrement()
    val iata = text("iata")
    val icao = text("icao")
    val name = text("name")
    val latitude = double("latitude")
    val longitude = double("longitude")
    val country = text("country")
    
    override val primaryKey = PrimaryKey(id)
}

/**
 * Exposed table definition for airlines.
 */
object Airlines : Table("airlines") {
    val id = integer("id").autoIncrement()
    val name = text("name")
    val reputation = integer("reputation").default(0)
    
    override val primaryKey = PrimaryKey(id)
}
