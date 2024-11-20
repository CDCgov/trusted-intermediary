#!/bin/bash

show_base_usage() {
    local type=$1
    local script_name=$2

    local sender_helper, submission_endpoints, query_endpoints
    if [ "$type" = "rs" ]; then
        sender_helper=", of type <sender_org>.<sender_name>"
        submission_endpoints="waters endpoint"
        query_endpoints="history endpoint"
    elif [ "$type" = "ti" ]; then
        sender_helper=""
        submission_endpoints="orders and results endpoints"
        query_endpoints="orders, results and metadata endpoints"
    fi

    cat <<EOF
Usage: ./$script_name <ENDPOINT_NAME> [OPTIONS]

ENDPOINT_NAME:
    The name of the endpoint to call (required)

Options:
    -e <ENVIRONMENT>    Environment: local|staging (Default: $ENVIRONMENT)
    -f <REL_PATH>       Path to the message file to submit (Required for $submission_endpoints)
    -r <ROOT_PATH>      Root path to the message files (Default: $ROOT_PATH)
    -t <CONTENT_TYPE>   Content type for the message file (Default: $CONTENT_TYPE)
    -k <KEY_PATH>       Path to the sender private key (Required for non-local environments)
    -s <SENDER>         Sender ID used for authentication$sender_helper (Default: $SENDER)
    -i <SUBMISSION_ID>  Submission ID (Required for $query_endpoints)
    -v                  Verbose mode
    -h                  Display this help and exit
EOF
}

parse_base_args() {
    local type=$1
    shift

    [ "$1" = "-h" ] || [ "$1" = "--help" ] && return 1
    [ $# -eq 0 ] && fail "Missing required argument <ENDPOINT_NAME>"

    HURL_FILE_PATH="$CDCTI_HOME/scripts/hurl/$type/$1.hurl"
    shift

    while getopts ':e:f:r:t:k:s:i:v' opt; do
        case "$opt" in
        e) ENVIRONMENT="$OPTARG" ;;
        f) REL_PATH="$OPTARG" ;;
        r) ROOT_PATH="$OPTARG" ;;
        t) CONTENT_TYPE="$OPTARG" ;;
        k) SENDER_PRIVATE_KEY="$OPTARG" ;;
        s) SENDER="$OPTARG" ;;
        i) SUBMISSION_ID="$OPTARG" ;;
        v) VERBOSE="--verbose" ;;
        ?) fail "Invalid option -$OPTARG" ;;
        esac
    done

    shift "$((OPTIND - 1))"
    REMAINING_ARGS="$*"
}

setup_base_credentials() {
    if [ -z "$SENDER_PRIVATE_KEY" ] && [ "$ENVIRONMENT" = "local" ] && [ -f "$DEFAULT_SENDER_PRIVATE_KEY_PATH" ] && [ "$SENDER_ORG" = "$DEFAULT_SENDER_ORG" ]; then
        SENDER_PRIVATE_KEY="$DEFAULT_SENDER_PRIVATE_KEY_PATH"
    elif [ -z "$SENDER_PRIVATE_KEY" ] && [ "$ENVIRONMENT" != "local" ]; then
        fail "Sender private key (-k) is required for non-local environments"
    fi
    [ ! -f "$SENDER_PRIVATE_KEY" ] && fail "Sender private key file not found: $SENDER_PRIVATE_KEY"
}

handle_api_request() {
    local api_type=$1
    shift

    local url host jwt_token
    url=$(get_api_url "$ENVIRONMENT" "$api_type")
    host=$(extract_host_from_url "$url")
    jwt_token=$(generate_jwt "$SENDER" "$host" "$SENDER_PRIVATE_KEY")

    local vars=(
        --variable fpath=$REL_PATH
        --file-root $ROOT_PATH
        --variable url=$url
        --variable jwt=$jwt_token
        --variable content-type=$CONTENT_TYPE
    )

    if [ "$api_type" = "rs" ]; then
        parse_sender_string "$SENDER"
        vars+=(
            --variable sender-org=$SENDER_ORG
            --variable sender-name=$SENDER_NAME
        )
    elif [ "$api_type" = "ti" ]; then
        vars+=(--variable sender=$SENDER)
    fi

    if [ -n "$SUBMISSION_ID" ]; then vars+=(--variable submissionid="$SUBMISSION_ID"); fi
    if [ -n "$VERBOSE" ]; then vars+=("$VERBOSE"); fi

    hurl "${vars[@]}" "$HURL_FILE_PATH" ${REMAINING_ARGS:+$REMAINING_ARGS}
}
