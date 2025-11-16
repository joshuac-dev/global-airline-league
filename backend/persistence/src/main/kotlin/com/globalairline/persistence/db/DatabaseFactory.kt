package com.globalairline.persistence.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

/**
 * Database factory for managing database connections and migrations.
 */
object DatabaseFactory {
    
    private lateinit var dataSource: HikariDataSource
    
    fun init(
        jdbcUrl: String,
        username: String,
        password: String,
        driverClassName: String = "org.postgresql.Driver",
        maximumPoolSize: Int = 10
    ) {
        dataSource = createHikariDataSource(jdbcUrl, username, password, driverClassName, maximumPoolSize)
        Database.connect(dataSource)
    }
    
    private fun createHikariDataSource(
        jdbcUrl: String,
        username: String,
        password: String,
        driverClassName: String,
        maximumPoolSize: Int
    ): HikariDataSource {
        val config = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = username
            this.password = password
            this.driverClassName = driverClassName
            this.maximumPoolSize = maximumPoolSize
            this.isAutoCommit = false
            this.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }
    
    fun runMigrations(jdbcUrl: String, username: String, password: String) {
        val flyway = Flyway.configure()
            .dataSource(jdbcUrl, username, password)
            .locations("classpath:db/migration")
            .load()
        flyway.migrate()
    }
    
    fun <T> dbQuery(block: Transaction.() -> T): T = transaction { block() }
    
    fun close() {
        if (::dataSource.isInitialized) {
            dataSource.close()
        }
    }
}
