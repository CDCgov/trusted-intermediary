#!/bin/bash

[ -z "${CDCTI_HOME}" ] && echo "Error: Environment variable CDCTI_HOME is not set" && exit 1
source "$CDCTI_HOME/scripts/lib/common.sh"
source "$CDCTI_HOME/scripts/lib/submission-utils.sh"

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
    -f <REL_PATH>       Path to the hl7/fhir file to submit (Required for orders and results APIs)
    -r <ROOT_PATH>      Root path to the hl7/fhir files (Default: $root)
    -e <ENVIRONMENT>    Environment: local|staging (Default: $env)
    -c <CLIENT>         Client ID to create JWT with (Default: $client)
    -k <KEY_PATH>       Path to the client private key (Required for non-local environments)
    -i <SUBMISSION_ID>  Submission ID for metadata API (Required for orders, results and metadata API)
    -v                  Verbose mode
    -h                  Display this help and exit

Environment Variables:
    CDCTI_HOME          Base directory for CDC TI repository (Required)
EOF
}

parse_arguments() {
    if [ $# -eq 0 ] || [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
        show_usage
        exit 0
    fi

    [ $# -eq 0 ] && fail "Missing required argument <ENDPOINT_NAME>"
    endpoint_name="hurl/ti/$1.hurl"
    shift # Remove endpoint name from args

    while getopts ':f:r:e:c:k:i:v' opt; do
        case "$opt" in
        f) fpath="$OPTARG" ;;
        r) root="$OPTARG" ;;
        e) env="$OPTARG" ;;
        c) client="$OPTARG" ;;
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
    if [ -z "$private_key" ] && [ "$client" = "report-stream" ] && [ "$env" = "local" ]; then
        if [ -f "$RS_LOCAL_PRIVATE_KEY_PATH" ]; then
            private_key="$RS_LOCAL_PRIVATE_KEY_PATH"
        else
            fail "Local environment client private key not found at: $RS_LOCAL_PRIVATE_KEY_PATH"
        fi
    fi

    if [ "$env" != "local" ]; then
        [ -z "$private_key" ] && fail "Client private key (-k) is required for non-local environments"
    fi

    [ ! -f "$private_key" ] && fail "Client private key file not found: $private_key"
}

run_hurl_command() {
    url=$(get_api_url "$env" "ti")
    host=$(extract_host_from_url "$url")
    jwt_token=$(generate_jwt "$client" "$host" "$private_key") || fail "Failed to generate JWT token"

    hurl \
        --variable "fpath=$fpath" \
        --file-root "$root" \
        --variable "url=$url" \
        --variable "client=$client" \
        --variable "jwt=$jwt_token" \
        ${submission_id:-} \
        ${verbose:-} \
        "$endpoint_name" \
        ${remaining_args:+$remaining_args}
}

parse_arguments "$@"
setup_credentials
run_hurl_command
