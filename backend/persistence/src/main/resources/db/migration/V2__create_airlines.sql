-- Create airlines table with minimal identity fields and strict name uniqueness

CREATE TABLE airlines (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Enforce case-insensitive uniqueness on airline names
CREATE UNIQUE INDEX airlines_name_lower_uniq ON airlines ((lower(name)));
