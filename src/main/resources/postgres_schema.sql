-- Simplified PostgreSQL schema for Spring Boot auto-init (Render Free Tier)

-- Core tables only (matches controllers exactly)
CREATE TABLE IF NOT EXISTS events (
    event_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    location VARCHAR(255) NOT NULL,
    description TEXT,
    guest_limit INTEGER NOT NULL CHECK (guest_limit > 0),
    current_guests INTEGER DEFAULT 0 CHECK (current_guests >= 0),
    event_type VARCHAR(20) NOT NULL DEFAULT 'SIMPLE',
    parent_event_id INTEGER REFERENCES events(event_id) ON DELETE CASCADE,
    image_path TEXT,
    CHECK (current_guests <= guest_limit)
);;

CREATE TABLE IF NOT EXISTS bookings (
    booking_id SERIAL PRIMARY KEY,
    event_id INTEGER NOT NULL REFERENCES events(event_id) ON DELETE CASCADE,
    user_name VARCHAR(255) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    digital_id VARCHAR(255) NOT NULL,
    booking_type VARCHAR(20) NOT NULL DEFAULT 'SUB_EVENT',
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_email, event_id)
);;

CREATE TABLE IF NOT EXISTS app_users (
    user_id SERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);;

CREATE OR REPLACE FUNCTION validate_sub_event_capacity() RETURNS trigger AS $$
DECLARE
    parent_limit INTEGER;
    parent_type VARCHAR(20);
    sibling_total INTEGER;
BEGIN
    IF NEW.parent_event_id IS NOT NULL THEN
        SELECT guest_limit, event_type INTO parent_limit, parent_type FROM events WHERE event_id = NEW.parent_event_id;
        IF parent_type <> 'MAJOR' THEN
            RAISE EXCEPTION 'Sub-events must belong to a major event';
        END IF;
        SELECT COALESCE(SUM(guest_limit), 0) INTO sibling_total
        FROM events
        WHERE parent_event_id = NEW.parent_event_id
          AND event_id <> COALESCE(NEW.event_id, -1);

        IF sibling_total + NEW.guest_limit > parent_limit THEN
            RAISE EXCEPTION 'Combined sub-event guest limits cannot exceed parent major event guest limit';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;;

DROP TRIGGER IF EXISTS trg_validate_sub_event_capacity ON events;;
CREATE TRIGGER trg_validate_sub_event_capacity
BEFORE INSERT OR UPDATE OF guest_limit, parent_event_id ON events
FOR EACH ROW EXECUTE FUNCTION validate_sub_event_capacity();;
