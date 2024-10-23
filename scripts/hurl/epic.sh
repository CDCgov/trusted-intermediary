#!/bin/bash

source ./utils.sh

client=
audience=https://epicproxy-np.et0502.epichosted.com/FhirProxy/oauth2/token
secret=/path/to/ucsd-epic-private-key.pem
root=$CDCTI_HOME/examples/CA/
fpath="$1"
shift

jwt_token=$(generate_jwt "$client" "$audience" "$secret") || fail "Failed to generate JWT token"

hurl \
    --variable "fpath=$fpath" \
    --file-root "$root" \
    --variable "jwt=$jwt_token" \
    epic/results.hurl \
    $@
