#!/bin/bash
set -e

ls -ltra /docker-entrypoint-initdb.d/
#echo 'Creating Orbital user`'
psql --username $POSTGRES_USER -d $POSTGRES_DB < /docker-entrypoint-initdb.d/orbital-user.sql
echo 'Importing data into db $POSTGRES_DB'
psql --username $POSTGRES_USER -d $POSTGRES_DB < /docker-entrypoint-initdb.d/pagila-schema.sql
echo 'Importing schema finished successfully'
psql --username $POSTGRES_USER -d $POSTGRES_DB < /docker-entrypoint-initdb.d/pagila-insert-data.sql
echo 'Importing insert-data finished successfully'
psql --username $POSTGRES_USER -d $POSTGRES_DB < /docker-entrypoint-initdb.d/pagila-data.sql
echo 'Importing data finished successfully'

echo 'Data import finished successfully'
