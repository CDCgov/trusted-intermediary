#!/bin/bash

show_usage() {
    local script_name=$1

    cat <<EOF
Usage: ./$script_name <ENDPOINT_NAME> [OPTIONS]

ENDPOINT_NAME:
    The name of the endpoint to call (required)

Options:
    -e <ENVIRONMENT>    Environment: local|staging (Default: $ENVIRONMENT)
    -f <REL_PATH>       Path to the message file to submit (Required for submission endpoints)
    -r <ROOT_PATH>      Root path to the message files (Default: $ROOT_PATH)
    -t <CONTENT_TYPE>   Content type for the message file (Default: $CONTENT_TYPE)
    -k <KEY_PATH>       Path to the sender private key (Required for non-local environments)
    -s <SENDER>         Sender ID used for authentication (Default: $SENDER)
    -u <URL>            URL root for the API endpoint (Default: auto-detected)
    -i <SUBMISSION_ID>  Submission ID (Required for query endpoints)
    -v                  Verbose mode
    -h                  Display this help and exit
EOF
}

parse_args() {
    local type=$1
    shift

    [ "$1" = "-h" ] || [ "$1" = "--help" ] && return 1
    [ $# -eq 0 ] && fail "Missing required argument <ENDPOINT_NAME>"

    HURL_FILE_PATH="$CDCTI_HOME/scripts/hurl/$type/$1.hurl"
    shift

    while getopts ':e:f:r:t:k:s:u:i:v' opt; do
        case "$opt" in
        e) ENVIRONMENT="$OPTARG" ;;
        f) REL_PATH="$OPTARG" ;;
        r) ROOT_PATH="$OPTARG" ;;
        t) CONTENT_TYPE="$OPTARG" ;;
        k) SENDER_PRIVATE_KEY="$OPTARG" ;;
        s) SENDER="$OPTARG" ;;
        u) URL="$OPTARG" ;;
        i) SUBMISSION_ID="$OPTARG" ;;
        v) VERBOSE="--verbose" ;;
        ?) fail "Invalid option -$OPTARG" ;;
        esac
    done

    shift "$((OPTIND - 1))"
    REMAINING_ARGS="$*"
}

setup_credentials() {
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

    local host jwt_token
    [ -z "$URL" ] && URL=$(get_api_url "$ENVIRONMENT" "$api_type")
    host=$(extract_host_from_url "$URL")
    jwt_token=$(generate_jwt "$SENDER" "$host" "$SENDER_PRIVATE_KEY")

    local vars=(
        --variable fpath=$REL_PATH
        --file-root $ROOT_PATH
        --variable url=$URL
        --variable jwt=$jwt_token
        --variable content-type=$CONTENT_TYPE
    )

    if [ "$api_type" = "rs" ]; then
        parse_sender_string "$SENDER"
        vars+=(
            --variable sender-org=$SENDER_ORG
            --variable sender-name=$SENDER_NAME
        )
    else
        vars+=(--variable sender=$SENDER)
    fi

    if [ -n "$SUBMISSION_ID" ]; then vars+=(--variable submissionid="$SUBMISSION_ID"); fi
    if [ -n "$VERBOSE" ]; then vars+=("$VERBOSE"); fi

    hurl "${vars[@]}" "$HURL_FILE_PATH" ${REMAINING_ARGS:+$REMAINING_ARGS}
}
