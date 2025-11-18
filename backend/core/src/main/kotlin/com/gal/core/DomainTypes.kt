package com.gal.core

import kotlinx.serialization.Serializable

/**
 * Represents the game simulation clock/cycle.
 * Each tick advances the game state forward.
 */
@Serializable
@JvmInline
value class GameClock(val cycle: Long) {
    init {
        require(cycle >= 0) { "Game cycle cannot be negative" }
    }

    fun next(): GameClock = GameClock(cycle + 1)

    operator fun compareTo(other: GameClock): Int = cycle.compareTo(other.cycle)
}

/**
 * Type-safe airline identifier.
 */
@Serializable
@JvmInline
value class AirlineId(val value: Long) {
    init {
        require(value > 0) { "Airline ID must be positive" }
    }
}

/**
 * Type-safe airport identifier.
 */
@Serializable
@JvmInline
value class AirportId(val value: Long) {
    init {
        require(value > 0) { "Airport ID must be positive" }
    }
}

/**
 * Type-safe route identifier.
 */
@Serializable
@JvmInline
value class RouteId(val value: Long) {
    init {
        require(value > 0) { "Route ID must be positive" }
    }
}
