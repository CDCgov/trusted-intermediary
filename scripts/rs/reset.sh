#!/usr/bin/env bash
# NOTE: Remember to run this script inside the prime-router directory of the prime-reportstream codebase

# Reset database
../gradlew resetDB

# Reload tables
./prime lookuptables loadall

# Load baseline organization settings
./prime multiple-settings set -s -i ./settings/organizations.yml

# Load ETOR organization settings
./load-etor-org-settings.sh
