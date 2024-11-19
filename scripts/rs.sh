#!/bin/bash

# set -x

[ -z "${CDCTI_HOME}" ] && echo "Error: Environment variable CDCTI_HOME is not set. Please refer to /scripts/README.md for instructions" && exit 1
source "$CDCTI_HOME/scripts/lib/common.sh"
source "$CDCTI_HOME/scripts/lib/api.sh"

# default values
ENVIRONMENT=local
ROOT_PATH=$CDCTI_HOME/examples/
CONTENT_TYPE=application/hl7-v2
SENDER=flexion.simulated-sender

handle_request() {
    parse_base_args "rs" "$@" || {
        show_base_usage "rs" "$(basename "$0")" "$ENVIRONMENT" "$ROOT_PATH" "$CONTENT_TYPE" "$SENDER"
        exit 0
    }

    parse_sender_string "$SENDER" sender_org sender_name
    setup_base_credentials "$ENVIRONMENT" "$sender_org" "$SENDER_PRIVATE_KEY" "$TI_LOCAL_PRIVATE_KEY_PATH" "flexion"

    local url host jwt_token
    url=$(get_api_url "$ENVIRONMENT" "rs")
    host=$(extract_host_from_url "$url")
    jwt_token=$(generate_jwt "$SENDER" "$host" "$SENDER_PRIVATE_KEY")

    hurl \
        --variable "fpath=$REL_PATH" \
        --file-root "$ROOT_PATH" \
        --variable "url=$url" \
        --variable "content-type=$CONTENT_TYPE" \
        --variable "sender-org=$sender_org" \
        --variable "sender-name=$sender_name" \
        --variable "jwt=$jwt_token" \
        ${SUBMISSION_ID:-} \
        ${VERBOSE:-} \
        "$HURL_FILE_PATH" \
        ${REMAINING_ARGS:+$REMAINING_ARGS}
}

handle_request "$@"
