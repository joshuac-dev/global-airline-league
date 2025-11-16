package com.globalairline.jobs

import com.globalairline.api.websocket.WebSocketBroadcaster
import com.globalairline.core.service.SimulationClock
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

@Serializable
data class WorldUpdateMessage(
    val type: String = "world_update",
    val cycle: Long,
    val ts: Long
)

/**
 * Background job that runs periodic simulation ticks.
 */
class SimulationTickJob(
    private val scope: CoroutineScope,
    private val clock: SimulationClock = SimulationClock(),
    private val tickIntervalMs: Long = 5000 // 5 seconds
) {
    private val logger = LoggerFactory.getLogger(SimulationTickJob::class.java)
    private var job: Job? = null
    
    fun start() {
        logger.info("Starting simulation tick job with interval ${tickIntervalMs}ms")
        job = scope.launch {
            while (isActive) {
                try {
                    tick()
                } catch (e: Exception) {
                    logger.error("Error during simulation tick", e)
                }
                delay(tickIntervalMs)
            }
        }
    }
    
    private suspend fun tick() {
        clock.incrementCycle()
        val currentCycle = clock.getCurrentCycle()
        
        logger.info("Simulation tick: cycle $currentCycle")
        
        // Broadcast world update to all connected clients
        val update = WorldUpdateMessage(
            cycle = currentCycle,
            ts = System.currentTimeMillis()
        )
        WebSocketBroadcaster.broadcastToWorld(Json.encodeToString(update))
    }
    
    fun stop() {
        logger.info("Stopping simulation tick job")
        job?.cancel()
    }
}

/**
 * Helper to create a simulation job scope tied to an external scope.
 */
fun createSimulationJobScope(parentScope: CoroutineScope): CoroutineScope {
    return CoroutineScope(parentScope.coroutineContext + SupervisorJob())
}
