#!/usr/bin/env bash

CURRENT_DIR=$(pwd)

# This script resets the database and loads the baseline settings.
# NOTE: Remember to run this script inside the prime-router directory of the prime-reportstream codebase

# Need to CD to the RS codebase local working copy to run the gradlew commands
cd "$RS_HOME" || exit

./gradlew resetDB
./gradlew reloadTable
./gradlew reloadSettings

cd "$CURRENT_DIR" || exit
