package com.gal.core.route

import com.gal.core.AirlineId
import com.gal.core.AirportId
import com.gal.core.RouteId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RouteTest {
    
    @Test
    fun testValidRouteCreation() {
        val route = Route(
            id = RouteId(1),
            airlineId = AirlineId(1),
            originAirportId = AirportId(100),
            destinationAirportId = AirportId(200),
            distanceKm = 5571,
            createdAtEpochSeconds = 1234567890L
        )
        
        assertEquals(RouteId(1), route.id)
        assertEquals(AirlineId(1), route.airlineId)
        assertEquals(AirportId(100), route.originAirportId)
        assertEquals(AirportId(200), route.destinationAirportId)
        assertEquals(5571, route.distanceKm)
        assertEquals(1234567890L, route.createdAtEpochSeconds)
    }
    
    @Test
    fun testRouteWithSameOriginAndDestinationFails() {
        assertFailsWith<IllegalArgumentException>(
            "Origin and destination airports must be different"
        ) {
            Route(
                id = RouteId(1),
                airlineId = AirlineId(1),
                originAirportId = AirportId(100),
                destinationAirportId = AirportId(100),
                distanceKm = 100,
                createdAtEpochSeconds = 1234567890L
            )
        }
    }
    
    @Test
    fun testRouteWithNegativeDistanceFails() {
        assertFailsWith<IllegalArgumentException>(
            "Distance must be positive"
        ) {
            Route(
                id = RouteId(1),
                airlineId = AirlineId(1),
                originAirportId = AirportId(100),
                destinationAirportId = AirportId(200),
                distanceKm = -100,
                createdAtEpochSeconds = 1234567890L
            )
        }
    }
    
    @Test
    fun testRouteWithZeroDistanceFails() {
        assertFailsWith<IllegalArgumentException>(
            "Distance must be positive"
        ) {
            Route(
                id = RouteId(1),
                airlineId = AirlineId(1),
                originAirportId = AirportId(100),
                destinationAirportId = AirportId(200),
                distanceKm = 0,
                createdAtEpochSeconds = 1234567890L
            )
        }
    }
    
    @Test
    fun testRouteWithNegativeTimestampFails() {
        assertFailsWith<IllegalArgumentException>(
            "Created at timestamp must be non-negative"
        ) {
            Route(
                id = RouteId(1),
                airlineId = AirlineId(1),
                originAirportId = AirportId(100),
                destinationAirportId = AirportId(200),
                distanceKm = 100,
                createdAtEpochSeconds = -1L
            )
        }
    }
    
    @Test
    fun testRouteCreatedAtInstant() {
        val route = Route(
            id = RouteId(1),
            airlineId = AirlineId(1),
            originAirportId = AirportId(100),
            destinationAirportId = AirportId(200),
            distanceKm = 100,
            createdAtEpochSeconds = 1234567890L
        )
        
        assertEquals(1234567890L, route.createdAt.epochSecond)
    }
    
    @Test
    fun testValidateRouteEndpointsSuccess() {
        val result = validateRouteEndpoints(AirportId(100), AirportId(200))
        assertTrue(result.isSuccess())
    }
    
    @Test
    fun testValidateRouteEndpointsSameAirport() {
        val result = validateRouteEndpoints(AirportId(100), AirportId(100))
        assertFalse(result.isSuccess())
        assertEquals(
            "Origin and destination airports must be different",
            (result as RouteValidationResult.Failure).message
        )
    }
    
    @Test
    fun testValidateRouteEndpointsThrowsOnFailure() {
        val result = validateRouteEndpoints(AirportId(100), AirportId(100))
        assertFailsWith<IllegalArgumentException> {
            result.getOrThrow()
        }
    }
    
    @Test
    fun testValidateRouteEndpointsNoThrowOnSuccess() {
        val result = validateRouteEndpoints(AirportId(100), AirportId(200))
        // Should not throw
        result.getOrThrow()
    }
}
