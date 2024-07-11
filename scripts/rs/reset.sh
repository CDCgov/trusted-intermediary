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

## AL
./prime multiple-settings set -s -i ./settings/STLTs/AL/al-phl.yml
# ./prime organization addkey --public-key /path/to/public/key.pem --scope "al-phl.*.report" --orgName al-phl --kid al-phl.etor-nbs-results --doit
./prime multiple-settings set -s -i ./settings/STLTs/Oracle/oracle-rln.yml
# ./prime organization addkey --public-key /path/to/public/key.pem --scope "oracle-rln.*.report" --orgName oracle-rln --kid oracle-rln.etor-nbs-orders --doit

## CA
./prime multiple-settings set -s -i ./settings/STLTs/CA/ucsd.yml
./prime multiple-settings set -s -i ./settings/STLTs/CA/ca-phl.yml
# ./prime organization addkey --public-key /path/to/public/key.pem --scope "ca-phl.*.report" --orgName ca-phl --kid ca-phl.etor-nbs-results --doit

## LA
./prime multiple-settings set -s -i ./settings/STLTs/LA/la-phl.yml
# ./prime organization addkey --public-key /path/to/public/key.pem --scope "la-phl.*.report" --orgName la-phl --kid la-phl.etor-nbs-results --doit
./prime multiple-settings set -s -i ./settings/STLTs/LA/la-ochsner.yml
# ./prime organization addkey --public-key /path/to/public/key.pem --scope "la-ochsner.*.report" --orgName la-ochsner --kid la-ochsner.etor-nbs-orders --doit
