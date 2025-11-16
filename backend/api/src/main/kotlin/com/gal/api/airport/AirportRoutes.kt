package com.gal.api.airport

import com.gal.core.AirportId
import com.gal.core.airport.CountryCode
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val error: String)

/**
 * Register airport-related routes.
 */
fun Route.airportRoutes() {
    route("/airports") {
        // GET /api/airports?offset=0&limit=50&country=US
        get {
            val repository = RepositoryLocator.getAirportRepository()
            if (repository == null) {
                call.respond(HttpStatusCode.ServiceUnavailable, ErrorResponse("database unavailable"))
                return@get
            }
            
            val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
            val country = call.request.queryParameters["country"]?.let { CountryCode(it) }
            
            val airports = repository.list(offset, limit, country)
            call.respond(airports.map { it.toResponse() })
        }
        
        // GET /api/airports/{id}
        get("/{id}") {
            val repository = RepositoryLocator.getAirportRepository()
            if (repository == null) {
                call.respond(HttpStatusCode.ServiceUnavailable, ErrorResponse("database unavailable"))
                return@get
            }
            
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("invalid airport id"))
                return@get
            }
            
            val airport = repository.get(AirportId(id))
            if (airport == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("airport not found"))
            } else {
                call.respond(airport.toResponse())
            }
        }
    }
    
    route("/search/airports") {
        // GET /api/search/airports?q=heathrow&limit=10
        get {
            val repository = RepositoryLocator.getAirportRepository()
            if (repository == null) {
                call.respond(HttpStatusCode.ServiceUnavailable, ErrorResponse("database unavailable"))
                return@get
            }
            
            val query = call.request.queryParameters["q"] ?: ""
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
            
            if (query.isBlank()) {
                call.respond(emptyList<AirportResponse>())
                return@get
            }
            
            val airports = repository.search(query, limit)
            call.respond(airports.map { it.toResponse() })
        }
    }
}
