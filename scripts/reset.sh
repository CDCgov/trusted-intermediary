#!/usr/bin/env bash
#Run in the prime-reportstream repo inside the prime-router folder.

./gradlew resetDB

./gradlew reloadTable

./gradlew flywayRepair

./gradlew reloadSettings

./prime multiple-settings set -s -i ./settings/staging/0166-flexion-staging-results-handling.yml

./prime organization addkey --public-key ~/trusted-intermediary/mock_credentials/organization-trusted-intermediary-public-key-local.pem --scope "flexion.*.report" --orgName flexion --kid flexion.etor-service-sender --doit

./prime organization addkey --public-key /trusted-intermediary/mock_credentials/organization-trusted-intermediary-public-key-local.pem --scope "flexion.*.report" --orgName flexion --kid flexion.simulated-hospital --doit

./prime organization addkey --public-key /trusted-intermediary/mock_credentials/organization-trusted-intermediary-public-key-local.pem --scope "flexion.*.report" --orgName flexion --kid flexion.simulated-lab --doit
