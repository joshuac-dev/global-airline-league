package com.gal.core.geo

import kotlin.math.*

/**
 * Utility for calculating geographic distances between coordinates.
 */
object DistanceCalculator {
    private const val EARTH_RADIUS_KM = 6371.0
    
    /**
     * Calculate the great circle distance between two points using the Haversine formula.
     * 
     * @param lat1 Latitude of the first point in degrees
     * @param lon1 Longitude of the first point in degrees
     * @param lat2 Latitude of the second point in degrees
     * @param lon2 Longitude of the second point in degrees
     * @return Distance in kilometers
     */
    fun greatCircleKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        require(lat1 in -90.0..90.0) { "lat1 must be between -90 and 90" }
        require(lat2 in -90.0..90.0) { "lat2 must be between -90 and 90" }
        require(lon1 in -180.0..180.0) { "lon1 must be between -180 and 180" }
        require(lon2 in -180.0..180.0) { "lon2 must be between -180 and 180" }
        
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(deltaLat / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLon / 2).pow(2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        val distanceKm = EARTH_RADIUS_KM * c
        return distanceKm.roundToInt()
    }
}
