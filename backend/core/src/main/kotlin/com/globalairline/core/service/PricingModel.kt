package com.globalairline.core.service

/**
 * Interface for pricing model.
 * Placeholder for future pricing logic.
 */
interface PricingModel {
    fun calculatePrice(distance: Double, demand: Double): Double
}

/**
 * No-op implementation of PricingModel.
 */
class NoOpPricingModel : PricingModel {
    override fun calculatePrice(distance: Double, demand: Double): Double = 0.0
}
