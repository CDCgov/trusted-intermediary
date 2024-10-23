#!/bin/bash

FILE_NAME_SUFFIX_STEP_0="_0_initial_message"
FILE_NAME_SUFFIX_STEP_1="_1_hl7_translation"
FILE_NAME_SUFFIX_STEP_2="_2_fhir_transformation"
FILE_NAME_SUFFIX_STEP_3="_3_hl7_translation_final"

AZURITE_CONNECTION_STRING="DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://localhost:10000/devstoreaccount1;" # pragma: allowlist secret

LOCAL_HOST="localhost"
RS_STG_HOST="staging.prime.cdc.gov"
RS_PRD_HOST="prime.cdc.gov"
RS_LOCAL_PORT=7071
TI_STG_HOST="cdcti-stg-api.azurewebsites.net"
TI_PRD_HOST="cdcti-prd-api.azurewebsites.net"
TI_LOCAL_PORT=8080

check_installed_commands() {
    for cmd in "$@"; do
        if ! command -v "$cmd" &>/dev/null; then
            echo "$cmd could not be found. Please install $cmd to proceed."
            exit 1
        fi
    done
}

check_apis() {
    for service in "$@"; do
        if ! curl -s --head --fail "$service" | grep "200 OK" >/dev/null; then
            echo "The service at $service is not reachable"
            exit 1
        fi
    done
}

check_env_vars() {
    local env_vars=("$@")
    for var in "${env_vars[@]}"; do
        if [ -z "${!var}" ]; then
            echo "Error: Environment variable '$var' is not set"
            exit 1
        fi
    done
}

get_endpoint_url() {
    local type=$1
    local env=$2
    local protocol host port

    case "$env" in
    local)
        protocol="http"
        host="$LOCAL_HOST"
        port=$([ "$type" = "rs" ] && echo "$RS_LOCAL_PORT" || echo "$TI_LOCAL_PORT")
        ;;
    staging)
        protocol="https"
        host=$([ "$type" = "rs" ] && echo "$RS_STG_HOST" || echo "$TI_STG_HOST")
        port=443
        ;;
    production)
        protocol="https"
        host=$([ "$type" = "rs" ] && echo "$RS_PRD_HOST" || echo "$TI_PRD_HOST")
        port=443
        ;;
    *)
        echo "Error: Invalid environment '$env'"
        show_help
        exit 1
        ;;
    esac

    echo "$protocol://$host:$port"
}

generate_jwt() {
    # requires: jwt-cli
    local client_id=$1
    local client_sender=$2
    local host=$3
    local secret_path=$4

    jwt encode \
        --exp='+5min' \
        --jti "$(uuidgen)" \
        --alg RS256 \
        -k "$client_id.$client_sender" \
        -i "$client_id.$client_sender" \
        -s "$client_id.$client_sender" \
        -a "$host" \
        --no-iat \
        -S "@$secret_path"
}

check_submission_status() {
    # requires: hurl, jq
    local submission_id=$1
    local timeout=180       # 3 minutes
    local retry_interval=10 # Retry every 10 seconds

    start_time=$(date +%s)

    while true; do
        history_response=$(./rs.sh history -i "$submission_id")
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

extract_submission_id() {
    # requires: jq
    local history_response=$1
    echo "$history_response" | jq '.destinations[0].sentReports[0].externalName' | sed 's/.*-\([0-9a-f]\{8\}-[0-9a-f]\{4\}-[0-9a-f]\{4\}-[0-9a-f]\{4\}-[0-9a-f]\{12\}\)-.*/\1/'
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

submit_message() {
    # requires: hurl, jq, az
    local file=$1
    local message_file_path=$(dirname "$file")
    local message_file_name=$(basename "$file")
    local message_base_name="${message_file_name%.hl7}"
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

    waters_response=$(./rs.sh waters -f "$message_file_name" -r "$message_file_path")
    submission_id=$(echo "$waters_response" | jq -r '.id')

    echo "[First leg] Checking submission status for ID: $submission_id"
    if ! check_submission_status "$submission_id"; then
        echo "Failed to deliver the first leg of the message. Skipping the next steps."
        return
    fi

    inbound_submission_id=$(extract_submission_id "$history_response")
    translated_blob_name="ready/$first_leg_receiver/$inbound_submission_id.fhir"
    translated_file_path="$message_file_path/$message_base_name$FILE_NAME_SUFFIX_STEP_1.fhir"
    download_from_azurite "$translated_blob_name" "$translated_file_path"

    metadata_response=$(./ti.sh metadata -i "$inbound_submission_id")
    outbound_submission_id=$(echo "$metadata_response" | jq -r '.issue[] | select(.details.text == "outbound submission id") | .diagnostics')

    transformed_blob_name="receive/flexion.etor-service-sender/$outbound_submission_id.fhir"
    transformed_file_path="$message_file_path/$message_base_name$FILE_NAME_SUFFIX_STEP_2.fhir"
    download_from_azurite "$transformed_blob_name" "$transformed_file_path"

    echo "[Second leg] Checking submission status for ID: $outbound_submission_id"
    if ! check_submission_status "$outbound_submission_id"; then
        echo "Failed to deliver the second leg of the message. Skipping the next steps."
        return
    fi

    final_submission_id=$(extract_submission_id "$history_response")
    final_blob_name="ready/$second_leg_receiver/$final_submission_id.hl7"
    final_file_path="$message_file_path/$message_base_name$FILE_NAME_SUFFIX_STEP_3.hl7"
    download_from_azurite "$final_blob_name" "$final_file_path"
}
