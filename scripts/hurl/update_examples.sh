#!/bin/bash

source ./utils.sh

env=local

check_installed_commands hurl jq az
check_apis "$(get_api_url "$env" "rs")" "$(get_api_url "$env" "ti")"

find "$CDCTI_HOME/examples" -type f -name "*$FILE_NAME_SUFFIX_STEP_0.hl7" | while read -r file; do
    echo "-----------------------------------------------------------------------------------------------------------"
    echo "Submitting message: $file"
    submit_message "$env" "$file" "$TI_LOCAL_PRIVATE_KEY_PATH" "$(cat "$RS_LOCAL_JWT_PATH")"
done
