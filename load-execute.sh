#!/usr/bin/env bash
set -e

start_api() {
    echo 'Starting API'
    export DB_URL=localhost
    export DB_PORT=5433
    export DB_NAME=intermediary
    export DB_USER=intermediary
    export DB_PASS=changeIT!
    export DB_SSL=require
    ./gradlew --no-daemon app:clean app:run > /dev/null 2>&1 &
    export API_PID="${!}"
    echo "API starting at PID ${API_PID}"
}

start_database() {
    echo 'Starting database'
    docker compose -f docker-compose.postgres.yml up -d
    sleep 2
    echo "Database started"
}

migrate_database() {
    echo 'Migrating database'
    export 'PGPASSWORD=changeIT!'
    psql --set=sslmode=require -h localhost -p 5433 -d intermediary -U intermediary -c "CREATE TYPE message_status AS ENUM ('PENDING', 'DELIVERED', 'FAILED'); CREATE TABLE IF NOT EXISTS metadata (received_message_id varchar(40) PRIMARY KEY, sent_message_id varchar(40), sender varchar(30), receiver varchar(30), hash_of_order varchar(1000), time_received timestamptz, time_delivered timestamptz, delivery_status message_status, failure_reason varchar(1000));"
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

run_tests() {
    echo 'Running the load test'
    locust --headless -f ./operations/locustfile.py -H http://localhost:8080 -u 1000 -r 17 -t 5m
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
start_api
wait_for_api
run_tests
