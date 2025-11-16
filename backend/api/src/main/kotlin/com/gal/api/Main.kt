package com.gal.api

import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {
    // Load .env file if it exists
    val dotenv = try {
        dotenv {
            ignoreIfMissing = true
        }
    } catch (e: Exception) {
        null
    }
    
    // Get port from environment or .env file
    val port = System.getenv("PORT")?.toIntOrNull()
        ?: dotenv?.get("PORT")?.toIntOrNull()
        ?: 8080
    
    embeddedServer(
        CIO,
        port = port,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}
