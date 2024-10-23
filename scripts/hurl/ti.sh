#!/bin/bash

source ./utils.sh

LOCAL_JWT_PATH="$CDCTI_HOME/mock_credentials/report-stream-valid-token.jwt"

# default values
env=local
root=$CDCTI_HOME/examples/
client=report-stream

show_usage() {
    cat <<EOF
Usage: $(basename "$0") <ENDPOINT_NAME> [OPTIONS]

ENDPOINT_NAME:
    The name of the endpoint to call (required)

Options:
    -f <REL_PATH>         Path to the hl7/fhir file to submit (Required for orders and results APIs)
    -i <SUBMISSION_ID>    Submission ID for metadata API (Required for orders, results and metadata API)
    -r <ROOT_PATH>        Root path to the hl7/fhir files (Default: $root)
    -e <ENVIRONMENT>      Environment: local|staging (Default: $env)
    -j <JWT>              JWT token for authentication
    -v                    Verbose mode
    -h                    Display this help and exit

Environment Variables:
    CDCTI_HOME            Base directory for CDC TI repository (Required)
EOF
}

parse_arguments() {
    if [ $# -eq 0 ] || [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
        show_usage
        exit 0
    fi

    [ $# -eq 0 ] && fail "Missing required argument <ENDPOINT_NAME>"
    endpoint_name="ti/$1.hurl"
    shift # Remove endpoint name from args

    while getopts ':f:r:e:j:i:v' opt; do
        case "$opt" in
        f) fpath="$OPTARG" ;;
        i) submission_id="--variable submissionid=$OPTARG" ;;
        r) root="$OPTARG" ;;
        e) env="$OPTARG" ;;
        j) jwt="$OPTARG" ;;
        v) verbose="--verbose" ;;
        ?) fail "Invalid option -$OPTARG" ;;
        esac
    done

    shift "$(($OPTIND - 1))"
    remaining_args="$*"
}

setup_credentials() {
    if [ -z "$jwt" ] && [ "$client" = "report-stream" ] && [ "$env" = "local" ]; then
        if [ -f "$LOCAL_JWT_PATH" ]; then
            jwt=$(cat "$LOCAL_JWT_PATH")
        else
            fail "Local JWT file not found at: $LOCAL_JWT_PATH"
        fi
    fi

    [ -n "$jwt" ] || fail "Please provide the JWT for $client"
}

run_hurl_command() {
    url=$(get_api_url "$env" "ti")

    hurl \
        --variable "fpath=$fpath" \
        --file-root "$root" \
        --variable "url=$url" \
        --variable "client=$client" \
        --variable "jwt=$jwt" \
        ${submission_id:-} \
        ${verbose:-} \
        "$endpoint_name" \
        ${remaining_args:+$remaining_args}
}

check_env_vars CDCTI_HOME
parse_arguments "$@"
setup_credentials
run_hurl_command
