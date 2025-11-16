package com.globalairline.api.websocket

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList

@Serializable
data class HeartbeatMessage(
    val type: String = "heartbeat",
    val ts: Long
)

/**
 * Manages WebSocket connections and broadcasts.
 */
object WebSocketBroadcaster {
    private val worldConnections = CopyOnWriteArrayList<WebSocketSession>()
    private val airlineConnections = mutableMapOf<Int, CopyOnWriteArrayList<WebSocketSession>>()
    
    fun addWorldConnection(session: WebSocketSession) {
        worldConnections.add(session)
    }
    
    fun removeWorldConnection(session: WebSocketSession) {
        worldConnections.remove(session)
    }
    
    suspend fun broadcastToWorld(message: String) {
        worldConnections.forEach { session ->
            try {
                session.send(Frame.Text(message))
            } catch (e: Exception) {
                // Connection closed, will be cleaned up
            }
        }
    }
    
    fun addAirlineConnection(airlineId: Int, session: WebSocketSession) {
        airlineConnections.getOrPut(airlineId) { CopyOnWriteArrayList() }.add(session)
    }
    
    fun removeAirlineConnection(airlineId: Int, session: WebSocketSession) {
        airlineConnections[airlineId]?.remove(session)
    }
    
    suspend fun broadcastToAirline(airlineId: Int, message: String) {
        airlineConnections[airlineId]?.forEach { session ->
            try {
                session.send(Frame.Text(message))
            } catch (e: Exception) {
                // Connection closed, will be cleaned up
            }
        }
    }
}

fun Application.configureWebSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(30)
        timeout = Duration.ofSeconds(60)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    
    routing {
        webSocket("/ws/world") {
            WebSocketBroadcaster.addWorldConnection(this)
            
            try {
                // Send periodic heartbeat
                launch {
                    while (isActive) {
                        val heartbeat = HeartbeatMessage(ts = System.currentTimeMillis())
                        send(Frame.Text(Json.encodeToString(heartbeat)))
                        delay(5000) // Every 5 seconds
                    }
                }
                
                // Keep connection open and handle incoming messages
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        // Handle incoming messages if needed
                    }
                }
            } finally {
                WebSocketBroadcaster.removeWorldConnection(this)
            }
        }
        
        webSocket("/ws/airline/{id}") {
            val airlineId = call.parameters["id"]?.toIntOrNull() ?: return@webSocket close(
                CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid airline ID")
            )
            
            WebSocketBroadcaster.addAirlineConnection(airlineId, this)
            
            try {
                // Send periodic heartbeat
                launch {
                    while (isActive) {
                        val heartbeat = HeartbeatMessage(ts = System.currentTimeMillis())
                        send(Frame.Text(Json.encodeToString(heartbeat)))
                        delay(5000) // Every 5 seconds
                    }
                }
                
                // Keep connection open and handle incoming messages
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        // Handle incoming messages if needed
                    }
                }
            } finally {
                WebSocketBroadcaster.removeAirlineConnection(airlineId, this)
            }
        }
    }
}
