package com.gal.core.airline

import com.gal.core.AirlineId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AirlineTest {
    
    @Test
    fun testValidAirlineName() {
        // Test valid names
        assertTrue(validateAirlineName("ABC Airlines").isSuccess())
        assertTrue(validateAirlineName("Air France").isSuccess())
        assertTrue(validateAirlineName("Delta Air Lines").isSuccess())
        assertTrue(validateAirlineName("O'Hare Express").isSuccess())
        assertTrue(validateAirlineName("Trans-World Airlines").isSuccess())
        assertTrue(validateAirlineName("A&B Airways").isSuccess())
        assertTrue(validateAirlineName("U.S. Airways").isSuccess())
        assertTrue(validateAirlineName("123 Air").isSuccess())
    }
    
    @Test
    fun testInvalidAirlineNameTooShort() {
        val result = validateAirlineName("AB")
        assertFalse(result.isSuccess())
        assertEquals(
            "Airline name must be at least 3 characters long", 
            (result as ValidationResult.Failure).message
        )
    }
    
    @Test
    fun testInvalidAirlineNameTooLong() {
        val longName = "A".repeat(41)
        val result = validateAirlineName(longName)
        assertFalse(result.isSuccess())
        assertEquals(
            "Airline name must not exceed 40 characters", 
            (result as ValidationResult.Failure).message
        )
    }
    
    @Test
    fun testInvalidAirlineNameNotTrimmed() {
        val result = validateAirlineName(" ABC Airlines ")
        assertFalse(result.isSuccess())
        assertEquals(
            "Airline name must be trimmed", 
            (result as ValidationResult.Failure).message
        )
    }
    
    @Test
    fun testInvalidAirlineNameInvalidCharacters() {
        // Test various invalid characters
        val invalidNames = listOf(
            "ABC@Airlines",
            "Air#France",
            "Delta*Air",
            "Trans_World",
            "A+B Airways",
            "Euro€Air"
        )
        
        invalidNames.forEach { name ->
            val result = validateAirlineName(name)
            assertFalse(result.isSuccess(), "Expected '$name' to be invalid")
        }
    }
    
    @Test
    fun testAirlineCreation() {
        val airline = Airline(
            id = AirlineId(1),
            name = "Test Airlines",
            createdAtEpochSeconds = 1234567890L
        )
        
        assertEquals(AirlineId(1), airline.id)
        assertEquals("Test Airlines", airline.name)
        assertEquals(1234567890L, airline.createdAtEpochSeconds)
    }
}
