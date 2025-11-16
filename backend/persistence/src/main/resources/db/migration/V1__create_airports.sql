-- Create airports table with full-text search support

CREATE TABLE airports (
    id SERIAL PRIMARY KEY,
    iata CHAR(3),
    icao CHAR(4),
    name TEXT NOT NULL,
    city TEXT NOT NULL,
    country_code CHAR(2) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    elevation_m INTEGER,
    timezone TEXT,
    size INTEGER,
    search_vector TSVECTOR GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', coalesce(iata, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(icao, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(name, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(city, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(country_code, '')), 'C')
    ) STORED
);

-- Index for full-text search
CREATE INDEX airports_search_gin ON airports USING GIN (search_vector);

-- Indexes for common lookups
CREATE INDEX airports_iata_idx ON airports (iata) WHERE iata IS NOT NULL;
CREATE INDEX airports_icao_idx ON airports (icao) WHERE icao IS NOT NULL;
CREATE INDEX airports_country_code_idx ON airports (country_code);
