# Airport Data Import

This guide explains how to import airport data from an OurAirports CSV file into the PostgreSQL database.

## Overview

The airport importer reads an OurAirports-style CSV file and populates the `airports` table with:
- Airport names, IATA/ICAO codes
- Geographic coordinates (latitude, longitude)
- City, country, and timezone information
- Elevation (converted from feet to meters)

## Prerequisites

1. **PostgreSQL database** running and accessible
2. **OurAirports CSV file** downloaded (see below)
3. **Environment variables** configured for database connection

## Downloading the Dataset

Download the latest airports CSV from OurAirports:

**URL:** https://davidmegginson.github.io/ourairports-data/airports.csv

Or use curl:
```bash
curl -o airports.csv https://davidmegginson.github.io/ourairports-data/airports.csv
```

The CSV file contains comprehensive global airport data with the following relevant columns:
- `name` - Airport name
- `iata_code` - 3-letter IATA code (e.g., "LHR")
- `icao_code` - 4-letter ICAO code (e.g., "EGLL")
- `gps_code` - Alternative 4-letter code (fallback if icao_code is missing)
- `municipality` - City/town name
- `iso_country` - ISO 3166-1 alpha-2 country code (e.g., "GB", "US")
- `latitude_deg` - Latitude in decimal degrees
- `longitude_deg` - Longitude in decimal degrees
- `elevation_ft` - Elevation in feet (converted to meters during import)
- `timezone` - Timezone identifier (e.g., "Europe/London")

## CSV to Database Mapping

| CSV Column | DB Column | Type | Transform |
|------------|-----------|------|-----------|
| `name` | `name` | TEXT | Trim |
| `iata_code` | `iata` | CHAR(3) | Trim, uppercase, null if empty |
| `icao_code` or `gps_code` | `icao` | CHAR(4) | Trim, uppercase, prefer icao_code |
| `municipality` | `city` | TEXT | Trim, empty string if missing |
| `iso_country` | `country_code` | CHAR(2) | Trim, uppercase |
| `latitude_deg` | `latitude` | DOUBLE PRECISION | Parse as double |
| `longitude_deg` | `longitude` | DOUBLE PRECISION | Parse as double |
| `elevation_ft` | `elevation_m` | INTEGER | Convert ft→m, round |
| `timezone` | `timezone` | TEXT | Trim, null if empty |
| N/A | `size` | INTEGER | Always null (not in CSV) |

**Essential fields:** Rows missing `name`, `iso_country`, `latitude_deg`, or `longitude_deg` are skipped.

## Environment Variables

You can configure the importer using environment variables in two ways:

### Option 1: Using a .env file (Recommended)

Create a `.env` file in the project root directory:

```bash
# Database connection
DB_URL=jdbc:postgresql://localhost:5432/gal
DB_USER=gal
DB_PASSWORD=gal

# CSV file path (absolute path required)
IMPORT_AIRPORTS_CSV=/path/to/airports.csv

# Optional settings
IMPORT_AIRPORTS_TRUNCATE=false
IMPORT_AIRPORTS_BATCH_SIZE=1000
IMPORT_AIRPORTS_LOG_INTERVAL=5000
```

You can copy the `.env.example` file as a starting point:
```bash
cp .env.example .env
# Edit .env with your values
```

### Option 2: Using system environment variables

Set these environment variables before running the import:

#### Required
```bash
# Database connection
export DB_URL="jdbc:postgresql://localhost:5432/gal"
export DB_USER="gal"
export DB_PASSWORD="gal"

# CSV file path (absolute path required)
export IMPORT_AIRPORTS_CSV="/path/to/airports.csv"
```

#### Optional
```bash
# Truncate table before import (default: false)
# Only set to true if you want to replace all existing data
export IMPORT_AIRPORTS_TRUNCATE=true

# Number of rows per batch insert (default: 1000)
export IMPORT_AIRPORTS_BATCH_SIZE=1000

# Log progress every N rows (default: 5000)
export IMPORT_AIRPORTS_LOG_INTERVAL=5000
```

**Note:** System environment variables take precedence over .env file values.

## Running the Import

The importer can be run using either a .env file or system environment variables. The Gradle task automatically loads configuration from the .env file if it exists.

### Method 1: Using .env file (Recommended)

```bash
# Step 1: Download the CSV
curl -o airports.csv https://davidmegginson.github.io/ourairports-data/airports.csv

# Step 2: Copy and configure .env
cp .env.example .env
# Edit .env and set IMPORT_AIRPORTS_CSV to the absolute path of airports.csv

# Step 3: Run the import
./gradlew :backend:jobs:importAirports
```

The Gradle task will automatically read configuration from the .env file.

