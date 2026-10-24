-- Runs once, when the Postgres container initialises an empty data volume.
--
-- This is plain SQL rather than a shell script on purpose: the Postgres image runs *.sql files
-- through psql, so nothing here depends on the execute bit, which bind mounts on Docker Desktop
-- (macOS / Windows) don't reliably carry across. The dumps are pulled in with \i.
--
-- The entrypoint runs this with ON_ERROR_STOP=1. The Pagila dumps pre-date Postgres 15 and
-- include a couple of statements that now fail harmlessly (eg., CREATE PROCEDURAL LANGUAGE plpgsql,
-- which already exists), so error-stop is switched off for the imports.

-- The dumps were taken by a user called "postgres" and ALTER ... OWNER TO postgres throughout.
-- The container's superuser is "root" (see docker-compose.yml), so create the role the dumps expect.
CREATE ROLE postgres;

\echo 'Importing the Pagila schema into' :DBNAME
\set ON_ERROR_STOP off
\i /docker-entrypoint-initdb.d/dump/pagila-schema.sql
\echo 'Importing the Pagila data'
\i /docker-entrypoint-initdb.d/dump/pagila-data.sql
\set ON_ERROR_STOP on
\echo 'Pagila import finished'
