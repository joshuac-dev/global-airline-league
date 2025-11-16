-- Initial schema for Global Airline League

-- Airports table with full-text search support
CREATE TABLE airports (
    id SERIAL PRIMARY KEY,
    iata TEXT NOT NULL,
    icao TEXT NOT NULL,
    name TEXT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    country TEXT NOT NULL,
    search_vector TSVECTOR GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(name, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(iata, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(icao, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(country, '')), 'C')
    ) STORED
);

-- Indexes for airports
CREATE UNIQUE INDEX idx_airports_iata ON airports(iata);
CREATE UNIQUE INDEX idx_airports_icao ON airports(icao);
CREATE INDEX idx_airports_country ON airports(country);
CREATE INDEX idx_airports_search ON airports USING GIN(search_vector);

-- Airlines table
CREATE TABLE airlines (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    reputation INTEGER NOT NULL DEFAULT 0,
    CHECK (reputation >= 0)
);

-- Index for airlines
CREATE INDEX idx_airlines_name ON airlines(name);
