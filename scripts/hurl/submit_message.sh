#!/bin/bash

source ./message_submission_utils.sh

check_prerequisites

if [ $# -eq 0 ]; then
    echo "Usage: $0 /path/to/message.hl7"
    exit 1
fi

submit_message "$1"
