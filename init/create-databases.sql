-- Router DB
CREATE DATABASE routerdb;
CREATE USER router WITH PASSWORD 'router';
GRANT ALL PRIVILEGES ON DATABASE routerdb TO router;

\connect routerdb;
ALTER SCHEMA public OWNER TO router;


-- Uploader DB
CREATE DATABASE uploaderdb;
CREATE USER uploader WITH PASSWORD 'uploader';
GRANT ALL PRIVILEGES ON DATABASE uploaderdb TO uploader;

\connect uploaderdb;
ALTER SCHEMA public OWNER TO uploader;
