package com.globalairline.core.service

/**
 * Interface for demand modeling.
 * Placeholder for future demand calculation logic.
 */
interface DemandModel {
    fun calculateDemand(fromAirportId: Int, toAirportId: Int): Double
}

/**
 * No-op implementation of DemandModel.
 */
class NoOpDemandModel : DemandModel {
    override fun calculateDemand(fromAirportId: Int, toAirportId: Int): Double = 0.0
}