### Method 2: Using system environment variables

```bash
# Step 1: Download the CSV
curl -o airports.csv https://davidmegginson.github.io/ourairports-data/airports.csv

# Step 2: Set environment variables
export DB_URL="jdbc:postgresql://localhost:5432/gal"
export DB_USER="gal"
export DB_PASSWORD="gal"
export IMPORT_AIRPORTS_CSV="$(pwd)/airports.csv"

# Step 3: Run the importer
./gradlew :backend:jobs:importAirports
```

**Note:** System environment variables take precedence over .env file values, allowing you to override specific settings without modifying the .env file.

The importer will:
1. Connect to the database
2. Check if the airports table is empty (skip if not, unless TRUNCATE is set)
3. Read the CSV file in streaming mode
4. Map and validate each row
5. Insert rows in batches
6. Log progress every 5,000 rows
7. Report final statistics

### Expected Output
```
Starting airport data import
Configuration:
  CSV path: /home/user/airports.csv
  Truncate: false
  Batch size: 1000
  Log interval: 5000
Initializing database connection
Reading CSV file: /home/user/airports.csv
Progress: 5000 rows read, 4823 inserted, 177 skipped
Progress: 10000 rows read, 9645 inserted, 355 skipped
...
Import complete:
  Total rows read: 72815
  Rows inserted: 65432
  Rows skipped: 7383
Airport import completed successfully
```

## Verification

After importing, verify the data using the API:

### List all airports (paginated)
```bash
curl 'http://localhost:8080/api/airports?limit=5'
```

Expected response (example):
```json
[
  {
    "id": 1,
    "name": "London Heathrow Airport",
    "iata": "LHR",
    "icao": "EGLL",
    "city": "London",
    "countryCode": "GB",
    "latitude": 51.4706,
    "longitude": -0.461941,
    "elevationM": 25,
    "timezone": "Europe/London",
    "size": null
  },
  ...
]
```

### Search for a specific airport
```bash
curl 'http://localhost:8080/api/search/airports?q=heathrow'
```

Expected response:
```json
[
  {
    "id": 1,
    "name": "London Heathrow Airport",
    "iata": "LHR",
    "icao": "EGLL",
    "city": "London",
    "countryCode": "GB",
    "latitude": 51.4706,
    "longitude": -0.461941,
    "elevationM": 25,
    "timezone": "Europe/London",
    "size": null
  }
]
```

### Filter by country
```bash
curl 'http://localhost:8080/api/airports?country=US&limit=5'
```

## Behavior Notes

### Default: Skip if Non-Empty
By default, the importer checks if the `airports` table has any rows. If it does, the import is skipped to prevent accidental data duplication. You'll see:
```
Airports table is not empty. Skipping import.
To force import, set IMPORT_AIRPORTS_TRUNCATE=true
```

### Truncate Mode
To replace all existing airport data, set:
```bash
export IMPORT_AIRPORTS_TRUNCATE=true
```

**⚠️ Warning:** This deletes all existing airport records before importing. Use with caution!

### Skipped Rows
Rows are skipped if they:
- Miss essential fields (name, country, latitude, longitude)
- Have invalid latitude/longitude (non-numeric)
- Have invalid country code length (not 2 characters)

Skipped rows are logged in the summary but do not cause the import to fail.

### Performance
- Batch size of 1000 provides good performance for most datasets
- Progress is logged every 5,000 rows (configurable)
- Full OurAirports dataset (~70,000 airports) imports in ~10-30 seconds on typical hardware

## Troubleshooting

### "CSV file not found"
Ensure `IMPORT_AIRPORTS_CSV` is set to an absolute path and the file exists:
```bash
ls -la "$IMPORT_AIRPORTS_CSV"
```

### "Airports table is not empty"
If you want to re-import, set:
```bash
export IMPORT_AIRPORTS_TRUNCATE=true
```

### Database connection errors
Verify database is running and credentials are correct:
```bash
psql -U gal -d gal -c "SELECT 1;"
```

### High number of skipped rows
Review the CSV file for missing essential fields. The OurAirports dataset includes small airstrips, helipads, and closed airports that may not have complete data.

## Sample Data for Testing

For quick local testing without downloading the full dataset, see [sample-airports.csv](./sample-airports.csv) which contains 5 sample airports.

To use the sample:
```bash
export IMPORT_AIRPORTS_CSV="$(pwd)/docs/dev/sample-airports.csv"
./gradlew :backend:jobs:importAirports
```

## Next Steps

After importing airports:
1. Test the API endpoints (see Verification section)
2. Implement runway data import (future PR)
3. Add city/region data (future PR)
4. Import weather/climate data if needed (future PR)
