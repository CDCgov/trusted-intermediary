#!/bin/bash

extract_rs_history_submission_id() {
    # requires: jq
    local history_response=$1
    local externalName
    externalName=$(echo "$history_response" | jq -r '.destinations[0].sentReports[0].externalName') || return 1
    [[ -z "$externalName" || "$externalName" == "null" ]] && return 1
    echo "$externalName" | sed 's/.*-\([0-9a-f]\{8\}-[0-9a-f]\{4\}-[0-9a-f]\{4\}-[0-9a-f]\{4\}-[0-9a-f]\{12\}\)-.*/\1/'
}

extract_ti_metadata_submission_id() {
    # requires: jq
    local metadata_response=$1
    echo "$metadata_response" | jq -r '.issue[] | select(.details.text == "outbound submission id") | .diagnostics'
}

download_from_azurite() {
    # requires: jq, az
    local blob_name=$1
    local file_path=$2

    echo "  Downloading from blob storage '$blob_name' to '$file_path'"
    az storage blob download \
        --container-name "reports" \
        --name "$blob_name" \
        --file "$file_path" \
        --connection-string "$AZURITE_CONNECTION_STRING" \
        --output none || {
        echo "Download failed for blob '$blob_name'."
        exit 1
    }

    if [[ "$file_path" == *.fhir ]]; then
        echo "  Formatting the content for: $file_path"
        formatted_content=$(jq '.' "$file_path")
        echo "$formatted_content" >"$file_path"
    fi
}

check_submission_status() {
    # requires: hurl, jq
    local env=$1
    local submission_id=$2
    local private_key=$3

    local timeout=180       # 3 minutes
    local retry_interval=10 # Retry every 10 seconds

    start_time=$(date +%s)

    while true; do
        history_response=$("$CDCTI_HOME"/scripts/rs.sh history -i "$submission_id" -e "$env" -k "$private_key") || {
            exit_code=$?
            if [ $exit_code -ne 0 ]; then
                fail "Expected exit code 0 but got $exit_code for RS history API call"
            fi
        }
        overall_status=$(echo "$history_response" | jq -r '.overallStatus')

        echo -n "  Status: $overall_status"
        if [ "$overall_status" == "Delivered" ]; then
            echo "!"
            return 0
        else
            echo ". Retrying in $retry_interval seconds..."
        fi

        # Check if the timeout has been reached
        current_time=$(date +%s)
        elapsed_time=$((current_time - start_time))
        if [ "$elapsed_time" -ge "$timeout" ]; then
            echo "  Timeout reached after $elapsed_time seconds. Status is still: $overall_status."
            return 1
        fi

        sleep $retry_interval
    done
}

submit_message() {
    # requires: hurl, jq, az
    local env=$1
    local file=$2
    local rs_sender_private_key=$3
    local ti_sender_private_key=$4

    local message_file_path message_file_name message_base_name
    message_file_path="$(dirname "${file}")"
    message_file_name="$(basename "${file}")"
    message_base_name="${message_file_name%.hl7}"
    message_base_name="${message_base_name%"$FILE_NAME_SUFFIX_STEP_0"}"

    msh9=$(awk -F'|' '/^MSH/ { print $9 }' "$file")
    case "$msh9" in
    "ORU^R01^ORU_R01")
        first_leg_receiver="flexion.etor-service-receiver-results"
        second_leg_receiver="flexion.simulated-hospital"
        ;;
    "OML^O21^OML_O21" | "ORM^O01^ORM_O01")
        first_leg_receiver="flexion.etor-service-receiver-orders"
        second_leg_receiver="flexion.simulated-lab"
        ;;
    *)
        echo "Unknown receivers for MSH-9 value '$msh9'. Skipping the message"
        return
        ;;
    esac

    echo "Assuming receivers are '$first_leg_receiver' and '$second_leg_receiver' because of MSH-9 value '$msh9'"

    waters_response=$("$CDCTI_HOME"/scripts/rs.sh waters -f "$message_file_name" -r "$message_file_path" -e "$env" -k "$rs_sender_private_key") || {
        exit_code=$?
        if [ $exit_code -ne 0 ]; then
            fail "Expected exit code 0 but got $exit_code for RS waters API call"
        fi
    }
    submission_id=$(echo "$waters_response" | jq -r '.id')

    echo "[First leg] Checking submission status for ID: $submission_id"
    if ! check_submission_status "$env" "$submission_id" "$rs_sender_private_key"; then
        echo "Failed to deliver the first leg of the message. Skipping the next steps."
        return
    fi

    inbound_submission_id=$(extract_rs_history_submission_id "$history_response")

    translated_blob_name="ready/$first_leg_receiver/$inbound_submission_id.fhir"
    translated_file_path="$message_file_path/$message_base_name$FILE_NAME_SUFFIX_STEP_1.fhir"
    if [ "$env" = "local" ]; then
        download_from_azurite "$translated_blob_name" "$translated_file_path"
    else
        echo "  Snapshot for translated message in blob storage at: $translated_blob_name"
    fi

    echo "[Intermediary] Getting outbound submission ID"
    if [ -n "$ti_sender_private_key" ]; then
        echo "  Attempting to get outbound submission ID from TI's metadata API..."
        metadata_response=$("$CDCTI_HOME"/scripts/ti.sh metadata -i "$inbound_submission_id" -e "$env" -k "$ti_sender_private_key") || {
            echo "Failed to get metadata for inbound submission ID: $inbound_submission_id"
            outbound_submission_id=""
        }
        if [ -n "$metadata_response" ]; then
            outbound_submission_id=$(extract_ti_metadata_submission_id "$metadata_response")
        fi
    fi
    if [ -z "$outbound_submission_id" ] || [ "$outbound_submission_id" = "null" ]; then
        echo -n "  Please enter the outbound submission ID manually (you may find it in the TI $env logs): "
        read -r outbound_submission_id
        if [ -z "$outbound_submission_id" ]; then
            fail "No outbound submission ID provided"
        fi
    fi

    if [ -z "$outbound_submission_id" ] || [ "$outbound_submission_id" = "null" ]; then
        outbound_submission_id=$(extract_ti_metadata_submission_id "$metadata_response")
    fi

    transformed_blob_name="receive/flexion.etor-service-sender/$outbound_submission_id.fhir"
    transformed_file_path="$message_file_path/$message_base_name$FILE_NAME_SUFFIX_STEP_2.fhir"
    if [ "$env" = "local" ]; then
        download_from_azurite "$transformed_blob_name" "$transformed_file_path"
    else
        echo "  Snapshot for transformed message in blob storage at: $translated_blob_name"
    fi

    echo "[Second leg] Checking submission status for ID: $outbound_submission_id"
    if ! check_submission_status "$env" "$outbound_submission_id" "$rs_sender_private_key"; then
        echo "Failed to deliver the second leg of the message. Skipping the next steps."
        return
    fi

    final_submission_id=$(extract_rs_history_submission_id "$history_response")
    final_blob_name="ready/$second_leg_receiver/$final_submission_id.hl7"
    final_file_path="$message_file_path/$message_base_name$FILE_NAME_SUFFIX_STEP_3.hl7"
    if [ "$env" = "local" ]; then
        download_from_azurite "$final_blob_name" "$final_file_path"
    else
        echo "  Snapshot for final message in blob storage at: $translated_blob_name"
    fi
}
