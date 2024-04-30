POST {{url}}/v1/auth/token
Content-Type: application/x-www-form-urlencoded
[FormParams]
scope: {{client}}
client_assertion: {{jwt}}
