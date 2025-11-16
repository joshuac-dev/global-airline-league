package com.globalairline.core.service

/**
 * Manages the game simulation clock and tick cycles.
 * Placeholder for future tick/cycle management.
 */
class SimulationClock {
    private var currentCycle: Long = 0

    fun getCurrentCycle(): Long = currentCycle

    fun incrementCycle() {
        currentCycle++
    }

    fun reset() {
        currentCycle = 0
    }
}
