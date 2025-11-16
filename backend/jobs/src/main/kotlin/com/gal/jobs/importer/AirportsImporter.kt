package com.gal.jobs.importer

import com.gal.persistence.DatabaseFactory
import com.gal.persistence.airport.AirportRow
import com.gal.persistence.airport.AirportsBulkRepository
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileReader

/**
 * CLI tool to import airport data from an OurAirports-style CSV file.
 * 
 * Environment variables (can be set in .env file or system environment):
 * - IMPORT_AIRPORTS_CSV: Path to the CSV file (required)
 * - IMPORT_AIRPORTS_TRUNCATE: Set to "true" to truncate the table before import (default: false)
 * - IMPORT_AIRPORTS_BATCH_SIZE: Number of rows to insert in each batch (default: 1000)
 * - IMPORT_AIRPORTS_LOG_INTERVAL: Log progress every N rows (default: 5000)
 * - DB_URL, DB_USER, DB_PASSWORD: Database connection parameters (required)
 */
object AirportsImporter {
    private val logger = LoggerFactory.getLogger(AirportsImporter::class.java)
    
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
    
    private fun getEnv(key: String): String? {
        return System.getenv(key) ?: dotenv?.get(key)
    }
    
    private fun getEnvOrDefault(key: String, default: String): String {
        return getEnv(key) ?: default
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        logger.info("Starting airport data import")

        // Read configuration from environment or .env file
        val csvPath = getEnv("IMPORT_AIRPORTS_CSV")
            ?: error("IMPORT_AIRPORTS_CSV environment variable is required")
        val truncate = getEnv("IMPORT_AIRPORTS_TRUNCATE")?.toBoolean() ?: false
        val batchSize = getEnv("IMPORT_AIRPORTS_BATCH_SIZE")?.toIntOrNull() ?: 1000
        val logInterval = getEnv("IMPORT_AIRPORTS_LOG_INTERVAL")?.toIntOrNull() ?: 5000

        logger.info("Configuration:")
        logger.info("  CSV path: $csvPath")
        logger.info("  Truncate: $truncate")
        logger.info("  Batch size: $batchSize")
        logger.info("  Log interval: $logInterval")

        // Validate CSV file exists
        val csvFile = File(csvPath)
        if (!csvFile.exists()) {
            error("CSV file not found: $csvPath")
        }

        // Initialize database connection
        logger.info("Initializing database connection")
        DatabaseFactory.init()

        val repository = AirportsBulkRepository()

        // Check if table is empty (unless truncate is requested)
        if (!truncate && !repository.isEmpty()) {
            logger.warn("Airports table is not empty. Skipping import.")
            logger.warn("To force import, set IMPORT_AIRPORTS_TRUNCATE=true")
            return@runBlocking
        }

        // Truncate if requested
        if (truncate) {
            logger.info("Truncating airports table")
            repository.truncateAirports()
        }

        // Parse and import CSV
        logger.info("Reading CSV file: $csvPath")
        importCsv(csvFile, repository, batchSize, logInterval)

        logger.info("Airport import completed successfully")
    }

    private suspend fun importCsv(
        csvFile: File,
        repository: AirportsBulkRepository,
        batchSize: Int,
        logInterval: Int
    ) {
        val csvFormat = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .build()

        var totalRows = 0
        var skippedRows = 0
        var insertedRows = 0
        var currentBatch = mutableListOf<AirportRow>()

        FileReader(csvFile).use { reader ->
            CSVParser(reader, csvFormat).use { parser ->
                for ((index, record) in parser.withIndex()) {
                    totalRows++

                    // Map CSV record to AirportRow
                    val airportRow = AirportCsvMapper.mapCsvRecord(record)
                    if (airportRow == null) {
                        skippedRows++
                        continue
                    }

                    currentBatch.add(airportRow)

                    // Insert batch when it reaches the batch size
                    if (currentBatch.size >= batchSize) {
                        val inserted = repository.insertBatch(currentBatch)
                        insertedRows += inserted
                        currentBatch.clear()

                        // Log progress
                        if (totalRows % logInterval == 0) {
                            logger.info("Progress: $totalRows rows read, $insertedRows inserted, $skippedRows skipped")
                        }
                    }
                }

                // Insert remaining rows in the last batch
                if (currentBatch.isNotEmpty()) {
                    val inserted = repository.insertBatch(currentBatch)
                    insertedRows += inserted
                    currentBatch.clear()
                }
            }
        }

        logger.info("Import complete:")
        logger.info("  Total rows read: $totalRows")
        logger.info("  Rows inserted: $insertedRows")
        logger.info("  Rows skipped: $skippedRows")
    }
}
