#!/bin/bash

[ -z "${CDCTI_HOME}" ] && echo "Error: Environment variable CDCTI_HOME is not set. Please refer to /scripts/README.md for instructions" && exit 1
source "$CDCTI_HOME/scripts/lib/common.sh"
source "$CDCTI_HOME/scripts/lib/api.sh"

# default values
ENVIRONMENT=staging
ROOT_PATH=$CDCTI_HOME/examples/CA/
CONTENT_TYPE=application/hl7-v2
URL=https://epicproxy-np.et0502.epichosted.com/FhirProxy/oauth2/token

parse_args "ucsd" "$@" || {
    show_usage "$(basename "$0")"
    exit 0
}

handle_api_request "ucsd" "$@"
