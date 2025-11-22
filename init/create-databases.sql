-- =======================================
-- Router DB
-- =======================================
CREATE DATABASE routerdb;
CREATE USER router WITH PASSWORD 'router';
GRANT ALL PRIVILEGES ON DATABASE routerdb TO router;

\connect routerdb;

-- Change schema owner
ALTER SCHEMA public OWNER TO router;

-- Grant full permissions (required for Hibernate)
GRANT ALL PRIVILEGES ON SCHEMA public TO router;


-- =======================================
-- Uploader DB
-- =======================================
CREATE DATABASE uploaderdb;
CREATE USER uploader WITH PASSWORD 'uploader';
GRANT ALL PRIVILEGES ON DATABASE uploaderdb TO uploader;

\connect uploaderdb;

-- Change schema owner
ALTER SCHEMA public OWNER TO uploader;

-- Grant full permissions (required for Hibernate)
GRANT ALL PRIVILEGES ON SCHEMA public TO uploader;
