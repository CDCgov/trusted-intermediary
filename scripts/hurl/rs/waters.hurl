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
Content-Type: {{content-type}}
Client: {{client-id}}.{{client-sender}}
Authorization: Bearer {{token}}
file,{{fpath}};
