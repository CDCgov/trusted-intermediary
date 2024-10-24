#!/bin/bash

source ./utils.sh

env="local"
file=""

show_usage() {
    cat <<EOF
Usage: $(basename "$0") -f <message_file.hl7> [-e <environment>]

Options:
    -f <FILE>        Message file path (required)
    -e <ENVIRONMENT> Environment: local|staging|production (Default: $DEFAULT_ENV)
    -x <KEY_PATH>    Path to the client private key for authentication with RS API
    -j <JWT>         JWT token for authentication with TI API
    -h               Display this help and exit
EOF
    exit 1
}

parse_arguments() {
    # Show help if no arguments
    [ $# -eq 0 ] && show_usage

    while getopts "e:f:x:j:h" opt; do
        case $opt in
        e) env="$OPTARG" ;;
        f) file="$OPTARG" ;;
        x) private_key="$OPTARG" ;;
        j) jwt="$OPTARG" ;;
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
    if [ "$env" = "local" ] && [ -z "$jwt" ]; then
        if [ -f "$RS_LOCAL_JWT_PATH" ]; then
            jwt=$(cat "$RS_LOCAL_JWT_PATH")
        else
            fail "Local JWT file not found at: $RS_LOCAL_JWT_PATH"
        fi
    fi

    if [ "$env" = "local" ] && [ -z "$private_key" ]; then
        if [ -f "$TI_LOCAL_PRIVATE_KEY_PATH" ]; then
            private_key="$TI_LOCAL_PRIVATE_KEY_PATH"
        else
            fail "Local environment key not found at: $TI_LOCAL_PRIVATE_KEY_PATH"
        fi
    fi

    # Validate credentials if not local environment
    if [ "$env" != "local" ]; then
        [ -z "$jwt" ] && fail "JWT (-j) is required for non-local environments"
        [ -z "$private_key" ] && fail "Private key (-x) is required for non-local environments"
    fi

    [ -n "$private_key" ] && [ ! -f "$private_key" ] && fail "Private key file not found: $private_key"
}

check_installed_commands hurl jq az
check_apis "$(get_api_url "$env" "rs")" "$(get_api_url "$env" "ti")"
parse_arguments "$@"
setup_credentials
submit_message "$env" "$file" "$private_key" "$jwt"
