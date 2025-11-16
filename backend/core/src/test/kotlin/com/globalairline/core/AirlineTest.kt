package com.globalairline.core

import com.globalairline.core.domain.Airline
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AirlineTest {
    
    @Test
    fun `reputation should be non-negative`() {
        assertThrows<IllegalArgumentException> {
            Airline(id = 1, name = "Test Airline", reputation = -10)
        }
    }

    @Test
    fun `airline should have valid reputation`() {
        val airline = Airline(id = 1, name = "Test Airline", reputation = 100)
        airline.reputation shouldBe 100
    }
}
