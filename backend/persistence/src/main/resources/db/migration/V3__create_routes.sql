-- Create routes table to link airlines with airport connections

CREATE TABLE routes (
    id SERIAL PRIMARY KEY,
    airline_id BIGINT NOT NULL REFERENCES airlines(id) ON DELETE CASCADE,
    origin_airport_id BIGINT NOT NULL REFERENCES airports(id) ON DELETE CASCADE,
    destination_airport_id BIGINT NOT NULL REFERENCES airports(id) ON DELETE CASCADE,
    distance_km INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Prevent duplicate same-direction routes per airline
CREATE UNIQUE INDEX routes_unique_airline_origin_dest ON routes(airline_id, origin_airport_id, destination_airport_id);

-- Indexes for common lookups
CREATE INDEX routes_origin_idx ON routes(origin_airport_id);
CREATE INDEX routes_destination_idx ON routes(destination_airport_id);
CREATE INDEX routes_airline_idx ON routes(airline_id);
