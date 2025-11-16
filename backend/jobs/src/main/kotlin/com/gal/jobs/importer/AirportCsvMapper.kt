package com.gal.jobs.importer

import com.gal.persistence.airport.AirportRow
import org.apache.commons.csv.CSVRecord
import kotlin.math.roundToInt

/**
 * Maps OurAirports CSV records to AirportRow domain objects.
 * Handles field transformations, trimming, and validation.
 */
object AirportCsvMapper {

    /**
     * Map a CSV record to an AirportRow, or null if essential fields are missing.
     * 
     * Essential fields: name, country_code, latitude_deg, longitude_deg
     * 
     * Transformations:
     * - IATA/ICAO codes are trimmed, uppercased, and empty strings are treated as null
     * - ICAO prefers icao_code field, falls back to gps_code if not present
     * - elevation_ft is converted to meters and rounded
     * - city falls back to municipality
     */
    fun mapCsvRecord(record: CSVRecord): AirportRow? {
        // Check essential fields
        val name = record.getOrNull("name")?.trim()
        val countryCode = record.getOrNull("iso_country")?.trim()?.uppercase()
        val latitudeStr = record.getOrNull("latitude_deg")
        val longitudeStr = record.getOrNull("longitude_deg")

        if (name.isNullOrBlank() || countryCode.isNullOrBlank() || 
            latitudeStr.isNullOrBlank() || longitudeStr.isNullOrBlank()) {
            return null
        }

        // Parse coordinates
        val latitude = latitudeStr.toDoubleOrNull() ?: return null
        val longitude = longitudeStr.toDoubleOrNull() ?: return null

        // Validate country code is 2 characters
        if (countryCode.length != 2) {
            return null
        }

        // Extract and normalize IATA code
        val iata = record.getOrNull("iata_code")?.trim()?.uppercase()
            ?.takeIf { it.isNotBlank() && it.length == 3 }

        // Extract and normalize ICAO code - prefer icao_code, fall back to gps_code
        val icaoCode = record.getOrNull("icao_code")?.trim()?.uppercase()
            ?.takeIf { it.isNotBlank() }
        val gpsCode = record.getOrNull("gps_code")?.trim()?.uppercase()
            ?.takeIf { it.isNotBlank() }
        val icao = (icaoCode ?: gpsCode)?.takeIf { it.length == 4 }

        // Extract city - use municipality field
        val city = record.getOrNull("municipality")?.trim()?.takeIf { it.isNotBlank() } ?: ""

        // Convert elevation from feet to meters
        val elevationM = record.getOrNull("elevation_ft")?.toDoubleOrNull()
            ?.let { (it * 0.3048).roundToInt() }

        // Extract timezone
        val timezone = record.getOrNull("timezone")?.trim()?.takeIf { it.isNotBlank() }

        return AirportRow(
            name = name,
            iata = iata,
            icao = icao,
            city = city,
            countryCode = countryCode,
            latitude = latitude,
            longitude = longitude,
            elevationM = elevationM,
            timezone = timezone,
            size = null // Size is not in OurAirports CSV
        )
    }

    /**
     * Safely get a value from a CSV record, returning null if the column doesn't exist.
     */
    private fun CSVRecord.getOrNull(column: String): String? {
        return try {
            if (this.isMapped(column)) this.get(column) else null
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
