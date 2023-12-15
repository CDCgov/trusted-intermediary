#!/bin/bash

# Define the content of the .env file
env_content="ENV=local\nKEY_VAULT_NAME=ti-key-vault-name
\nREPORT_STREAM_URL_PREFIX=http://localhost:7071\nDB_URL=localhost
\nDB_PORT=5433\nDB_NAME=intermediary\nDB_USER=intermediary
\nDB_PASS=changeIT!\nDB_SSL=require"

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
