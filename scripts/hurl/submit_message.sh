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
    -h               Display this help and exit
EOF
    exit 1
}

parse_arguments() {
    # Show help if no arguments
    [ $# -eq 0 ] && show_usage

    while getopts "e:f:h" opt; do
        case $opt in
        e) env="$OPTARG" ;;
        f) file="$OPTARG" ;;
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

check_installed_commands hurl jq az
check_apis "$(get_api_url "$env" "rs")" "$(get_api_url "$env" "ti")"
parse_arguments "$@"
submit_message "$file"
