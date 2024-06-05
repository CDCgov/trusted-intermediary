#!/usr/bin/env bash
set -e

start_api() {
    echo 'Starting API'
    export DB_URL=postgresql
    export DB_PORT=5432
    export DB_NAME=intermediary-test
    export DB_USER=intermediary
    export DB_PASS=changeIT!
    export DB_SSL=require
    ./gradlew shadowJar
    docker compose up --build -d
    export API_PID="${!}"
    echo "API starting at PID ${API_PID}"
}

start_database() {
    echo 'Starting database'
    docker compose -f docker-compose.postgres-test.yml up -d
    sleep 2
    echo "Database started"
}
migrate_database() {
    echo 'Migrating database'
    liquibase update --changelog-file ./etor/databaseMigrations/root.yml --url jdbc:postgresql://localhost:5433/intermediary-test --username intermediary --password 'changeIT!' --label-filter '!azure'
    echo "Database migrated"
}

wait_for_api() {
    attempt_counter=0
    max_attempts=36

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

warm_up_api() {
    echo 'Warming up API...'
    local endpoint=(
        "http://localhost:8080/v1/etor/token"
        "http://localhost:8080/v1/etor/orders"
        "http://localhost:8080/v1/etor/results"
        "http://localhost:8080/v1/etor/metadata/1"
        "http://localhost:8080/v1/etor/results/1"
    )

    for endpoint in "${endpoints[@]}"; do
      for i in {1..2}; do
        curl --output /dev/null --silent --request POST "$endpoint"
      done
    done

    sleep 15
    echo 'API is cozy'
}

run_tests() {
    echo 'Running the load test'
    locust --headless -f ./operations/locustfile.py -H http://localhost:8080 -u 1000 -r 15 -t 5m
}

cleanup() {
    echo "Stopping API docker container"
    docker compose down
    echo "API Docker container stopped"
    echo "Stopping and deleting database"
    docker compose -f docker-compose.postgres-test.yml down -v
    echo "Database stopped and deleted"
}

trap cleanup EXIT  # Run the cleanup function on exit
start_database
migrate_database
start_api
wait_for_api
warm_up_api
run_tests
