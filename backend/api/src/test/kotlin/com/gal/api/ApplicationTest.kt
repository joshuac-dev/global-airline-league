package com.gal.api

import com.gal.api.airline.AirlineRepositoryLocator
import com.gal.api.airline.AirlineResponse
import com.gal.api.airport.AirportResponse
import com.gal.api.airport.ErrorResponse
import com.gal.api.airport.RepositoryLocator
import com.gal.core.AirlineId
import com.gal.core.AirportId
import com.gal.core.airline.Airline
import com.gal.core.airport.*
import com.gal.persistence.airline.AirlineRepository
import com.gal.persistence.airport.AirportRepository
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {
    @Test
    fun testHealthEndpoint() = testApplication {
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val healthResponse = Json.decodeFromString<HealthResponse>(body)
        assertEquals("ok", healthResponse.status)
    }

    @Test
    fun testNotImplementedEndpoints() = testApplication {
        // Routes endpoint is now implemented and returns 503 when DB is unavailable
        val response = client.get("/api/routes")
        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
    }
    
    @Test
    fun testAirportsWithoutDatabase() = testApplication {
        // Test that airport endpoints return 503 when database is not available
        RepositoryLocator.clear()
        
        val response = client.get("/api/airports")
        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        
        val body = response.bodyAsText()
        val errorResponse = Json.decodeFromString<ErrorResponse>(body)
        assertEquals("database unavailable", errorResponse.error)
    }
    
    @Test
    fun testAirportsListEmpty() = testApplication {
        // Setup stub repository
        RepositoryLocator.setAirportRepository(StubAirportRepository())
        
        val response = client.get("/api/airports")
        assertEquals(HttpStatusCode.OK, response.status)
        
        val body = response.bodyAsText()
        val airports = Json.decodeFromString<List<AirportResponse>>(body)
        assertEquals(0, airports.size)
        
        RepositoryLocator.clear()
    }
    
    @Test
    fun testAirportByIdNotFound() = testApplication {
        // Setup stub repository
        RepositoryLocator.setAirportRepository(StubAirportRepository())
        
        val response = client.get("/api/airports/999")
        assertEquals(HttpStatusCode.NotFound, response.status)
        
        val body = response.bodyAsText()
        val errorResponse = Json.decodeFromString<ErrorResponse>(body)
        assertEquals("airport not found", errorResponse.error)
        
        RepositoryLocator.clear()
    }
    
    @Test
    fun testSearchAirportsEmpty() = testApplication {
        // Setup stub repository
        RepositoryLocator.setAirportRepository(StubAirportRepository())
        
        val response = client.get("/api/search/airports?q=test")
        assertEquals(HttpStatusCode.OK, response.status)
        
        val body = response.bodyAsText()
        val airports = Json.decodeFromString<List<AirportResponse>>(body)
        assertEquals(0, airports.size)
        
        RepositoryLocator.clear()
    }
    
    @Test
    fun testSearchAirportsWithoutQuery() = testApplication {
        // Setup stub repository
        RepositoryLocator.setAirportRepository(StubAirportRepository())
        
        val response = client.get("/api/search/airports")
        assertEquals(HttpStatusCode.OK, response.status)
        
        val body = response.bodyAsText()
        val airports = Json.decodeFromString<List<AirportResponse>>(body)
        assertEquals(0, airports.size)
        
        RepositoryLocator.clear()
    }
    
    @Test
    fun testAirportParameterParsing() = testApplication {
        // Setup stub repository
        RepositoryLocator.setAirportRepository(StubAirportRepository())
        
        // Test with offset and limit
        val response1 = client.get("/api/airports?offset=10&limit=20")
        assertEquals(HttpStatusCode.OK, response1.status)
        
        // Test with country filter
        val response2 = client.get("/api/airports?country=US")
        assertEquals(HttpStatusCode.OK, response2.status)
        
        RepositoryLocator.clear()
    }
    
    @Test
    fun testAirlinesWithoutDatabase() = testApplication {
        // Test that airline endpoints return 503 when database is not available
        AirlineRepositoryLocator.clear()
        
        val response = client.get("/api/airlines")
        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        
        val body = response.bodyAsText()
        val errorResponse = Json.decodeFromString<ErrorResponse>(body)
        assertEquals("database unavailable", errorResponse.error)
    }
    
    @Test
    fun testAirlinesListEmpty() = testApplication {
        // Setup stub repository
        AirlineRepositoryLocator.setAirlineRepository(StubAirlineRepository())
        
        val response = client.get("/api/airlines")
        assertEquals(HttpStatusCode.OK, response.status)
        
        val body = response.bodyAsText()
        val airlines = Json.decodeFromString<List<AirlineResponse>>(body)
        assertEquals(0, airlines.size)
        
        AirlineRepositoryLocator.clear()
    }
    
    @Test
    fun testAirlineByIdNotFound() = testApplication {
        // Setup stub repository
        AirlineRepositoryLocator.setAirlineRepository(StubAirlineRepository())
        
        val response = client.get("/api/airlines/999")
        assertEquals(HttpStatusCode.NotFound, response.status)
        
        val body = response.bodyAsText()
        val errorResponse = Json.decodeFromString<ErrorResponse>(body)
        assertEquals("airline not found", errorResponse.error)
        
        AirlineRepositoryLocator.clear()
    }
    
    @Test
    fun testAirlineParameterParsing() = testApplication {
        // Setup stub repository
        AirlineRepositoryLocator.setAirlineRepository(StubAirlineRepository())
        
        // Test with offset and limit
        val response1 = client.get("/api/airlines?offset=10&limit=20")
        assertEquals(HttpStatusCode.OK, response1.status)
        
        AirlineRepositoryLocator.clear()
    }
}

/**
 * Stub repository for testing that returns empty results.
 */
class StubAirportRepository : AirportRepository {
    override suspend fun get(id: AirportId): Airport? = null
    
    override suspend fun list(offset: Int, limit: Int, country: CountryCode?): List<Airport> = emptyList()
    
    override suspend fun search(query: String, limit: Int): List<Airport> = emptyList()
}

/**
 * Stub airline repository for testing that returns empty results.
 */
class StubAirlineRepository : AirlineRepository {
    override suspend fun create(name: String): Result<Airline> = 
        Result.failure(UnsupportedOperationException("Not implemented in stub"))
    
    override suspend fun get(id: AirlineId): Airline? = null
    
    override suspend fun list(offset: Int, limit: Int): List<Airline> = emptyList()
}
