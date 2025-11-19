package com.gal.api.route

import com.gal.core.AirlineId
import com.gal.core.AirportId
import com.gal.core.RouteId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Register route-related REST endpoints.
 */
fun Route.routeRoutes() {
    route("/routes") {
        // POST /api/routes
        post {
            val repository = RouteRepositoryLocator.getRouteRepository()
            if (repository == null) {
                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    ErrorResponse(ErrorDetails("service_unavailable", "Database unavailable"))
                )
                return@post
            }

            val request = try {
                call.receive<RouteCreateRequest>()
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(ErrorDetails("invalid_request", "Invalid request body"))
                )
                return@post
            }

            // Validate IDs are positive
            if (request.airlineId <= 0 || request.originAirportId <= 0 || request.destinationAirportId <= 0) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(ErrorDetails("invalid_input", "IDs must be positive integers"))
                )
                return@post
            }

            // Validate origin != destination
            if (request.originAirportId == request.destinationAirportId) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        ErrorDetails(
                            "invalid_input",
                            "Origin and destination airports must be different"
                        )
                    )
                )
                return@post
            }

            // Create the route
            val result = repository.create(
                AirlineId(request.airlineId.toLong()),
                AirportId(request.originAirportId.toLong()),
                AirportId(request.destinationAirportId.toLong())
            )

            result.fold(
                onSuccess = { route ->
                    call.respond(HttpStatusCode.Created, route.toResponse())
                },
                onFailure = { error ->
                    when {
                        error.message?.contains("Airline not found") == true -> {
                            call.respond(
                                HttpStatusCode.NotFound,
                                ErrorResponse(ErrorDetails("not_found", "Airline not found"))
                            )
                        }
                        error.message?.contains("airport not found") == true -> {
                            call.respond(
                                HttpStatusCode.NotFound,
                                ErrorResponse(ErrorDetails("not_found", "Airport not found"))
                            )
                        }
                        error.message?.contains("already exists") == true -> {
                            call.respond(
                                HttpStatusCode.Conflict,
                                ErrorResponse(
                                    ErrorDetails(
                                        "duplicate_route",
                                        "Route already exists for this airline"
                                    )
                                )
                            )
                        }
                        error.message?.contains("Invalid route endpoints") == true -> {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse(
                                    ErrorDetails(
                                        "invalid_input",
                                        "Origin and destination airports must be different"
                                    )
                                )
                            )
                        }
                        else -> {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                ErrorResponse(
                                    ErrorDetails("internal_error", error.message ?: "Unknown error")
                                )
                            )
                        }
                    }
                }
            )
        }

        // GET /api/routes?airlineId=INT&limit=INT&offset=INT
        get {
            val repository = RouteRepositoryLocator.getRouteRepository()
            if (repository == null) {
                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    ErrorResponse(ErrorDetails("service_unavailable", "Database unavailable"))
                )
                return@get
            }

            val airlineIdParam = call.request.queryParameters["airlineId"]?.toLongOrNull()
            val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50

            // Validate pagination parameters
            if (offset < 0 || limit < 1 || limit > 100) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        ErrorDetails(
                            "invalid_input",
                            "Invalid pagination parameters (offset >= 0, 1 <= limit <= 100)"
                        )
                    )
                )
                return@get
            }

            val routes = if (airlineIdParam != null) {
                if (airlineIdParam <= 0) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(ErrorDetails("invalid_input", "Airline ID must be positive"))
                    )
                    return@get
                }
                repository.listByAirline(AirlineId(airlineIdParam), offset, limit)
            } else {
                repository.listAll(offset, limit)
            }

            call.respond(routes.map { it.toResponse() })
        }

        // GET /api/routes/{id}
        get("/{id}") {
            val repository = RouteRepositoryLocator.getRouteRepository()
            if (repository == null) {
                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    ErrorResponse(ErrorDetails("service_unavailable", "Database unavailable"))
                )
                return@get
            }

            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null || id <= 0) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(ErrorDetails("invalid_input", "Invalid route ID"))
                )
                return@get
            }

            val route = repository.get(RouteId(id))
            if (route == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(ErrorDetails("not_found", "Route not found"))
                )
            } else {
                call.respond(route.toResponse())
            }
        }

        // DELETE /api/routes/{id}
        delete("/{id}") {
            val repository = RouteRepositoryLocator.getRouteRepository()
            if (repository == null) {
                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    ErrorResponse(ErrorDetails("service_unavailable", "Database unavailable"))
                )
                return@delete
            }

            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null || id <= 0) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(ErrorDetails("invalid_input", "Invalid route ID"))
                )
                return@delete
            }

            val deleted = repository.delete(RouteId(id))
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(ErrorDetails("not_found", "Route not found"))
                )
            }
        }
    }
}
