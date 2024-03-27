#!/usr/bin/env bash
# NOTE: Remember to run this script inside the prime-router directory of the prime-reportstream codebase
# Make sure to update the path_to_cdcti variable to the trusted-intermediary directory

path_to_cdcti="/path/to/trusted-intermediary"
private_key=$(cat $path_to_cdcti/mock_credentials/organization-report-stream-private-key-local.pem)

export $(xargs <.vault/env/.env.local)

./prime create-credential --type UserPass --user foo --pass pass --persist DEFAULT-SFTP
./prime create-credential --type UserApiKey --apikey-user flexion --apikey "$private_key" --persist FLEXION--ETOR-SERVICE-RECEIVER-ORDERS
./prime create-credential --type UserApiKey --apikey-user flexion --apikey "$private_key" --persist FLEXION--ETOR-SERVICE-RECEIVER-RESULTS
