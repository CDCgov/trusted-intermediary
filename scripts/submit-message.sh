#!/bin/bash

[ -z "${CDCTI_HOME}" ] && echo "Error: Environment variable CDCTI_HOME is not set" && exit 1
source "$CDCTI_HOME/scripts/utils.sh"
source "$CDCTI_HOME/scripts/helper.sh"

env="local"

show_usage() {
    cat <<EOF
Usage: $(basename "$0") -f <message_file.hl7> [-e <environment>]

Options:
    -f <FILE>                   Message file path (Required)
    -e <ENVIRONMENT>            Environment: local|staging|production (Default: $DEFAULT_ENV)
    -x <RS_CLIENT_PRIVATE_KEY>  Path to the client private key for authentication with RS API (Required for non-local environments)
    -z <TI_CLIENT_PRIVATE_KEY>  Path to the client private key for authentication with TI API (Optional for all environments)
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
    # Handle RS client key
    if [ "$env" = "local" ] && [ -z "$rs_client_private_key" ]; then
        rs_client_private_key="$TI_LOCAL_PRIVATE_KEY_PATH"
    fi

    [ "$env" != "local" ] && [ -z "$rs_client_private_key" ] && fail "RS client private key (-x) is required for non-local environments"
    [ ! -f "$rs_client_private_key" ] && fail "RS client private key file not found: $rs_client_private_key"

    # Handle optional TI client key
    if [ "$env" = "local" ] && [ -z "$ti_client_private_key" ]; then
        ti_client_private_key="$RS_LOCAL_PRIVATE_KEY_PATH"
    fi

    # Only verify TI key if provided
    [ -n "$ti_client_private_key" ] && [ ! -f "$ti_client_private_key" ] && fail "TI client private key file not found: $ti_client_private_key"
}

check_installed_commands hurl jq az
check_apis "$(get_api_url "$env" "rs")" "$(get_api_url "$env" "ti")"
parse_arguments "$@"
setup_credentials
submit_message "$env" "$file" "$rs_client_private_key" "$ti_client_private_key"
