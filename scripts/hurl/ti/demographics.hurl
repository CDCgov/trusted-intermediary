POST {{url}}/v1/auth/token
Content-Type: application/x-www-form-urlencoded
[FormParams]
scope: {{client}}
client_assertion: {{jwt}}

HTTP 200

[Captures]
token: jsonpath "$['access_token']"

POST {{url}}/v1/etor/demographics
Authorization: Bearer {{token}}
file,{{fpath}};
