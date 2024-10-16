#!/bin/bash

# Ensure jq is installed
if ! command -v jq &>/dev/null; then
    echo "jq could not be found. Please install jq to proceed."
    exit
fi

# Maximum time to wait for the overallStatus to be "Delivered" (in seconds)
TIMEOUT=300       # 5 minutes
RETRY_INTERVAL=10 # Retry every 10 seconds
CURRENT_DIR=$(pwd)

check_submission_status() {
    local submission_id=$1
    start_time=$(date +%s)

    while true; do
        history_response=$(./hrl history.hurl -i "$submission_id")
        overall_status=$(echo "$history_response" | jq -r '.overallStatus')

        echo -n "Status: $overall_status"
        if [ "$overall_status" == "Delivered" ]; then
            echo "!"
            return 0 # Success
        else
            echo ". Retrying in $RETRY_INTERVAL seconds..."
        fi

        # Check if the timeout has been reached
        current_time=$(date +%s)
        elapsed_time=$((current_time - start_time))
        if [ "$elapsed_time" -ge "$TIMEOUT" ]; then
            echo "Timeout reached after $elapsed_time seconds. Status is still: $overall_status."
            return 1 # Timeout
        fi

        sleep $RETRY_INTERVAL
    done
}

find "$CDCTI_HOME/examples/MN" -type f -name '*0_initial_message.hl7' | while read -r file; do
    echo "Submitting file: $file"

    temp_file=$(mktemp)
    temp_dir_path=$(dirname "$temp_file")
    temp_file_name=$(basename "$temp_file")

    # Read the file content, update MSH-6, and write the result to a temporary file
    awk -F'|' '/^MSH/ { $6 = "^simulated-hospital-id^" } 1' OFS='|' "$file" >"$temp_file"
    # echo "$temp_file"

    # Submit the updated file, capture the JSON response and extract the submission ID
    waters_response=$(./hrl waters.hurl -f "$temp_file_name" -r "$temp_dir_path")
    submission_id=$(echo "$waters_response" | jq -r '.id')

    echo "[First leg] Checking submission status for ID: $submission_id"
    if ! check_submission_status "$submission_id"; then
        echo "Failed to deliver the first leg of the message. Skipping the next steps."
        rm "$temp_file"
        continue
    fi

    cd ../ti || exit 1
    inbound_submission_id=$(echo $history_response | jq '.destinations[0].sentReports[0].externalName' | sed 's/^[^-]*-\(.*\)-[^-]*$/\1/')
    metadata_response=$(./hrl metadata.hurl -i "$inbound_submission_id")
    outbound_submission_id=$(echo "$metadata_response" | jq -r '.issue[] | select(.details.text == "outbound submission id") | .diagnostics')
    cd "$CURRENT_DIR"

    echo "[Second leg] Checking submission status for ID: $outbound_submission_id"
    if ! check_submission_status "$outbound_submission_id"; then
        echo "Failed to deliver the second leg of the message. Skipping the next steps."
        rm "$temp_file"
        continue
    fi

    final_file_name=$(echo $history_response | jq '.destinations[0].sentReports[0].externalName')
    echo "Final file name: $final_file_name"

    rm "$temp_file"

    echo "Waiting 5 seconds before the next submission..."
    sleep 5
done
