#!/bin/bash

# Script to check health of API. It sends a curl command to the health endpoint and checks
# that the status code is 200.
# Note: This script can only be used when the application is running inside a container.

set -e

echo "API Health Check..."

API_HEALTH_CHECK_PASSED="PASS: API Health Check"
API_HEALTH_CHECK_FAILED="FAIL: API Health Check"

wait() {
  sleep 5
}

health_check() {
    attempts=0
    max_attempts=10
    URL="http://$(ip -f inet -o addr show docker0 | awk '{print $4}' | cut -d '/' -f 1):8080/health"
    HTTP_CODE=0

    until HTTP_CODE=$(curl -s -o /dev/null -L -w '%{http_code}\n' $URL); do
        if [[ "${attempts}" -eq "${max_attempts}" ]];then
            echo 'FAIL: API to Respond'
            exit 1
        fi
        ((attempts=attempts+1))
        echo 'Waiting for API to respond...'
        wait
    done

      if [[ "$HTTP_CODE" -ne 200 ]]; then
        echo "$API_HEALTH_CHECK_FAILED"
        echo "Status Code: $HTTP_CODE"
        exit 1
      else
        echo "$API_HEALTH_CHECK_PASSED"
        echo "Status Code: $HTTP_CODE"
        exit 0
      fi
}

# main
health_check
