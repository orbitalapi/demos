ARG POSTGRESQL_VERSION=16.1

FROM postgres:${POSTGRESQL_VERSION}

ENV POSTGRES_DB=demoDb \
    POSTGRES_USER=admin \
    POSTGRES_PASSWORD=orbital

COPY init.sql /docker-entrypoint-initdb.d/