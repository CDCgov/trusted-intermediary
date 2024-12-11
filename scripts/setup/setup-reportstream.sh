#!/usr/bin/env bash

[ -z "${CDCTI_HOME}" ] && echo "Error: Environment variable CDCTI_HOME is not set. Please refer to /scripts/README.md for instructions" && exit 1
source "$CDCTI_HOME/scripts/lib/common.sh"

# Check requirements for this script
check_env_vars RS_HOME
check_installed_commands yq

# Need to CD to the RS codebase local working copy to run the gradlew commands
CURRENT_DIR=$(pwd)
cd "$RS_HOME" || exit

echo "Resetting the database and loading the baseline settings..."
./gradlew resetDB
./gradlew reloadTables
./gradlew reloadSettings

# Update RS Configuration for the TI API based on docker or gradle
LOCAL_DOCKER_IMAGE_NAME=$(docker ps --filter "name=trusted-intermediary-router-1" | grep trusted-intermediary-router-1)
if [[ ! -z $LOCAL_DOCKER_IMAGE_NAME ]]; then
  ti_api_url=${TI_LCL_API_URL}
else
  ti_api_url=${TI_DOCKER_LCL_API_URL_RS_CONFIG}
fi

# Need to CD to prime-router to run the prime CLI
cd "prime-router" || exit

echo "Updating transport settings in Flexion org file..."
yq eval '.[0].receivers[] |= (
    select(.name == "etor-service-receiver-orders") |= (
        .transport.authTokenUrl = "__TI_API_URL__/v1/auth/token" |
        .transport.reportUrl = "__TI_API_URL__/v1/etor/orders" |
        .transport.authHeaders.Host = "__TI_API_HOST__"
    ) |
    select(.name == "etor-service-receiver-results") |= (
        .transport.authTokenUrl = "__TI_API_URL__/v1/auth/token" |
        .transport.reportUrl = "__TI_API_URL__/v1/etor/results" |
        .transport.authHeaders.Host = "__TI_API_HOST__"
    ) |
    select(.name == "simulated-hospital" or .name == "simulated-lab").transport = {
        "type": "SFTP",
        "host": "sftp",
        "port": 22,
        "filePath": "./upload",
        "credentialName": "DEFAULT-SFTP"
    }
)' -i "settings/STLTs/Flexion/flexion.yml"

echo "Updating local URL and host in transport settings..."
sed -i '' "s|__TI_API_URL__|${ti_api_url}|g" "settings/STLTs/Flexion/flexion.yml"
sed -i '' "s|__TI_API_HOST__|$(extract_host_from_url "${TI_DOCKER_LCL_API_URL_RS_CONFIG}")|g" "settings/STLTs/Flexion/flexion.yml"

echo "Updating transport settings in partner org files..."
for file in "settings/STLTs/CA/ucsd.yml" "settings/STLTs/LA/la-ochsner.yml" "settings/STLTs/LA/la-phl.yml"; do
    yq eval '.[0].receivers[] |= select(.name == "etor-nbs-results" or .name == "etor-nbs-orders").transport = {
        "type": "SFTP",
        "host": "sftp",
        "port": 22,
        "filePath": "./upload",
        "credentialName": "DEFAULT-SFTP"
    }' -i "$file"
done

echo "Setting up the organization settings..."
./prime multiple-settings set -s -i ./settings/STLTs/Flexion/flexion.yml
./prime multiple-settings set -s -i ./settings/STLTs/CA/ucsd.yml
./prime multiple-settings set -s -i ./settings/STLTs/LA/la-phl.yml
./prime multiple-settings set -s -i ./settings/STLTs/LA/la-ochsner.yml

echo "Adding public keys for senders..."
./prime organization addkey --public-key "$TI_LOCAL_PUBLIC_KEY_PATH" --scope "flexion.*.report" --orgName flexion --kid flexion.etor-service-sender --doit
./prime organization addkey --public-key "$TI_LOCAL_PUBLIC_KEY_PATH" --scope "flexion.*.report" --orgName flexion --kid flexion.simulated-hospital --doit
./prime organization addkey --public-key "$TI_LOCAL_PUBLIC_KEY_PATH" --scope "flexion.*.report" --orgName flexion --kid flexion.simulated-lab --doit
./prime organization addkey --public-key "$TI_LOCAL_PUBLIC_KEY_PATH" --scope "flexion.*.report" --orgName flexion --kid flexion.simulated-sender --doit

echo "Adding credentials for receivers..."
export $(xargs <"$RS_HOME/prime-router/.vault/env/.env.local")
rs_private_key=$(cat "$RS_LOCAL_PRIVATE_KEY_PATH")

./prime create-credential --type UserPass --user foo --pass pass --persist DEFAULT-SFTP
./prime create-credential --type UserApiKey --apikey-user flexion --apikey "$rs_private_key" --persist FLEXION--ETOR-SERVICE-RECEIVER-ORDERS
./prime create-credential --type UserApiKey --apikey-user flexion --apikey "$(cat "$RS_LOCAL_PRIVATE_KEY_PATH")" --persist FLEXION--ETOR-SERVICE-RECEIVER-RESULTS
./prime create-credential --type UserApiKey --apikey-user ucsd --apikey "$rs_private_key" --persist UCSD--ETOR-NBS-RESULTS
./prime create-credential --type UserApiKey --apikey-user la-phl --apikey "$rs_private_key" --persist LA-PHL--ETOR-NBS-ORDERS
./prime create-credential --type UserApiKey --apikey-user la-ochsner --apikey "$rs_private_key" --persist LA-OCHSNER--ETOR-NBS-RESULTS

cd "$CURRENT_DIR" || exit
