#!/usr/bin/env bash
set -e

start_api() {
    echo 'Starting API'
    ./gradlew --no-daemon clean app:run &
    export API_PID="${!}"
    echo "API starting at PID ${API_PID}"
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
}

trap cleanup EXIT  # Run the cleanup function on exit
start_api
wait_for_api
run_tests
