#!/bin/bash

[ -z "${CDCTI_HOME}" ] && echo "Error: Environment variable CDCTI_HOME is not set" && exit 1
source "$CDCTI_HOME/scripts/utils.sh"
source "$CDCTI_HOME/scripts/hurl/utils.sh"

# default values
env=local
root=$CDCTI_HOME/examples/
content_type=application/hl7-v2
client_id=flexion
client_sender=simulated-sender

show_usage() {
    cat <<EOF
Usage: $(basename "$0") <ENDPOINT_NAME> [OPTIONS]

ENDPOINT_NAME:
    The name of the endpoint to call (required)

Options:
    -f <REL_PATH>       Path to the hl7/fhir file to submit (Required for waters API)
    -r <ROOT_PATH>      Root path to the hl7/fhir files (Default: $root)
    -t <CONTENT_TYPE>   Content type for the message (Default: $content_type)
    -e <ENVIRONMENT>    Environment: local|staging|production (Default: $env)
    -c <CLIENT_ID>      Client ID (Default: $client_id)
    -s <CLIENT_SENDER>  Client sender (Default: $client_sender)
    -k <KEY_PATH>       Path to the client private key (Required for non-local environments)
    -i <SUBMISSION_ID>  Submission ID for history API (Required for history API)
    -v                  Verbose mode
    -h                  Display this help and exit

Environment Variables:
    CDCTI_HOME          Base directory for CDC TI repository (Required)
EOF
}

parse_arguments() {
    if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
        show_usage
        exit 0
    fi

    [ $# -eq 0 ] && fail "Missing required argument <ENDPOINT_NAME>"
    endpoint_name="rs/$1.hurl"
    shift # Remove endpoint name from args

    while getopts ':f:r:t:e:c:s:k:i:v' opt; do
        case "$opt" in
        f) fpath="$OPTARG" ;;
        r) root="$OPTARG" ;;
        t) content_type="$OPTARG" ;;
        e) env="$OPTARG" ;;
        c) client_id="$OPTARG" ;;
        s) client_sender="$OPTARG" ;;
        k) private_key="$OPTARG" ;;
        i) submission_id="--variable submissionid=$OPTARG" ;;
        v) verbose="--verbose" ;;
        ?) fail "Invalid option -$OPTARG" ;;
        esac
    done

    shift "$(($OPTIND - 1))"
    remaining_args="$*"
}

setup_credentials() {
    if [ -z "$private_key" ] && [ "$client_id" = "flexion" ] && [ "$env" = "local" ]; then
        if [ -f "$TI_LOCAL_PRIVATE_KEY_PATH" ]; then
            private_key="$TI_LOCAL_PRIVATE_KEY_PATH"
        else
            fail "Local environment client private key not found at: $TI_LOCAL_PRIVATE_KEY_PATH"
        fi
    fi

    if [ "$env" != "local" ]; then
        [ -z "$private_key" ] && fail "Client private key (-k) is required for non-local environments"
    fi

    [ ! -f "$private_key" ] && fail "Client private key file not found: $private_key"
}

run_hurl_command() {
    url=$(get_api_url "$env" "rs")
    host=$(extract_host_from_url "$url")
    jwt_token=$(generate_jwt "$client_id.$client_sender" "$host" "$private_key") || fail "Failed to generate JWT token"

    hurl \
        --variable "fpath=$fpath" \
        --file-root "$root" \
        --variable "url=$url" \
        --variable "content-type=$content_type" \
        --variable "client-id=$client_id" \
        --variable "client-sender=$client_sender" \
        --variable "jwt=$jwt_token" \
        ${submission_id:-} \
        ${verbose:-} \
        "$endpoint_name" \
        ${remaining_args:+$remaining_args}
}

parse_arguments "$@"
setup_credentials
run_hurl_command
