package com.gal.persistence.airline

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * Exposed table definition for airlines.
 */
object Airlines : Table("airlines") {
    val id = integer("id").autoIncrement()
    val name = text("name")
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
