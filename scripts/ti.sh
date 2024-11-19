#!/bin/bash

# set -x

[ -z "${CDCTI_HOME}" ] && echo "Error: Environment variable CDCTI_HOME is not set. Please refer to /scripts/README.md for instructions" && exit 1
source "$CDCTI_HOME/scripts/lib/common.sh"
source "$CDCTI_HOME/scripts/lib/api.sh"

# default values
ENVIRONMENT=local
ROOT_PATH=$CDCTI_HOME/examples/
CONTENT_TYPE=application/fhir+ndjson
SENDER=report-stream

handle_request() {
    parse_base_args "ti" "$@" || {
        show_base_usage "ti" "$(basename "$0")" "$ENVIRONMENT" "$ROOT_PATH" "$CONTENT_TYPE" "$SENDER"
        exit 0
    }

    setup_base_credentials "$ENVIRONMENT" "$SENDER" "$SENDER_PRIVATE_KEY" "$RS_LOCAL_PRIVATE_KEY_PATH" "report-stream"

    local url host jwt_token
    url=$(get_api_url "$ENVIRONMENT" "ti")
    host=$(extract_host_from_url "$url")
    jwt_token=$(generate_jwt "$SENDER" "$host" "$SENDER_PRIVATE_KEY")

    hurl \
        --variable "fpath=$REL_PATH" \
        --file-root "$ROOT_PATH" \
        --variable "url=$url" \
        --variable "sender=$SENDER" \
        --variable "jwt=$jwt_token" \
        ${SUBMISSION_ID:-} \
        ${VERBOSE:-} \
        "$HURL_FILE_PATH" \
        ${REMAINING_ARGS:+$REMAINING_ARGS}
}

handle_request "$@"
