#!/bin/bash

[ -z "${CDCTI_HOME}" ] && echo "Error: Environment variable CDCTI_HOME is not set" && exit 1
source "$CDCTI_HOME/scripts/utils.sh"

# This script updates the organization settings YAML files in RS to:
# - use local REST transport settings for Flexion's etor-service-receiver receivers
# - use local SFTP transport settings for Flexion's simulated-hospital and simulated-lab receivers
# - use local SFTP transport settings for partner organizations

# Requirements:
# - yq (https://github.com/mikefarah/yq)
# - This script should run inside the prime-router directory of the prime-reportstream codebase

ORG_SETTINGS_DIR="settings/STLTs"

check_installed_commands yq

echo "Updating transport in Flexion org settings file..."
yq eval '.[0].receivers[] |= (
    select(.name == "simulated-hospital" or .name == "simulated-lab").transport = {
        "type": "SFTP",
        "host": "sftp",
        "port": 22,
        "filePath": "./upload",
        "credentialName": "DEFAULT-SFTP"
    } |
    select(.name == "etor-service-receiver-orders") |= (
        .transport.authTokenUrl = "http://host.docker.internal:8080/v1/auth/token" |
        .transport.reportUrl = "http://host.docker.internal:8080/v1/etor/orders"
    ) |
    select(.name == "etor-service-receiver-results") |= (
        .transport.authTokenUrl = "http://host.docker.internal:8080/v1/auth/token" |
        .transport.reportUrl = "http://host.docker.internal:8080/v1/etor/results"
    )
)' -i "$ORG_SETTINGS_DIR/Flexion/flexion.yml"

echo "Updating transport in partner org settings files..."
for file in "$ORG_SETTINGS_DIR/CA/ucsd.yml" "$ORG_SETTINGS_DIR/LA/la-ochsner.yml" "$ORG_SETTINGS_DIR/LA/la-phl.yml"; do
    yq eval '.[0].receivers[] |= select(.name == "etor-nbs-results" or .name == "etor-nbs-orders").transport = {
        "type": "SFTP",
        "host": "sftp",
        "port": 22,
        "filePath": "./upload",
        "credentialName": "DEFAULT-SFTP"
    }' -i "$file"
done

echo "Updates completed."
