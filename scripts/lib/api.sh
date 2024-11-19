#!/bin/bash

show_base_usage() {
    local type=$1
    local script_name=$2
    local env=$3
    local root=$4
    local content_type=$5
    local sender=$6
    local sender_helper

    if [ "$type" = "rs" ]; then
        sender_helper=", which must be of type <sender_org>.<sender_name>"
    fi

    cat <<EOF
Usage: ./$script_name <ENDPOINT_NAME> [OPTIONS]

ENDPOINT_NAME:
    The name of the endpoint to call (required)

Options:
    -e <ENVIRONMENT>    Environment: local|staging (Default: $env)
    -f <REL_PATH>       Path to the message file to submit (Required for submission endpoints)
    -r <ROOT_PATH>      Root path to the message files (Default: $root)
    -t <CONTENT_TYPE>   Content type for the message file (Default: $content_type)
    -k <KEY_PATH>       Path to the sender private key (Required for non-local environments)
    -s <SENDER>         Sender ID used for authentication$sender_helper (Default: $sender)
    -i <SUBMISSION_ID>  Submission ID (Required for metadata/history endpoints)
    -v                  Verbose mode
    -h                  Display this help and exit
EOF
}

parse_base_args() {
    local type=$1
    shift

    [ "$1" = "-h" ] || [ "$1" = "--help" ] && return 1
    [ $# -eq 0 ] && fail "Missing required argument <ENDPOINT_NAME>"

    export HURL_FILE_PATH="$CDCTI_HOME/scripts/hurl/$type/$1.hurl"
    shift

    while getopts ':e:f:r:t:k:s:i:v' opt; do
        case "$opt" in
        e) export ENVIRONMENT="$OPTARG" ;;
        f) export REL_PATH="$OPTARG" ;;
        r) export ROOT_PATH="$OPTARG" ;;
        t) export CONTENT_TYPE="$OPTARG" ;;
        k) export SENDER_PRIVATE_KEY="$OPTARG" ;;
        s) export SENDER="$OPTARG" ;;
        i) export SUBMISSION_ID="--variable submissionid=$OPTARG" ;;
        v) export VERBOSE="--verbose" ;;
        ?) fail "Invalid option -$OPTARG" ;;
        esac
    done

    shift "$((OPTIND - 1))"
    export REMAINING_ARGS="$*"
}

setup_base_credentials() {
    local env=$1
    local sender=$2
    local private_key=$3
    local default_key_path=$4
    local default_sender=$5

    if [ -z "$private_key" ] && [ "$env" = "local" ] && [ -f "$default_key_path" ] && [ "$sender" = "$default_sender" ]; then
        export SENDER_PRIVATE_KEY="$default_key_path"
    elif [ -z "$private_key" ] && [ "$env" != "local" ]; then
        fail "Sender private key (-k) is required for non-local environments"
    fi
    [ ! -f "${SENDER_PRIVATE_KEY:-$private_key}" ] && fail "Sender private key file not found: ${SENDER_PRIVATE_KEY:-$private_key}"
}
