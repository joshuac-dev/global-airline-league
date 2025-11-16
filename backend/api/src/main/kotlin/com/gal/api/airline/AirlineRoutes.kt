package com.gal.api.airline

import com.gal.api.airport.ErrorResponse
import com.gal.core.AirlineId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Register airline-related routes.
 */
fun Route.airlineRoutes() {
    route("/airlines") {
        // GET /api/airlines?offset=0&limit=50
        get {
            val repository = AirlineRepositoryLocator.getAirlineRepository()
            if (repository == null) {
                call.respond(HttpStatusCode.ServiceUnavailable, ErrorResponse("database unavailable"))
                return@get
            }
            
            val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
            
            val airlines = repository.list(offset, limit)
            call.respond(airlines.map { it.toResponse() })
        }
        
        // GET /api/airlines/{id}
        get("/{id}") {
            val repository = AirlineRepositoryLocator.getAirlineRepository()
            if (repository == null) {
                call.respond(HttpStatusCode.ServiceUnavailable, ErrorResponse("database unavailable"))
                return@get
            }
            
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("invalid airline id"))
                return@get
            }
            
            val airline = repository.get(AirlineId(id))
            if (airline == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("airline not found"))
            } else {
                call.respond(airline.toResponse())
            }
        }
    }
}
