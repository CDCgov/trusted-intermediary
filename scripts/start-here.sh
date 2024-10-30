#!/bin/bash

if [ ! -f .env ]; then
    cp .env.template .env || {
        echo "Failed to create .env file"
        exit 1
    }
    ${EDITOR:-vi} .env
fi

# Export environment variables
set -a
source .env
set +a
