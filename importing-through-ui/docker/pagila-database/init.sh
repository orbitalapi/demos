#!/bin/bash
set -e

ls -ltra /docker-entrypoint-initdb.d/
ls -ltra /docker-entrypoint-initdb.d/dump
echo 'Creating Orbital user`'
psql --username $POSTGRES_USER -d $POSTGRES_DB < /docker-entrypoint-initdb.d/dump/orbital-user.sql
echo 'Importing data into db $POSTGRES_DB'
psql --username $POSTGRES_USER -d $POSTGRES_DB < /docker-entrypoint-initdb.d/dump/pagila-schema.sql
echo 'Importing schema finished successfully'
psql --username $POSTGRES_USER -d $POSTGRES_DB < /docker-entrypoint-initdb.d/dump/pagila-insert-data.sql
echo 'Importing insert-data finished successfully'
psql --username $POSTGRES_USER -d $POSTGRES_DB < /docker-entrypoint-initdb.d/dump/pagila-data.sql
echo 'Importing data finished successfully'

echo 'Data import finished successfully'
