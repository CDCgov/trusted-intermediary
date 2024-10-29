#!/usr/bin/env bash

# This script loads the ETOR organization settings locally and adds the public keys for the senders.
# NOTE: Remember to run this script inside the prime-router directory of the prime-reportstream codebase
# Make sure to add a CDCTI_HOME environment variable pointing to the trusted-intermediary directory
# export CDCTI_HOME="/path/to/trusted-intermediary"
# You'll also need to set the path to the keys if running in a non-local environment

# Load organization settings
./prime multiple-settings set -s -i ./settings/STLTs/Flexion/flexion.yml
./prime multiple-settings set -s -i ./settings/STLTs/CA/ucsd.yml
./prime multiple-settings set -s -i ./settings/STLTs/LA/la-phl.yml
./prime multiple-settings set -s -i ./settings/STLTs/LA/la-ochsner.yml

# Add public keys for senders
./prime organization addkey --public-key "$TI_LOCAL_PUBLIC_KEY_PATH" --scope "flexion.*.report" --orgName flexion --kid flexion.etor-service-sender --doit
./prime organization addkey --public-key "$TI_LOCAL_PUBLIC_KEY_PATH" --scope "flexion.*.report" --orgName flexion --kid flexion.simulated-hospital --doit
./prime organization addkey --public-key "$TI_LOCAL_PUBLIC_KEY_PATH" --scope "flexion.*.report" --orgName flexion --kid flexion.simulated-lab --doit
./prime organization addkey --public-key "$TI_LOCAL_PUBLIC_KEY_PATH" --scope "flexion.*.report" --orgName flexion --kid flexion.simulated-sender --doit
