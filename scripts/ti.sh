#!/bin/bash

[ -z "${CDCTI_HOME}" ] && echo "Error: Environment variable CDCTI_HOME is not set. Please refer to /scripts/README.md for instructions" && exit 1
source "$CDCTI_HOME/scripts/lib/common.sh"

# default values
env=local
root=$CDCTI_HOME/examples/
content_type=application/fhir+ndjson
sender=report-stream

show_usage() {
    cat <<EOF
Usage: ./$(basename "$0") <ENDPOINT_NAME> [OPTIONS]

ENDPOINT_NAME:
    The name of the endpoint to call (required)

Options:
    -f <REL_PATH>       Path to the fhir file to submit (Required for orders and results APIs)
    -r <ROOT_PATH>      Root path to the fhir files (Default: $root)
    -e <ENVIRONMENT>    Environment: local|staging (Default: $env)
    -k <KEY_PATH>       Path to the sender private key (Required for non-local environments)
    -i <SUBMISSION_ID>  Submission ID for metadata API (Required for orders, results and metadata API)
    -s <SENDER>         Sender ID to create JWT with (Default: $sender)
    -t <CONTENT_TYPE>   Content type for the message (Default: $content_type)
    -v                  Verbose mode
    -h                  Display this help and exit
EOF
}

parse_arguments() {
    if [ $# -eq 0 ] || [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
        show_usage
        exit 0
    fi

    [ $# -eq 0 ] && fail "Missing required argument <ENDPOINT_NAME>"
    hurl_file_path="$CDCTI_HOME/scripts/hurl/ti/$1.hurl"
    shift # Remove endpoint name from args

    while getopts ':f:r:e:k:i:s:t:v' opt; do
        case "$opt" in
        f) fpath="$OPTARG" ;;
        r) root="$OPTARG" ;;
        e) env="$OPTARG" ;;
        k) private_key="$OPTARG" ;;
        i) submission_id="--variable submissionid=$OPTARG" ;;
        s) sender="$OPTARG" ;;
        v) verbose="--verbose" ;;
        ?) fail "Invalid option -$OPTARG" ;;
        esac
    done

    shift "$((OPTIND - 1))"
    remaining_args="$*"
}

setup_credentials() {
    if [ -z "$private_key" ] && [ "$sender" = "report-stream" ] && [ "$env" = "local" ]; then
        if [ -f "$RS_LOCAL_PRIVATE_KEY_PATH" ]; then
            private_key="$RS_LOCAL_PRIVATE_KEY_PATH"
        else
            fail "Local environment sender private key not found at: $RS_LOCAL_PRIVATE_KEY_PATH"
        fi
    fi

    if [ "$env" != "local" ]; then
        [ -z "$private_key" ] && fail "Sender private key (-k) is required for non-local environments"
    fi

    [ ! -f "$private_key" ] && fail "Sender private key file not found: $private_key"
}

run_hurl_command() {
    url=$(get_api_url "$env" "ti")
    host=$(extract_host_from_url "$url")
    jwt_token=$(generate_jwt "$sender" "$host" "$private_key") || fail "Failed to generate JWT token"

    hurl \
        --variable "fpath=$fpath" \
        --file-root "$root" \
        --variable "url=$url" \
        --variable "sender=$sender" \
        --variable "jwt=$jwt_token" \
        ${submission_id:-} \
        ${verbose:-} \
        "$hurl_file_path" \
        ${remaining_args:+$remaining_args}
}

parse_arguments "$@"
setup_credentials
run_hurl_command
