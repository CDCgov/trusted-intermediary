#!/usr/bin/env bash
# NOTE: Remember to run this script inside the prime-router directory of the prime-reportstream codebase
# Make sure to add a CDCTI_HOME environment variable pointing to the trusted-intermediary directory
# export CDCTI_HOME="/path/to/trusted-intermediary"

private_key=$(cat $CDCTI_HOME/mock_credentials/organization-report-stream-private-key-local.pem)

export $(xargs <.vault/env/.env.local)

./prime create-credential --type UserPass --user foo --pass pass --persist DEFAULT-SFTP
./prime create-credential --type UserApiKey --apikey-user flexion --apikey "$private_key" --persist FLEXION--ETOR-SERVICE-RECEIVER-ORDERS
./prime create-credential --type UserApiKey --apikey-user flexion --apikey "$private_key" --persist FLEXION--ETOR-SERVICE-RECEIVER-RESULTS
