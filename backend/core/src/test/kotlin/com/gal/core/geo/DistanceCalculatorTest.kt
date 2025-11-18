package com.gal.core.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DistanceCalculatorTest {
    
    @Test
    fun testGreatCircleDistanceNewYorkToLondon() {
        // JFK coordinates: 40.6413° N, 73.7781° W
        // LHR coordinates: 51.4700° N, 0.4543° W
        val distance = DistanceCalculator.greatCircleKm(40.6413, -73.7781, 51.4700, -0.4543)
        
        // Expected distance is approximately 5540 km
        assertEquals(5540, distance, "Distance from JFK to LHR should be approximately 5540 km")
    }
    
    @Test
    fun testGreatCircleDistanceSydneyToLosAngeles() {
        // SYD coordinates: 33.9461° S, 151.1772° E
        // LAX coordinates: 33.9416° N, 118.4085° W
        val distance = DistanceCalculator.greatCircleKm(-33.9461, 151.1772, 33.9416, -118.4085)
        
        // Expected distance is approximately 12061 km
        assertEquals(12061, distance, "Distance from SYD to LAX should be approximately 12061 km")
    }
    
    @Test
    fun testGreatCircleDistanceSameLocation() {
        // Distance from a point to itself should be 0
        val distance = DistanceCalculator.greatCircleKm(40.7128, -74.0060, 40.7128, -74.0060)
        assertEquals(0, distance, "Distance from a point to itself should be 0")
    }
    
    @Test
    fun testGreatCircleDistanceShortDistance() {
        // Test a short distance: Paris CDG to Paris Orly (about 34 km)
        // CDG: 49.0097° N, 2.5479° E
        // ORY: 48.7233° N, 2.3794° E
        val distance = DistanceCalculator.greatCircleKm(49.0097, 2.5479, 48.7233, 2.3794)
        
        // Expected distance is approximately 34 km
        assertEquals(34, distance, "Distance from CDG to ORY should be approximately 34 km")
    }
    
    @Test
    fun testGreatCircleDistanceInvalidLatitude() {
        assertFailsWith<IllegalArgumentException> {
            DistanceCalculator.greatCircleKm(91.0, 0.0, 0.0, 0.0)
        }
        
        assertFailsWith<IllegalArgumentException> {
            DistanceCalculator.greatCircleKm(-91.0, 0.0, 0.0, 0.0)
        }
        
        assertFailsWith<IllegalArgumentException> {
            DistanceCalculator.greatCircleKm(0.0, 0.0, 91.0, 0.0)
        }
        
        assertFailsWith<IllegalArgumentException> {
            DistanceCalculator.greatCircleKm(0.0, 0.0, -91.0, 0.0)
        }
    }
    
    @Test
    fun testGreatCircleDistanceInvalidLongitude() {
        assertFailsWith<IllegalArgumentException> {
            DistanceCalculator.greatCircleKm(0.0, 181.0, 0.0, 0.0)
        }
        
        assertFailsWith<IllegalArgumentException> {
            DistanceCalculator.greatCircleKm(0.0, -181.0, 0.0, 0.0)
        }
        
        assertFailsWith<IllegalArgumentException> {
            DistanceCalculator.greatCircleKm(0.0, 0.0, 0.0, 181.0)
        }
        
        assertFailsWith<IllegalArgumentException> {
            DistanceCalculator.greatCircleKm(0.0, 0.0, 0.0, -181.0)
        }
    }
    
    @Test
    fun testGreatCircleDistanceAcrossPrimeMeridian() {
        // Test distance calculation across prime meridian
        // London to Paris (about 347 km)
        // LHR: 51.4700° N, 0.4543° W
        // CDG: 49.0097° N, 2.5479° E
        val distance = DistanceCalculator.greatCircleKm(51.4700, -0.4543, 49.0097, 2.5479)
        
        // Expected distance is approximately 347 km
        assertEquals(347, distance, "Distance from LHR to CDG should be approximately 347 km")
    }
    
    @Test
    fun testGreatCircleDistanceAcrossDateLine() {
        // Test distance calculation across international date line
        // Tokyo Narita to Honolulu (about 6136 km)
        // NRT: 35.7647° N, 140.3864° E
        // HNL: 21.3187° N, 157.9225° W
        val distance = DistanceCalculator.greatCircleKm(35.7647, 140.3864, 21.3187, -157.9225)
        
        // Expected distance is approximately 6136 km
        assertEquals(6136, distance, "Distance from NRT to HNL should be approximately 6136 km")
    }
}
