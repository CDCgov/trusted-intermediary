#!/usr/bin/env bash

# This script resets the database and loads the baseline settings.
# NOTE: Remember to run this script inside the prime-router directory of the prime-reportstream codebase

# Reset database
../gradlew resetDB

# Reload tables
./prime lookuptables loadall

# Load baseline organization settings
./prime multiple-settings set -s -i ./settings/organizations.yml
