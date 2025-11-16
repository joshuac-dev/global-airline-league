package com.gal.api

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

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
        // Test that placeholder routes return 501 Not Implemented
        val endpoints = listOf(
            "/api/airlines",
            "/api/airports",
            "/api/routes",
            "/api/search"
        )

        endpoints.forEach { endpoint ->
            val response = client.get(endpoint)
            assertEquals(HttpStatusCode.NotImplemented, response.status, "Expected 501 for $endpoint")
        }
    }
}
