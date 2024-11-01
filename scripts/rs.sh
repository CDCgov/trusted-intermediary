#!/bin/bash

[ -z "${CDCTI_HOME}" ] && echo "Error: Environment variable CDCTI_HOME is not set. Please refer to /scripts/README.md for instructions" && exit 1
source "$CDCTI_HOME/scripts/lib/common.sh"

# default values
env=local
root=$CDCTI_HOME/examples/
content_type=application/hl7-v2
sender=flexion.simulated-sender

show_usage() {
    cat <<EOF
Usage: ./$(basename "$0") <ENDPOINT_NAME> [OPTIONS]

ENDPOINT_NAME:
    The name of the endpoint to call (required)

Options:
    -f <REL_PATH>       Path to the hl7 file to submit (Required for waters API)
    -r <ROOT_PATH>      Root path to the hl7 files (Default: $root)
    -e <ENVIRONMENT>    Environment: local|staging|production (Default: $env)
    -k <KEY_PATH>       Path to the sender private key (Required for non-local environments)
    -i <SUBMISSION_ID>  Submission ID for history API (Required for history API)
    -s <SENDER>         Sender ID which must be of type <sender_org>.<sender_name> (Default: $sender)
    -t <CONTENT_TYPE>   Content type for the message (Default: $content_type)
    -v                  Verbose mode
    -h                  Display this help and exit
EOF
}

parse_arguments() {
    if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
        show_usage
        exit 0
    fi

    [ $# -eq 0 ] && fail "Missing required argument <ENDPOINT_NAME>"
    hurl_file_path="$CDCTI_HOME/scripts/hurl/rs/$1.hurl"
    shift # Remove endpoint name from args

    while getopts ':f:r:e:k:i:s:t:v' opt; do
        case "$opt" in
        f) fpath="$OPTARG" ;;
        r) root="$OPTARG" ;;
        e) env="$OPTARG" ;;
        k) private_key="$OPTARG" ;;
        i) submission_id="--variable submissionid=$OPTARG" ;;
        s) sender="$OPTARG" ;;
        t) content_type="$OPTARG" ;;
        v) verbose="--verbose" ;;
        ?) fail "Invalid option -$OPTARG" ;;
        esac
    done

    parse_sender_string "$sender" sender_org sender_name

    shift "$((OPTIND - 1))"
    remaining_args="$*"
}

setup_credentials() {
    if [ -z "$private_key" ] && [ "$sender_org" = "flexion" ] && [ "$env" = "local" ]; then
        if [ -f "$TI_LOCAL_PRIVATE_KEY_PATH" ]; then
            private_key="$TI_LOCAL_PRIVATE_KEY_PATH"
        else
            fail "Local environment sender private key not found at: $TI_LOCAL_PRIVATE_KEY_PATH"
        fi
    fi

    if [ "$env" != "local" ]; then
        [ -z "$private_key" ] && fail "Sender private key (-k) is required for non-local environments"
    fi

    [ ! -f "$private_key" ] && fail "Sender private key file not found: $private_key"
}

run_hurl_command() {
    url=$(get_api_url "$env" "rs")
    host=$(extract_host_from_url "$url")
    jwt_token=$(generate_jwt "$sender_org.$sender_name" "$host" "$private_key") || fail "Failed to generate JWT token"

    hurl \
        --variable "fpath=$fpath" \
        --file-root "$root" \
        --variable "url=$url" \
        --variable "content-type=$content_type" \
        --variable "sender-org=$sender_org" \
        --variable "sender-name=$sender_name" \
        --variable "jwt=$jwt_token" \
        ${submission_id:-} \
        ${verbose:-} \
        "$hurl_file_path" \
        ${remaining_args:+$remaining_args}
}

parse_arguments "$@"
setup_credentials
run_hurl_command
