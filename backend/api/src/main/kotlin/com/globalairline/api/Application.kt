package com.globalairline.api

import com.globalairline.api.routes.configureRouting
import com.globalairline.api.websocket.configureWebSockets
import com.globalairline.persistence.db.DatabaseFactory
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

// Global scope for background jobs
val backgroundJobScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

fun Application.module() {
    // Initialize database
    val dbUrl = environment.config.property("database.url").getString()
    val dbUsername = environment.config.property("database.username").getString()
    val dbPassword = environment.config.property("database.password").getString()
    
    // Run migrations
    DatabaseFactory.runMigrations(dbUrl, dbUsername, dbPassword)
    
    // Initialize database connection pool
    DatabaseFactory.init(dbUrl, dbUsername, dbPassword)
    
    // Install plugins
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.local.uri.startsWith("/api") }
    }
    
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
        }
    }
    
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        anyHost() // For development only - restrict in production
    }
    
    // Configure WebSockets and routing
    configureWebSockets()
    configureRouting()
    
    // Shutdown hook
    environment.monitor.subscribe(ApplicationStopped) {
        DatabaseFactory.close()
    }
}
