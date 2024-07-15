#!/usr/bin/env bash
# NOTE: Remember to run this script inside the prime-router directory of the prime-reportstream codebase
# Make sure to add a CDCTI_HOME environment variable pointing to the trusted-intermediary directory
# export CDCTI_HOME="/path/to/trusted-intermediary"
# You'll also need to set the path to the secret if running in a non-local environment

env=${1:-"local"}

if [ "$env" = "local" ]; then
    secret="$CDCTI_HOME/mock_credentials/organization-trusted-intermediary-private-key-local.pem"
else
    secret="/path/to/ti-staging-private-key.pem"
fi

## Flexion
./prime multiple-settings set -s -e $env -i ./settings/STLTs/Flexion/flexion.yml
./prime organization addkey -e $env --public-key $secret --scope "flexion.*.report" --orgName flexion --kid flexion.etor-service-sender --doit
./prime organization addkey -e $env --public-key $secret --scope "flexion.*.report" --orgName flexion --kid flexion.simulated-hospital --doit
./prime organization addkey -e $env --public-key $secret --scope "flexion.*.report" --orgName flexion --kid flexion.simulated-lab --doit

## AL
./prime multiple-settings set -s -e $env -i ./settings/STLTs/AL/al-phl.yml
# ./prime organization addkey -e $env --public-key /path/to/public/key.pem --scope "al-phl.*.report" --orgName al-phl --kid al-phl.etor-nbs-results --doit
./prime multiple-settings set -e $env -s -i ./settings/STLTs/Oracle/oracle-rln.yml
# ./prime organization addkey -e $env --public-key /path/to/public/key.pem --scope "oracle-rln.*.report" --orgName oracle-rln --kid oracle-rln.etor-nbs-orders --doit

## CA
./prime multiple-settings set -s -e $env -i ./settings/STLTs/CA/ucsd.yml
./prime multiple-settings set -s -e $env -i ./settings/STLTs/CA/ca-phl.yml
# ./prime organization addkey -e $env --public-key /path/to/public/key.pem --scope "ca-phl.*.report" --orgName ca-phl --kid ca-phl.etor-nbs-results --doit

## LA
./prime multiple-settings set -s -e $env -i ./settings/STLTs/LA/la-phl.yml
# ./prime organization addkey -e $env --public-key /path/to/public/key.pem --scope "la-phl.*.report" --orgName la-phl --kid la-phl.etor-nbs-results --doit
./prime multiple-settings set -s -e $env -i ./settings/STLTs/LA/la-ochsner.yml
# ./prime organization addkey -e $env --public-key /path/to/public/key.pem --scope "la-ochsner.*.report" --orgName la-ochsner --kid la-ochsner.etor-nbs-orders --doit
