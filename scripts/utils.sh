#!/bin/bash

load_env() {
    local script_dir env_file
    script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    env_file="${script_dir}/.env"
    if [[ -f "$env_file" ]]; then
        source "$env_file"
    else
        echo "Error: $env_file file not found" >&2
        return 1
    fi
}
load_env || exit 1

fail() {
    echo "Error: $1" >&2
    exit 1
}

check_env_vars() {
    local env_vars=("$@")
    for var in "${env_vars[@]}"; do
        if [ -z "${!var}" ]; then
            echo "Error: Environment variable '$var' is not set"
            exit 1
        fi
    done
}

check_installed_commands() {
    for cmd in "$@"; do
        if ! command -v "$cmd" &>/dev/null; then
            echo "$cmd could not be found. Please install $cmd to proceed."
            exit 1
        fi
    done
}

check_apis() {
    for service in "$@"; do
        if ! curl -s --head --fail "$service" | grep "200 OK" >/dev/null; then
            echo "The service at $service is not reachable"
            exit 1
        fi
    done
}
