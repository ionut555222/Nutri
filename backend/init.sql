-- PostgreSQL Database Initialization Script
-- This script runs when the database is first created

-- Create database extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set timezone
SET timezone = 'UTC';

-- Create any initial data or configurations here
-- (The application will handle table creation via JPA/Hibernate)

-- Log initialization
SELECT 'Database initialized successfully' as status; 