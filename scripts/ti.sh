#!/bin/bash

[ -z "${CDCTI_HOME}" ] && echo "Error: Environment variable CDCTI_HOME is not set. Please refer to /scripts/README.md for instructions" && exit 1
source "$CDCTI_HOME/scripts/lib/common.sh"
source "$CDCTI_HOME/scripts/lib/api.sh"

# default values
DEFAULT_SENDER_PRIVATE_KEY_ORG=report-stream
DEFAULT_SENDER_PRIVATE_KEY_PATH="$RS_LOCAL_PRIVATE_KEY_PATH"
ENVIRONMENT=local
ROOT_PATH=$CDCTI_HOME/examples/
CONTENT_TYPE=application/fhir+ndjson
SENDER=report-stream

parse_args "ti" "$@" || {
    show_usage "$(basename "$0")"
    exit 0
}

handle_api_request "ti" "$@"
