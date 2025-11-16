package com.globalairline.api.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val status: String,
    val version: String
)

fun Application.configureRouting() {
    routing {
        route("/api") {
            // Health endpoint
            get("/health") {
                call.respond(HealthResponse(status = "ok", version = "0.1.0-SNAPSHOT"))
            }
            
            // Placeholder routes for future implementation
            route("/airlines") {
                get {
                    call.respond(emptyList<String>())
                }
            }
            
            route("/airports") {
                get {
                    call.respond(emptyList<String>())
                }
            }
            
            route("/routes") {
                get {
                    call.respond(emptyList<String>())
                }
            }
            
            route("/search") {
                get {
                    call.respond(emptyList<String>())
                }
            }
        }
    }
}
