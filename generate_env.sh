#!/bin/bash

# Define the content of the .env file



env_content="ENV=local
KEY_VAULT-NAME=ti-key-vault-name
REPORT_STREAM_URL_PREFIX=http://localhost:7071
STORAGE_ACCOUNT_BLOB_ENDPOINT=https://cdctiinternal.blob.core.windows.net
METADATA_CONTAINER_NAME=metadata
DB_URL=postgresql # for gradlew: DB_URL=localhost
DB_PORT=5432      # for gradlew: DB_PORT=5433
DB_NAME=intermediary
DB_USER=intermediary
DB_PASS=changeIT!
DB_SSL=require
DB_MAX_LIFETIME=86220000"

# Get directory of script file
script_dir="$(dirname "$0")"

# Path to the resource folder
folder_path="$script_dir/shared/src/main/resources"

# Check if folder exists, and create it if not
if [ ! -d "$folder_path" ]; then
    mkdir -p "$folder_path" || { echo "Error creating directory."; exit 1; }
fi

# Create or overwrite the .env file
echo -e "$env_content" > "$folder_path/.env" || { echo "Error creating .env file."; exit 1; }

# Display comments
echo "The .env file has been created in $folder_path with the following content: "
echo -e "$env_content"
