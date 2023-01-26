#!/bin/bash

# Script to check health of API. It sends a curl command to the health endpoint and checks
# that the status code is 200.
# Note: This script can only be used when the application is running inside a container.

set -e

echo "API health check..."

CONTAINER_NAME="trusted-intermediary-router-1"
CONTAINER_PASSED="PASSED: Container is running"
CONTAINER_FAILED="FAILED: Container is running"
API_HEALTH_CHECK_PASSED="PASSED: API health check"
API_HEALTH_CHECK_FAILED="FAILED: API health check"

if docker ps --format '{{.Names}}' | grep -Eq "^${CONTAINER_NAME}\$"; then
  echo $CONTAINER_PASSED
else
  echo $CONTAINER_FAILED
  echo $API_HEALTH_CHECK_FAILED
  exit 1
fi

URL="http://$(ip -f inet -o addr show docker0 | awk '{print $4}' | cut -d '/' -f 1):8080/health"
HTTP_CODE=$(curl -s -o /dev/null -L -w '%{http_code}\n' $URL)

if [[ "$HTTP_CODE" -ne 200 ]]; then
  echo $API_HEALTH_CHECK_FAILED
  echo "status code: $HTTP_CODE"
else
  echo $API_HEALTH_CHECK_PASSED
  echo "Status code: $HTTP_CODE"
  exit 0
fi
