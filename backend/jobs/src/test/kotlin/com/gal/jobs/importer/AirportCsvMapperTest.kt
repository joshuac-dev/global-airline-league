package com.gal.jobs.importer

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AirportCsvMapperTest {

    @Test
    fun `maps complete valid CSV record`() {
        val csvData = """
            name,iata_code,icao_code,gps_code,municipality,iso_country,latitude_deg,longitude_deg,elevation_ft,timezone
            "London Heathrow Airport",LHR,EGLL,EGLL,London,GB,51.4706,-0.461941,83,Europe/London
        """.trimIndent()

        val parser = CSVParser.parse(csvData, csvFormat())
        val record = parser.records.first()
        val result = AirportCsvMapper.mapCsvRecord(record)

        assertNotNull(result)
        assertEquals("London Heathrow Airport", result.name)
        assertEquals("LHR", result.iata)
        assertEquals("EGLL", result.icao)
        assertEquals("London", result.city)
        assertEquals("GB", result.countryCode)
        assertEquals(51.4706, result.latitude)
        assertEquals(-0.461941, result.longitude)
        assertEquals(25, result.elevationM) // 83 ft = ~25 m
        assertEquals("Europe/London", result.timezone)
        assertNull(result.size)
    }

    @Test
    fun `trims and uppercases IATA and ICAO codes`() {
        val csvData = """
            name,iata_code,icao_code,municipality,iso_country,latitude_deg,longitude_deg
            "Test Airport"," jfk "," kjfk ",New York,US,40.6413,-73.7781
        """.trimIndent()

        val parser = CSVParser.parse(csvData, csvFormat())
        val record = parser.records.first()
        val result = AirportCsvMapper.mapCsvRecord(record)

        assertNotNull(result)
        assertEquals("JFK", result.iata)
        assertEquals("KJFK", result.icao)
    }

    @Test
    fun `prefers icao_code over gps_code`() {
        val csvData = """
            name,iata_code,icao_code,gps_code,municipality,iso_country,latitude_deg,longitude_deg
            "Test Airport",ABC,WXYZ,DEFG,City,US,40.0,-74.0
        """.trimIndent()

        val parser = CSVParser.parse(csvData, csvFormat())
        val record = parser.records.first()
        val result = AirportCsvMapper.mapCsvRecord(record)

        assertNotNull(result)
        assertEquals("WXYZ", result.icao) // Should prefer icao_code
    }

    @Test
    fun `falls back to gps_code when icao_code is missing`() {
        val csvData = """
            name,iata_code,gps_code,municipality,iso_country,latitude_deg,longitude_deg
            "Test Airport",ABC,DEFG,City,US,40.0,-74.0
        """.trimIndent()

        val parser = CSVParser.parse(csvData, csvFormat())
        val record = parser.records.first()
        val result = AirportCsvMapper.mapCsvRecord(record)

        assertNotNull(result)
        assertEquals("DEFG", result.icao) // Should fall back to gps_code
    }

    @Test
    fun `treats empty IATA code as null`() {
        val csvData = """
            name,iata_code,icao_code,municipality,iso_country,latitude_deg,longitude_deg
            "Test Airport","",WXYZ,City,US,40.0,-74.0
        """.trimIndent()

        val parser = CSVParser.parse(csvData, csvFormat())
        val record = parser.records.first()
        val result = AirportCsvMapper.mapCsvRecord(record)

        assertNotNull(result)
        assertNull(result.iata)
    }

    @Test
    fun `converts elevation from feet to meters`() {
        val csvData = """
            name,iata_code,municipality,iso_country,latitude_deg,longitude_deg,elevation_ft
            "Test Airport",ABC,City,US,40.0,-74.0,1000
        """.trimIndent()

        val parser = CSVParser.parse(csvData, csvFormat())
        val record = parser.records.first()
        val result = AirportCsvMapper.mapCsvRecord(record)

        assertNotNull(result)
        assertEquals(305, result.elevationM) // 1000 ft ≈ 305 m
    }

    @Test
    fun `handles negative elevation`() {
        val csvData = """
            name,iata_code,municipality,iso_country,latitude_deg,longitude_deg,elevation_ft
            "Dead Sea Airport",DSA,Dead Sea,IL,31.5,-35.5,-1300
        """.trimIndent()

        val parser = CSVParser.parse(csvData, csvFormat())
        val record = parser.records.first()
        val result = AirportCsvMapper.mapCsvRecord(record)

        assertNotNull(result)
        assertEquals(-396, result.elevationM) // -1300 ft ≈ -396 m
    }

    @Test
    fun `returns null when name is missing`() {
        val csvData = """
            name,iata_code,municipality,iso_country,latitude_deg,longitude_deg
            "",ABC,City,US,40.0,-74.0
        """.trimIndent()

        val parser = CSVParser.parse(csvData, csvFormat())
        val record = parser.records.first()
        val result = AirportCsvMapper.mapCsvRecord(record)

        assertNull(result)
    }

    @Test
    fun `returns null when country code is missing`() {
        val csvData = """
            name,iata_code,municipality,iso_country,latitude_deg,longitude_deg
            "Test Airport",ABC,City,"",40.0,-74.0
        """.trimIndent()

        val parser = CSVParser.parse(csvData, csvFormat())
        val record = parser.records.first()
        val result = AirportCsvMapper.mapCsvRecord(record)

        assertNull(result)
    }

    @Test
    fun `returns null when latitude is missing`() {
        val csvData = """
            name,iata_code,municipality,iso_country,latitude_deg,longitude_deg
            "Test Airport",ABC,City,US,"",-74.0
        """.trimIndent()

        val parser = CSVParser.parse(csvData, csvFormat())
        val record = parser.records.first()
        val result = AirportCsvMapper.mapCsvRecord(record)

        assertNull(result)
    }

    @Test
    fun `returns null when longitude is missing`() {
        val csvData = """
            name,iata_code,municipality,iso_country,latitude_deg,longitude_deg
            "Test Airport",ABC,City,US,40.0,""
        """.trimIndent()

        val parser = CSVParser.parse(csvData, csvFormat())
        val record = parser.records.first()
        val result = AirportCsvMapper.mapCsvRecord(record)

        assertNull(result)
    }

    @Test
    fun `returns null when latitude is not a valid number`() {
        val csvData = """
            name,iata_code,municipality,iso_country,latitude_deg,longitude_deg
            "Test Airport",ABC,City,US,invalid,-74.0
        """.trimIndent()

        val parser = CSVParser.parse(csvData, csvFormat())
        val record = parser.records.first()
        val result = AirportCsvMapper.mapCsvRecord(record)

        assertNull(result)
    }

    @Test
    fun `handles empty city field`() {
        val csvData = """
            name,iata_code,municipality,iso_country,latitude_deg,longitude_deg
            "Test Airport",ABC,"",US,40.0,-74.0
        """.trimIndent()

        val parser = CSVParser.parse(csvData, csvFormat())
        val record = parser.records.first()
        val result = AirportCsvMapper.mapCsvRecord(record)

        assertNotNull(result)
        assertEquals("", result.city)
    }

    @Test
    fun `ignores invalid IATA code length`() {
        val csvData = """
            name,iata_code,municipality,iso_country,latitude_deg,longitude_deg
            "Test Airport",ABCD,City,US,40.0,-74.0
        """.trimIndent()

        val parser = CSVParser.parse(csvData, csvFormat())
        val record = parser.records.first()
        val result = AirportCsvMapper.mapCsvRecord(record)

        assertNotNull(result)
        assertNull(result.iata) // Invalid length should be treated as null
    }

    @Test
    fun `ignores invalid ICAO code length`() {
        val csvData = """
            name,iata_code,icao_code,municipality,iso_country,latitude_deg,longitude_deg
            "Test Airport",ABC,WXY,City,US,40.0,-74.0
        """.trimIndent()

        val parser = CSVParser.parse(csvData, csvFormat())
        val record = parser.records.first()
        val result = AirportCsvMapper.mapCsvRecord(record)

        assertNotNull(result)
        assertNull(result.icao) // Invalid length should be treated as null
    }

    @Test
    fun `validates country code length`() {
        val csvData = """
            name,iata_code,municipality,iso_country,latitude_deg,longitude_deg
            "Test Airport",ABC,City,USA,40.0,-74.0
        """.trimIndent()

        val parser = CSVParser.parse(csvData, csvFormat())
        val record = parser.records.first()
        val result = AirportCsvMapper.mapCsvRecord(record)

        assertNull(result) // Country code must be exactly 2 characters
    }

    private fun csvFormat() = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .setIgnoreEmptyLines(true)
        .setTrim(true)
        .build()
}
