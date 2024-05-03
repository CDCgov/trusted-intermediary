#!/usr/bin/env bash
set -e

shadowJar() {
  echo "Running shadowJar..."
  ./gradlew --no-daemon clean shadowJar
}

start_api() {

    pushd ./app/

    DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    SUB_DIR="build/libs"
    JAR_NAME="app-all.jar"

    echo 'Starting API'
    export DB_URL=localhost
    export DB_PORT=5433
    export DB_NAME=intermediary
    export DB_USER=intermediary
    export DB_PASS=changeIT!
    export DB_SSL=require
    java -jar "${DIR}"/"${SUB_DIR}"/"${JAR_NAME}" > /dev/null &
    export API_PID="${!}"
    echo "API starting at PID ${API_PID}"

    popd
}

start_database() {
    echo 'Starting database'
    docker compose -f docker-compose.postgres.yml up -d
    sleep 2
    echo "Database started"
}

migrate_database() {
    echo 'Migrating database'
    liquibase update --changelog-file ./etor/databaseMigrations/root.yml --url jdbc:postgresql://localhost:5433/intermediary --username intermediary --password 'changeIT!' --label-filter '!azure'
    echo "Database migrated"
}

wait_for_api() {
    attempt_counter=0
    max_attempts=20

    until curl --output /dev/null --silent --head --fail http://localhost:8080/health; do
        if [ "${attempt_counter}" -eq "${max_attempts}" ];then
            echo 'Done waiting for API to respond'
            exit 1
        fi
        attempt_counter=$(($attempt_counter+1))
        echo 'Waiting for API to respond'
        sleep 5
    done

    echo 'API is responding'
}

run_tests() {
    echo 'Running the end-to-end tests'
    ./gradlew --no-daemon e2e:clean e2e:test
}

cleanup() {
    echo "Killing API at PID ${API_PID}"
    kill "${API_PID}"
    echo "PID ${API_PID} killed"
    echo "Stopping and deleting database"
    docker stop trusted-intermediary-postgresql-1
    docker rm -f trusted-intermediary-postgresql-1
    docker volume rm trusted-intermediary_ti_postgres_data
    echo "Database stopped and deleted"
}

trap cleanup EXIT  # Run the cleanup function on exit
start_database
migrate_database
shadowJar
start_api
wait_for_api
run_tests
