#!/usr/bin/env bash

# This script loads the ETOR organization settings and adds the public keys for the senders.
# NOTE: Remember to run this script inside the prime-router directory of the prime-reportstream codebase
# Make sure to add a CDCTI_HOME environment variable pointing to the trusted-intermediary directory
# export CDCTI_HOME="/path/to/trusted-intermediary"
# You'll also need to set the path to the keys if running in a non-local environment

env=${1:-"local"}

if [ "$env" = "local" ]; then
    echo "Using local public keys"
    flexion_key="$CDCTI_HOME/mock_credentials/organization-trusted-intermediary-public-key-local.pem"
    # al_phl_key="/path/to/organization-al-phl-public-key-local.pem"
    # oracle_rln_key="/path/to/organization-oracle-rln-public-key-local.pem"
    # la_phl_key="/path/to/organization-la-phl-public-key-local.pem"
    # la_ochsner_key="/path/to/organization-la-ochsner-public-key-local.pem"
elif [ "$env" = "staging" ]; then
    echo "Using staging public keys"
    # flexion_key="/path/to/organization-trusted-intermediary-public-key-staging.pem"
    # al_phl_key="/path/to/organization-al-phl-public-key-staging.pem"
    # oracle_rln_key="/path/to/organization-oracle-rln-public-key-staging.pem"
    # la_phl_key="/path/to/organization-la-phl-public-key-staging.pem"
    # la_ochsner_key="/path/to/organization-la-ochsner-public-key-staging.pem"
elif [ "$env" = "prod" ]; then
    echo "Using prod public keys"
    # flexion_key="/path/to/organization-trusted-intermediary-public-key-prod.pem"
    # al_phl_key="/path/to/organization-al-phl-public-key-prod.pem"
    # oracle_rln_key="/path/to/organization-oracle-rln-public-key-prod.pem"
    # la_phl_key="/path/to/organization-la-phl-public-key-prod.pem"
    # la_ochsner_key="/path/to/organization-la-ochsner-public-key-prod.pem"
else
    echo "Unknown environment: $env"
    exit 1
fi

## Flexion
./prime multiple-settings set -s -e $env -i ./settings/STLTs/Flexion/flexion.yml
./prime organization addkey -e $env --public-key $flexion_key --scope "flexion.*.report" --orgName flexion --kid flexion.etor-service-sender --doit
./prime organization addkey -e $env --public-key $flexion_key --scope "flexion.*.report" --orgName flexion --kid flexion.simulated-hospital --doit
./prime organization addkey -e $env --public-key $flexion_key --scope "flexion.*.report" --orgName flexion --kid flexion.simulated-lab --doit
./prime organization addkey -e $env --public-key $flexion_key --scope "flexion.*.report" --orgName flexion --kid flexion.simulated-sender --doit

## CA
./prime multiple-settings set -s -e $env -i ./settings/STLTs/CA/ucsd.yml

## LA
./prime multiple-settings set -s -e $env -i ./settings/STLTs/LA/la-phl.yml
# ./prime organization addkey -e $env --public-key $la_phl_key --scope "la-phl.*.report" --orgName la-phl --kid la-phl.etor-nbs-results --doit
./prime multiple-settings set -s -e $env -i ./settings/STLTs/LA/la-ochsner.yml
# ./prime organization addkey -e $env --public-key $la_ochsner_key --scope "la-ochsner.*.report" --orgName la-ochsner --kid la-ochsner.etor-nbs-orders --doit
