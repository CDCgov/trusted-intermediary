#!/bin/bash

source ./utils.sh

env=local
rs_url=$(get_endpoint_url "rs" "$env")
ti_url=$(get_endpoint_url "ti" "$env")

check_installed_commands hurl jq az
check_apis "$rs_url" "$ti_url"

find "$CDCTI_HOME/examples" -type f -name "*$FILE_NAME_SUFFIX_STEP_0.hl7" | while read -r file; do
    echo "-----------------------------------------------------------------------------------------------------------"
    echo "Submitting message: $file"
    submit_message "$file"
done
