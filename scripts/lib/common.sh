#!/bin/bash

load_env() {
    local env_file="$CDCTI_HOME/scripts/.env"
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

get_api_url() {
    local env=$1
    local type=$2

    case "$type" in
    "rs")
        case "$env" in
        "local") echo "$RS_LCL_API_URL" ;;
        "staging") echo "$RS_STG_API_URL" ;;
        "production") echo "$RS_PRD_API_URL" ;;
        *)
            echo "Invalid environment: $env" >&2
            exit 1
            ;;
        esac
        ;;
    "ti")
        case "$env" in
        "local") echo "$TI_LCL_API_URL" ;;
        "staging") echo "$TI_STG_API_URL" ;;
        "production") echo "$TI_PRD_API_URL" ;;
        *)
            echo "Invalid environment: $env" >&2
            exit 1
            ;;
        esac
        ;;
    esac
}

extract_host_from_url() {
    local url=$1
    echo "$url" | sed 's|^.*://\([^/:]*\)[:/].*|\1|'
}

parse_sender_string() {
    local sender_string=$1

    if [[ "$sender_string" =~ ^([^.]+)\.([^.]+)$ ]]; then
        SENDER_ORG="${BASH_REMATCH[1]}"
        SENDER_NAME="${BASH_REMATCH[2]}"
        return 0
    else
        echo "Error: Sender string '$sender_string' must be in format 'sender_org.sender_name'" >&2
        return 1
    fi
}

generate_jwt() {
    # requires: jwt-cli
    local sender=$1
    local audience=$2
    local secret_path=$3

    jwt encode \
        --exp='+5min' \
        --jti "$(uuidgen)" \
        --alg RS256 \
        -k "$sender" \
        -i "$sender" \
        -s "$sender" \
        -a "$audience" \
        --no-iat \
        -S "@$secret_path"
}
