#!/bin/bash

# Ensure jq is installed
if ! command -v jq &>/dev/null; then
    echo "jq could not be found. Please install jq to proceed."
    exit
fi

# Maximum time to wait for the overallStatus to be "Delivered" (in seconds)
TIMEOUT=300       # 5 minutes
RETRY_INTERVAL=10 # Retry every 10 seconds

find "$CDCTI_HOME/examples/MN" -type f -name '*0_initial_message.hl7' | while read -r file; do
    echo "Submitting file: $file"

    temp_file=$(mktemp)
    temp_dir_path=$(dirname "$temp_file")
    temp_file_name=$(basename "$temp_file")

    # Read the file content, update MSH-6, and write the result to a temporary file
    awk -F'|' '/^MSH/ { $6 = "^simulated-hospital-id^" } 1' OFS='|' "$file" >"$temp_file"
    echo "$temp_file"

    # Submit the updated file, capture the JSON response and extract the submission ID
    waters_response=$(./hrl waters.hurl -f "$temp_file_name" -r "$temp_dir_path")
    submission_id=$(echo "$waters_response" | jq -r '.id')

    if [ -n "$submission_id" ]; then
        echo "Submission ID: $submission_id"
        history_response=$(./hrl history.hurl -i $submission_id)
        status=$(echo "$history_response" | jq '.overallStatus')
    else
        echo "Failed to extract submission ID."
    fi

    start_time=$(date +%s)
    while true; do
        history_response=$(./hrl history.hurl -i "$submission_id")
        overall_status=$(echo "$history_response" | jq -r '.overallStatus')

        if [ "$overall_status" == "Delivered" ]; then
            echo "Submission $submission_id is Delivered."
            break
        else
            echo "Status: $overall_status. Retrying in $RETRY_INTERVAL seconds..."
        fi

        # Check if the timeout has been reached
        current_time=$(date +%s)
        elapsed_time=$((current_time - start_time))
        if [ "$elapsed_time" -ge "$TIMEOUT" ]; then
            echo "Timeout reached after $elapsed_time seconds. Status is still: $overall_status."
            break
        fi

        sleep $RETRY_INTERVAL
    done

    rm "$temp_file"

    echo "Waiting 5 seconds before the next submission..."
    sleep 5
done
