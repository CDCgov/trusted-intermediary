#!/bin/bash

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
)' -i settings/STLTs/Flexion/flexion.yml

echo "Updating transport in partner org settings files..."
for file in settings/STLTs/CA/ucsd.yml settings/STLTs/LA/la-ochsner.yml settings/STLTs/LA/la-phl.yml; do
    yq eval '.[0].receivers[] |= select(.name == "etor-nbs-results" or .name == "etor-nbs-orders").transport = {
        "type": "SFTP",
        "host": "sftp",
        "port": 22,
        "filePath": "./upload",
        "credentialName": "DEFAULT-SFTP"
    }' -i "$file"
done

echo "Updates completed."
