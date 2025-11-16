package com.globalairline.core.domain

/**
 * Represents an airline in the simulation.
 */
data class Airline(
    val id: Int,
    val name: String,
    val reputation: Int = 0
) {
    init {
        require(reputation >= 0) { "Reputation cannot be negative" }
    }
}
