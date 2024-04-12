POST {{url}}/api/token
Content-Type: application/x-www-form-urlencoded
[FormParams]
scope: {{client-id}}.*.report
client_assertion: {{jwt}}
client_assertion_type: urn:ietf:params:oauth:client-assertion-type:jwt-bearer
grant_type: client_credentials

HTTP 200

[Captures]
token: jsonpath "$['access_token']"

POST {{url}}/api/waters
Content-Type: application/hl7-v2
Client: {{client-id}}.{{client-sender}}
Authorization: Bearer {{token}}
file,Orders/003_AL_ORM_O01_NBS_Fully_Populated_0_initial_message.hl7;

# POST {{url}}/api/waters
# Content-Type: application/fhir+ndjson
# Client: {{client-id}}.{{client-sender}}
# Authorization: Bearer {{token}}
# file,Orders/003_AL_ORM_O01_NBS_Fully_Populated_1_hl7_translation.fhir;
