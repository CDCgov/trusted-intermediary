#!/usr/bin/env bash

# This script loads the credentials into the local vault to set up the ETOR receivers.
# NOTE: Remember to run this script inside the prime-router directory of the prime-reportstream codebase
# Make sure to add a CDCTI_HOME environment variable pointing to the trusted-intermediary directory
# export CDCTI_HOME="/path/to/trusted-intermediary"

source "$RS_HOME/prime-router/.vault/env/.env.local"

./prime create-credential --type UserPass --user foo --pass pass --persist DEFAULT-SFTP

rs_private_key=$(cat "$RS_LOCAL_PRIVATE_KEY_PATH")
./prime create-credential --type UserApiKey --apikey-user flexion --apikey "$rs_private_key" --persist FLEXION--ETOR-SERVICE-RECEIVER-ORDERS
./prime create-credential --type UserApiKey --apikey-user flexion --apikey "$rs_private_key" --persist FLEXION--ETOR-SERVICE-RECEIVER-RESULTS
./prime create-credential --type UserApiKey --apikey-user ucsd --apikey "$rs_private_key" --persist UCSD--ETOR-NBS-RESULTS
./prime create-credential --type UserApiKey --apikey-user la-phl --apikey "$rs_private_key" --persist LA-PHL--ETOR-NBS-ORDERS
./prime create-credential --type UserApiKey --apikey-user la-ochsner --apikey "$rs_private_key" --persist LA-OCHSNER--ETOR-NBS-RESULTS
