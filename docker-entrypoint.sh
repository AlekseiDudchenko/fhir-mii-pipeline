#!/bin/sh
set -e

# Render's Blueprint wiring only exposes individual Postgres fields
# (PGHOST/PGPORT/PGDATABASE), not a ready-made JDBC URL, so build it here.
if [ -n "$PGHOST" ]; then
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE}"
fi

exec java -jar app.jar
