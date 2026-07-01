#!/bin/sh
set -e

# Render's Blueprint spec can't interpolate ${VAR} inside envVars.value, so the
# JDBC URL is assembled here from the discrete DB_HOST/DB_PORT/DB_NAME vars
# Render injects via fromDatabase. Local docker-compose sets
# SPRING_DATASOURCE_URL directly, so this is skipped in that case.
if [ -z "$SPRING_DATASOURCE_URL" ] && [ -n "$DB_HOST" ]; then
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT:-5432}/${DB_NAME}"
fi

# Render web services must listen on the port it assigns via $PORT.
if [ -n "$PORT" ]; then
  export SERVER_PORT="$PORT"
fi

exec java -jar app.jar
