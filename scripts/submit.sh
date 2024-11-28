#!/bin/bash

[ -z "${CDCTI_HOME}" ] && echo "Error: Environment variable CDCTI_HOME is not set. Please refer to /scripts/README.md for instructions" && exit 1
source "$CDCTI_HOME/scripts/lib/common.sh"
source "$CDCTI_HOME/scripts/lib/submission-utils.sh"

env="local"

show_usage() {
    cat <<EOF
Usage: ./$(basename "$0") -f <message_file.hl7> [-e <environment>]

Options:
    -f <FILE>                   Message file path (Required)
    -e <ENVIRONMENT>            Environment: local|staging|production (Default: $env)
    -x <RS_SENDER_PRIVATE_KEY>  Path to the sender private key for authentication with RS API (Required for non-local environments)
    -z <TI_SENDER_PRIVATE_KEY>  Path to the sender private key for authentication with TI API (Optional for all environments)
    -h                          Display this help and exit
EOF
    exit 1
}

parse_arguments() {
    # Show help if no arguments
    [ $# -eq 0 ] && show_usage

    while getopts "e:f:x:z:h" opt; do
        case $opt in
        e) env="$OPTARG" ;;
        f) file="$OPTARG" ;;
        x) rs_sender_private_key="$OPTARG" ;;
        z) ti_sender_private_key="$OPTARG" ;;
        h) show_usage ;;
        ?) fail "Invalid option: -$OPTARG" ;;
        esac
    done

    [ -z "$file" ] && fail "File (-f) is required"
    [ -f "$file" ] || fail "File not found: $file"

    # Validate environment
    case "$env" in
    local | staging | production) ;;
    *) fail "Invalid environment '$env'. Must be local, staging, or production" ;;
    esac
}

setup_credentials() {
    # Handle RS sender key
    if [ "$env" = "local" ] && [ -z "$rs_sender_private_key" ]; then
        rs_sender_private_key="$TI_LOCAL_PRIVATE_KEY_PATH"
    fi

    [ "$env" != "local" ] && [ -z "$rs_sender_private_key" ] && fail "RS sender private key (-x) is required for non-local environments"
    [ ! -f "$rs_sender_private_key" ] && fail "RS sender private key file not found: $rs_sender_private_key"

    # Handle optional TI sender key
    if [ "$env" = "local" ] && [ -z "$ti_sender_private_key" ]; then
        ti_sender_private_key="$RS_LOCAL_PRIVATE_KEY_PATH"
    fi

    # Only verify TI key if provided
    [ -n "$ti_sender_private_key" ] && [ ! -f "$ti_sender_private_key" ] && fail "TI sender private key file not found: $ti_sender_private_key"
}

check_installed_commands hurl jq az
check_apis "$(get_api_url "$env" "rs")" "$(get_api_url "$env" "ti")"
parse_arguments "$@"
setup_credentials
submit_message "$env" "$file" "$rs_sender_private_key" "$ti_sender_private_key"
