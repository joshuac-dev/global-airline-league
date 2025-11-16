package com.globalairline.core.domain

/**
 * Represents an airport in the simulation.
 */
data class Airport(
    val id: Int,
    val iata: String,
    val icao: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String
)
