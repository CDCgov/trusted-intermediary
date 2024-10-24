#!/bin/bash

source ./utils.sh

env="local"

show_usage() {
    cat <<EOF
Usage: $(basename "$0") -f <message_file.hl7> [-e <environment>]

Options:
    -f <FILE>                   Message file path (required)
    -e <ENVIRONMENT>            Environment: local|staging|production (Default: $DEFAULT_ENV)
    -x <RS_CLIENT_PRIVATE_KEY>  Path to the client private key for authentication with RS API
    -z <TI_CLIENT_PRIVATE_KEY>  Path to the client private key authentication with TI API
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
        x) rs_client_private_key="$OPTARG" ;;
        z) ti_client_private_key="$OPTARG" ;;
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
    # Set default credentials for local environment
    if [ "$env" = "local" ] && [ -z "$rs_client_private_key" ]; then
        if [ -f "$RS_CLIENT_LOCAL_PRIVATE_KEY_PATH" ]; then
            rs_client_private_key="$RS_CLIENT_LOCAL_PRIVATE_KEY_PATH"
        else
            fail "Local environment RS client private key not found at: $RS_CLIENT_LOCAL_PRIVATE_KEY_PATH"
        fi
    fi

    if [ "$env" = "local" ] && [ -z "$ti_client_private_key" ]; then
        if [ -f "$TI_CLIENT_LOCAL_PRIVATE_KEY_PATH" ]; then
            ti_client_private_key="$TI_CLIENT_LOCAL_PRIVATE_KEY_PATH"
        else
            fail "Local environment TI client private key not found at: $TI_CLIENT_LOCAL_PRIVATE_KEY_PATH"
        fi
    fi

    if [ "$env" != "local" ]; then
        [ -z "$rs_client_private_key" ] && fail "RS client private key (-x) is required for non-local environments"
        [ -z "$ti_client_private_key" ] && fail "TI client private key (-z) is required for non-local environments"
    fi

    [ ! -f "$rs_client_private_key" ] && fail "RS client private key file not found: $rs_client_private_key"
    [ ! -f "$ti_client_private_key" ] && fail "TI client private key file not found: $ti_client_private_key"
}

check_installed_commands hurl jq az
check_apis "$(get_api_url "$env" "rs")" "$(get_api_url "$env" "ti")"
parse_arguments "$@"
setup_credentials
submit_message "$env" "$file" "$rs_client_private_key" "$ti_client_private_key"
