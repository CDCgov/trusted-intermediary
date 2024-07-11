#!/usr/bin/env bash
# NOTE: Remember to run this script inside the prime-router directory of the prime-reportstream codebase
# Make sure to add a CDCTI_HOME environment variable pointing to the trusted-intermediary directory
# export CDCTI_HOME="/path/to/trusted-intermediary"

# Reset database
../gradlew resetDB

# Reload tables
./prime lookuptables loadall

# Load organization settings
./prime multiple-settings set -s -i ./settings/organizations.yml

## Flexion
./prime multiple-settings set -s -i ./settings/STLTs/Flexion/flexion.yml
./prime organization addkey --public-key $CDCTI_HOME/mock_credentials/organization-trusted-intermediary-public-key-local.pem --scope "flexion.*.report" --orgName flexion --kid flexion.etor-service-sender --doit
./prime organization addkey --public-key $CDCTI_HOME/mock_credentials/organization-trusted-intermediary-public-key-local.pem --scope "flexion.*.report" --orgName flexion --kid flexion.simulated-hospital --doit
./prime organization addkey --public-key $CDCTI_HOME/mock_credentials/organization-trusted-intermediary-public-key-local.pem --scope "flexion.*.report" --orgName flexion --kid flexion.simulated-lab --doit

## CA
./prime multiple-settings set -s -i ./settings/STLTs/CA/ucsd.yml

## LA
./prime multiple-settings set -s -i ./settings/STLTs/LA/la-phl.yml
./prime multiple-settings set -s -i ./settings/STLTs/LA/la-ochsner.yml

## CA
./prime multiple-settings set -s -i ./settings/STLTs/AL/al-phl.yml
./prime multiple-settings set -s -i ./settings/STLTs/Oracle/oracle-rln.yml
