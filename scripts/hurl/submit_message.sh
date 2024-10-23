#!/bin/bash

source ./message_submission_utils.sh

env="local"
file=""

show_usage() {
    echo "Usage: $0 -f <message_file.hl7> [-e <environment>]"
    echo "Options:"
    echo "  -f  Message file path (required)"
    echo "  -e  Environment (default: $env)"
    exit 1
}

# Parse command line arguments
while getopts "e:f:h" opt; do
    case $opt in
    e) env="$OPTARG" ;;
    f) file="$OPTARG" ;;
    h) show_usage ;;
    \?)
        echo "Invalid option: -$OPTARG"
        show_usage
        ;;
    esac
done

# Check if file was provided
if [ -z "$file" ]; then
    show_usage
fi

rs_url=$(get_endpoint_url "rs" "$env")
ti_url=$(get_endpoint_url "ti" "$env")

check_env_vars CDCTI_HOME
check_installed_commands hurl jq az
check_apis "$rs_url" "$ti_url"

submit_message "$2"
