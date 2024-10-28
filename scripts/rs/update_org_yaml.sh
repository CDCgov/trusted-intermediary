#!/bin/bash

ORG_SETTINGS_DIR="settings/STLTs"

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
