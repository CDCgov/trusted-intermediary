#!/bin/bash

source ./local_test_utils.sh

check_prerequisites

find "$CDCTI_HOME/examples" -type f -name "*$FILE_NAME_SUFFIX_STEP_0.hl7" | while read -r file; do
    echo "-----------------------------------------------------------------------------------------------------------"
    echo "Submitting message: $file"
    submit_message "$file"
done
