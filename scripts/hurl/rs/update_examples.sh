#!/bin/bash

find "$CDCTI_HOME/examples" -type f -name '*0_initial_message.hl7' | while read -r file; do
    echo "Submitting file: $file"

    temp_file=$(mktemp)
    awk -F'|' '/^MSH/ { $6 = "^simulated-lab-id^" } 1' OFS='|' "$file" >"$temp_file"
    echo "$temp_file"

    temp_dir_path=$(dirname "$temp_file")
    temp_file_name=$(basename "$temp_file")
    ./hrl waters.hurl -f $temp_file_name -r $temp_dir_path

    rm "$temp_file"

    echo "Waiting 5 seconds before the next submission..."
    sleep 5
done
