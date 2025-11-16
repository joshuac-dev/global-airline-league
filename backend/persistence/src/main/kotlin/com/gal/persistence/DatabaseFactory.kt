package com.gal.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.dotenv
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import javax.sql.DataSource

/**
 * Singleton responsible for database initialization and connection management.
 */
object DatabaseFactory {
    private var dataSource: HikariDataSource? = null
    private var isInitialized = false
    
    // Load .env file if it exists (only once)
    private val dotenv by lazy {
        try {
            dotenv {
                ignoreIfMissing = true
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Initialize the database connection pool and run migrations.
     *
     * @param url JDBC connection string (default from DB_URL env var or .env file)
     * @param user Database username (default from DB_USER env var or .env file)
     * @param password Database password (default from DB_PASSWORD env var or .env file)
     * @param runMigrations Whether to run Flyway migrations on init (default true)
     */
    fun init(
        url: String = getEnv("DB_URL", "jdbc:postgresql://localhost:5432/gal"),
        user: String = getEnv("DB_USER", "gal"),
        password: String = getEnv("DB_PASSWORD", "gal"),
        runMigrations: Boolean = true
    ) {
        if (isInitialized) {
            return
        }

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = url
            username = user
            this.password = password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            minimumIdle = 2
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        dataSource = HikariDataSource(hikariConfig)

        // Connect Exposed to the data source
        Database.connect(dataSource!!)

        // Run Flyway migrations if enabled
        if (runMigrations) {
            runFlywayMigrations(dataSource!!)
        }

        isInitialized = true
    }

    /**
     * Close the database connection pool.
     * Useful for graceful shutdown or testing cleanup.
     */
    fun close() {
        dataSource?.close()
        dataSource = null
        TransactionManager.closeAndUnregister(Database.connect(dataSource!!))
        isInitialized = false
    }

    /**
     * Run Flyway database migrations.
     */
    private fun runFlywayMigrations(dataSource: DataSource) {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()

        flyway.migrate()
    }

    private fun getEnv(key: String, default: String): String {
        // First check System environment variables (takes precedence)
        System.getenv(key)?.let { return it }
        
        // Then check .env file
        dotenv?.get(key)?.let { return it }
        
        // Fall back to default
        return default
    }
}
