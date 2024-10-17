#!/bin/bash

# Ensure jq is installed
if ! command -v jq &>/dev/null; then
    echo "jq could not be found. Please install jq to proceed."
    exit
fi

# Check if az (Azure CLI) is installed
if ! command -v az &>/dev/null; then
    echo "Azure CLI (az) could not be found. Please install Azure CLI to proceed."
    exit
fi

TIMEOUT=300
RETRY_INTERVAL=10
CURRENT_DIR=$(pwd)
ROOT="$CDCTI_HOME/examples/Temp"
AZURITE_CONNECTION_STRING="DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://localhost:10000/devstoreaccount1;" # pragma: allowlist secret

check_submission_status() {
    local submission_id=$1
    start_time=$(date +%s)

    while true; do
        history_response=$(./hrl history.hurl -i "$submission_id")
        overall_status=$(echo "$history_response" | jq -r '.overallStatus')

        echo -n "  Status: $overall_status"
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
            echo "  Timeout reached after $elapsed_time seconds. Status is still: $overall_status."
            return 1 # Timeout
        fi

        sleep $RETRY_INTERVAL
    done
}

get_sent_report_id_from_history_response() {
    local history_response=$1
    echo $history_response | jq '.destinations[0].sentReports[0].externalName' | sed 's/.*-\([0-9a-f]\{8\}-[0-9a-f]\{4\}-[0-9a-f]\{4\}-[0-9a-f]\{4\}-[0-9a-f]\{12\}\)-.*/\1/'
}

download_from_azurite() {
    local blob_name=$1
    local file_path=$2

    echo "  Downloading from blob storage '$translated_blob_name' to '$translated_file_path'"
    az storage blob download \
        --container-name "reports" \
        --name $blob_name \
        --file $file_path \
        --connection-string $AZURITE_CONNECTION_STRING \
        --output none
}

find $ROOT -type f -name '*0_initial_message.hl7' | while read -r file; do
    echo "Submitting file: $file"

    temp_file=$(mktemp)
    temp_dir_path=$(dirname "$temp_file")
    temp_file_name=$(basename "$temp_file")

    # Read the file content, update MSH-6, and write the result to a temporary file
    awk -F'|' '/^MSH/ { $6 = "^simulated-hospital-id^" } 1' OFS='|' "$file" >"$temp_file"

    # Submit the updated file, capture the JSON response and extract the submission ID
    waters_response=$(./hrl waters.hurl -f "$temp_file_name" -r "$temp_dir_path")
    submission_id=$(echo "$waters_response" | jq -r '.id')

    echo "[First leg] Checking submission status for ID: $submission_id"
    if ! check_submission_status "$submission_id"; then
        echo "Failed to deliver the first leg of the message. Skipping the next steps."
        rm "$temp_file"
        continue
    fi

    inbound_submission_id=$(get_sent_report_id_from_history_response "$history_response")
    translated_blob_name="ready/flexion.etor-service-receiver-results/$inbound_submission_id.fhir"
    translated_file_path="$ROOT/translated.fhir"
    download_from_azurite "$translated_blob_name" "$translated_file_path"

    cd ../ti || exit 1
    metadata_response=$(./hrl metadata.hurl -i "$inbound_submission_id")
    outbound_submission_id=$(echo "$metadata_response" | jq -r '.issue[] | select(.details.text == "outbound submission id") | .diagnostics')
    cd "$CURRENT_DIR"

    transformed_blob_name="receive/flexion.etor-service-sender/$outbound_submission_id.fhir"
    transformed_file_path="$ROOT/transformed.fhir"
    download_from_azurite "$transformed_blob_name" "$transformed_file_path"

    echo "[Second leg] Checking submission status for ID: $outbound_submission_id"
    if ! check_submission_status "$outbound_submission_id"; then
        echo "Failed to deliver the second leg of the message. Skipping the next steps."
        rm "$temp_file"
        continue
    fi

    final_submission_id=$(get_sent_report_id_from_history_response "$history_response")
    final_blob_name="ready/flexion.simulated-hospital/$final_submission_id.hl7"
    final_file_path="$ROOT/final.hl7"
    download_from_azurite "$final_blob_name" "$final_file_path"

    rm "$temp_file"

    echo "Waiting 5 seconds before the next submission..."
    sleep 5
done
