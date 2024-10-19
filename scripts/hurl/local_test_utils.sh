#!/bin/bash

CURRENT_DIR=$(pwd)
RS_HRL_SCRIPT_PATH="$CDCTI_HOME/scripts/hurl/rs"
TI_HRL_SCRIPT_PATH="$CDCTI_HOME/scripts/hurl/ti"
FILE_NAME_SUFFIX_STEP_0="_0_initial_message"
FILE_NAME_SUFFIX_STEP_1="_1_hl7_translation"
FILE_NAME_SUFFIX_STEP_2="_2_fhir_transformation"
FILE_NAME_SUFFIX_STEP_3="_3_hl7_translation_final"

TIMEOUT=180       # 3 minutes
RETRY_INTERVAL=10 # Retry every 10 seconds

AZURITE_CONNECTION_STRING="DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://localhost:10000/devstoreaccount1;" # pragma: allowlist secret

RS_LOCAL_API="http://localhost:7071"
TI_LOCAL_API="http://localhost:8080"

LOCAL_APIS=("$RS_LOCAL_API" "$TI_LOCAL_API")
# Check if the RS and TO local APIs are reachable
for service in "${LOCAL_APIS[@]}"; do
    if ! curl -s --head --fail "$service" | grep "200 OK" >/dev/null; then
        echo "The service at $service is not reachable"
        exit 1
    fi
done

# Check jq is installed
if ! command -v jq &>/dev/null; then
    echo "jq could not be found. Please install jq to proceed."
    exit 1
fi

# Check az (Azure CLI) is installed
if ! command -v az &>/dev/null; then
    echo "Azure CLI (az) could not be found. Please install Azure CLI to proceed."
    exit 1
fi

check_submission_status() {
    local submission_id=$1
    start_time=$(date +%s)

    while true; do
        history_response=$(
            cd "$RS_HRL_SCRIPT_PATH" || exit 1
            ./hrl history.hurl -i "$submission_id"
        )
        overall_status=$(echo "$history_response" | jq -r '.overallStatus')

        echo -n "  Status: $overall_status"
        if [ "$overall_status" == "Delivered" ]; then
            echo "!"
            return 0
        else
            echo ". Retrying in $RETRY_INTERVAL seconds..."
        fi

        # Check if the timeout has been reached
        current_time=$(date +%s)
        elapsed_time=$((current_time - start_time))
        if [ "$elapsed_time" -ge "$TIMEOUT" ]; then
            echo "  Timeout reached after $elapsed_time seconds. Status is still: $overall_status."
            return 1
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

    echo "  Downloading from blob storage '$blob_name' to '$file_path'"
    az storage blob download \
        --container-name "reports" \
        --name $blob_name \
        --file $file_path \
        --connection-string $AZURITE_CONNECTION_STRING \
        --output none

    if [[ "$file_path" == *.fhir ]]; then
        echo "  Formatting the content for: $file_path"
        formatted_content=$(jq '.' "$file_path")
        echo "$formatted_content" >"$file_path"
    fi
}

submit_message() {
    local file=$1
    local message_file_path=$(dirname "$file")
    local message_file_name=$(basename "$file")
    local message_base_name="${message_file_name%.hl7}"
    message_base_name="${message_base_name%$FILE_NAME_SUFFIX_STEP_0}"

    msh9=$(awk -F'|' '/^MSH/ { print $9 }' "$file")
    if [[ "$msh9" == "ORU^R01^ORU_R01" ]]; then
        first_leg_receiver="flexion.etor-service-receiver-results"
        second_leg_receiver="flexion.simulated-hospital"
    elif [[ "$msh9" == "OML^O21^OML_O21" || "$msh9" == "ORM^O01^ORM_O01" ]]; then
        first_leg_receiver="flexion.etor-service-receiver-orders"
        second_leg_receiver="flexion.simulated-lab"
    else
        echo "Unknown receivers for MSH-9 value '$msh9'. Skipping the message"
        return
    fi

    echo "Assuming receivers are '$first_leg_receiver' and '$second_leg_receiver' because of MSH-9 value '$msh9'"

    waters_response=$(
        cd "$RS_HRL_SCRIPT_PATH" || exit 1
        ./hrl waters.hurl -f "$message_file_name" -r "$message_file_path"
    )
    submission_id=$(echo "$waters_response" | jq -r '.id')

    echo "[First leg] Checking submission status for ID: $submission_id"
    if ! check_submission_status "$submission_id"; then
        echo "Failed to deliver the first leg of the message. Skipping the next steps."
        return
    fi

    inbound_submission_id=$(get_sent_report_id_from_history_response "$history_response")
    translated_blob_name="ready/$first_leg_receiver/$inbound_submission_id.fhir"
    translated_file_path="$message_file_path/$message_base_name$FILE_NAME_SUFFIX_STEP_1.fhir"
    download_from_azurite "$translated_blob_name" "$translated_file_path"

    metadata_response=$(
        cd "$TI_HRL_SCRIPT_PATH" || exit 1
        ./hrl metadata.hurl -i "$inbound_submission_id"
    )
    outbound_submission_id=$(echo "$metadata_response" | jq -r '.issue[] | select(.details.text == "outbound submission id") | .diagnostics')

    transformed_blob_name="receive/flexion.etor-service-sender/$outbound_submission_id.fhir"
    transformed_file_path="$message_file_path/$message_base_name$FILE_NAME_SUFFIX_STEP_2.fhir"
    download_from_azurite "$transformed_blob_name" "$transformed_file_path"

    echo "[Second leg] Checking submission status for ID: $outbound_submission_id"
    if ! check_submission_status "$outbound_submission_id"; then
        echo "Failed to deliver the second leg of the message. Skipping the next steps."
        return
    fi

    final_submission_id=$(get_sent_report_id_from_history_response "$history_response")
    final_blob_name="ready/$second_leg_receiver/$final_submission_id.hl7"
    final_file_path="$message_file_path/$message_base_name$FILE_NAME_SUFFIX_STEP_3.hl7"
    download_from_azurite "$final_blob_name" "$final_file_path"
}
