-- Sample airports for testing and development
-- Run this after the migrations to populate some test data

INSERT INTO airports (iata, icao, name, city, country_code, latitude, longitude, elevation_m, timezone, size) VALUES
('LHR', 'EGLL', 'London Heathrow Airport', 'London', 'GB', 51.4700, -0.4543, 25, 'Europe/London', 5),
('JFK', 'KJFK', 'John F. Kennedy International Airport', 'New York', 'US', 40.6413, -73.7781, 4, 'America/New_York', 5),
('LAX', 'KLAX', 'Los Angeles International Airport', 'Los Angeles', 'US', 33.9425, -118.4072, 38, 'America/Los_Angeles', 5),
('CDG', 'LFPG', 'Charles de Gaulle Airport', 'Paris', 'FR', 49.0097, 2.5479, 119, 'Europe/Paris', 5),
('NRT', 'RJAA', 'Narita International Airport', 'Tokyo', 'JP', 35.7647, 140.3864, 43, 'Asia/Tokyo', 5),
('SYD', 'YSSY', 'Sydney Kingsford Smith Airport', 'Sydney', 'AU', -33.9461, 151.1772, 21, 'Australia/Sydney', 5),
('DXB', 'OMDB', 'Dubai International Airport', 'Dubai', 'AE', 25.2532, 55.3657, 19, 'Asia/Dubai', 5),
('SIN', 'WSSS', 'Singapore Changi Airport', 'Singapore', 'SG', 1.3644, 103.9915, 7, 'Asia/Singapore', 5),
('ORD', 'KORD', 'O''Hare International Airport', 'Chicago', 'US', 41.9742, -87.9073, 205, 'America/Chicago', 5),
('FRA', 'EDDF', 'Frankfurt Airport', 'Frankfurt', 'DE', 50.0379, 8.5622, 111, 'Europe/Berlin', 5),
('AMS', 'EHAM', 'Amsterdam Airport Schiphol', 'Amsterdam', 'NL', 52.3086, 4.7639, -3, 'Europe/Amsterdam', 5),
('HKG', 'VHHH', 'Hong Kong International Airport', 'Hong Kong', 'HK', 22.3080, 113.9185, 9, 'Asia/Hong_Kong', 5),
('SFO', 'KSFO', 'San Francisco International Airport', 'San Francisco', 'US', 37.6213, -122.3790, 4, 'America/Los_Angeles', 5),
('LGA', 'KLGA', 'LaGuardia Airport', 'New York', 'US', 40.7769, -73.8740, 7, 'America/New_York', 4),
('BOS', 'KBOS', 'Boston Logan International Airport', 'Boston', 'US', 42.3656, -71.0096, 6, 'America/New_York', 4);

-- Verify insertion
SELECT COUNT(*) as airport_count FROM airports;

-- Display sample of inserted airports
SELECT id, iata, icao, name, city, country_code FROM airports ORDER BY name LIMIT 5;
