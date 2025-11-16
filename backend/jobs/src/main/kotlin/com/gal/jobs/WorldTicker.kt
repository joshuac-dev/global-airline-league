package com.gal.jobs

import com.gal.core.GameClock
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

/**
 * WorldTicker is responsible for advancing the game simulation clock
 * at regular intervals and triggering game state updates.
 */
class WorldTicker(
    private val tickIntervalSeconds: Long = getEnvLong("TICK_INTERVAL_SECONDS", 5L)
) {
    private val logger = LoggerFactory.getLogger(WorldTicker::class.java)
    private var job: Job? = null
    private var currentClock = GameClock(0L)

    /**
     * Start the world ticker in the given coroutine scope.
     * The ticker will run until stop() is called or the scope is cancelled.
     */
    fun start(scope: CoroutineScope) {
        if (job?.isActive == true) {
            logger.warn("WorldTicker is already running")
            return
        }

        logger.info("Starting WorldTicker with interval: ${tickIntervalSeconds}s")
        
        job = scope.launch {
            while (isActive) {
                tick()
                delay(tickIntervalSeconds.seconds)
            }
        }
    }

    /**
     * Stop the world ticker.
     */
    fun stop() {
        logger.info("Stopping WorldTicker")
        job?.cancel()
        job = null
    }

    /**
     * Perform a single simulation tick.
     * In the future, this will orchestrate domain services to update game state.
     */
    private fun tick() {
        currentClock = currentClock.next()
        logger.info("Simulation tick: cycle=${currentClock.cycle}")
        
        // TODO: Trigger simulation events:
        // - Update passenger demand
        // - Process routes and assign passengers
        // - Calculate airline finances
        // - Update reputation
        // - Trigger events (oil price changes, disasters, etc.)
        // - Broadcast updates to WebSocket clients
    }

    /**
     * Get the current game clock (for testing or monitoring).
     */
    fun getCurrentClock(): GameClock = currentClock

    companion object {
        // Load .env file if it exists
        private val dotenv by lazy {
            try {
                dotenv {
                    ignoreIfMissing = true
                }
            } catch (e: Exception) {
                null
            }
        }
        
        private fun getEnvLong(key: String, default: Long): Long {
            // Check system environment first
            System.getenv(key)?.toLongOrNull()?.let { return it }
            
            // Check .env file
            dotenv?.get(key)?.toLongOrNull()?.let { return it }
            
            // Return default
            return default
        }
    }
}
