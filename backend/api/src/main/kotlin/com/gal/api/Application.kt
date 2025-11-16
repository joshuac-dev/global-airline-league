package com.gal.api

import com.gal.api.airport.RepositoryLocator
import com.gal.api.airport.airportRoutes
import com.gal.persistence.DatabaseFactory
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.serialization.Serializable
import org.slf4j.event.Level
import java.time.Duration

fun Application.module() {
    // Initialize database and repositories once at startup
    // Skip initialization if already done (e.g., by tests that set up stub repositories)
    val shouldInitialize = System.getenv("SKIP_DB_INIT") != "true" && 
                           RepositoryLocator.getAirportRepository() == null
    
    if (shouldInitialize) {
        try {
            DatabaseFactory.init()
            RepositoryLocator.initialize()
        } catch (e: Exception) {
            log.warn("Failed to initialize database: ${e.message}")
            // Continue without database - API will return 503 for database-dependent endpoints
        }
    }

    // Install plugins
    install(CallLogging) {
        level = Level.INFO
    }

    install(ContentNegotiation) {
        json()
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost() // TODO: Configure properly for production
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(
                text = "500: ${cause.message ?: "Internal Server Error"}",
                status = HttpStatusCode.InternalServerError
            )
        }
    }

    // Configure routing
    routing {
        // Health check endpoint
        get("/health") {
            call.respond(HealthResponse(status = "ok"))
        }

        // API routes
        route("/api") {
            // Airport routes (implemented)
            airportRoutes()
            
            // Placeholder route groups (not yet implemented)
            route("/airlines") {
                get {
                    call.respond(HttpStatusCode.NotImplemented, "Airlines API not yet implemented")
                }
            }

            route("/routes") {
                get {
                    call.respond(HttpStatusCode.NotImplemented, "Routes API not yet implemented")
                }
            }
        }

        // Placeholder WebSocket endpoints
        webSocket("/ws/world") {
            // Global world updates - to be implemented
            close(CloseReason(CloseReason.Codes.NORMAL, "Not yet implemented"))
        }

        webSocket("/ws/airline/{id}") {
            // Airline-specific updates - to be implemented
            close(CloseReason(CloseReason.Codes.NORMAL, "Not yet implemented"))
        }
    }
}

@Serializable
data class HealthResponse(val status: String)
