# curl --header 'Content-Type: application/x-www-form-urlencoded' --data 'scope=flexion.*.report' --data 'client_assertion=$(jwt encode --exp=+5min --jti $(uuidgen) --alg RS256 -k $client -i $client -s $client -a $host --no-iat -S @/path/to/ucsd-epic-private-key.pem) --data 'client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer' --data 'grant_type=client_credentials' 'http://localhost:7071/api/token'
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

GET {{url}}/api/waters/report/{{submissionid}}/delivery
Authorization: Bearer {{token}}
