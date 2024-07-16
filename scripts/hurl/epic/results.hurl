# curl -H 'Content-Type: application/x-www-form-urlencoded' --data 'client_assertion=$(jwt encode --exp=+5min --jti $(uuidgen) --alg RS256 -k $client -i $client -s $client -a https://epicproxy-np.et0502.epichosted.com/FhirProxy/oauth2/token --no-iat -S @/path/to/ucsd-epic-private-key.pem)' --data 'client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer' --data 'grant_type=client_credentials' 'https://epicproxy-np.et0502.epichosted.com/FhirProxy/oauth2/token'
POST https://epicproxy-np.et0502.epichosted.com/FhirProxy/oauth2/token
Content-Type: application/x-www-form-urlencoded
[FormParams]
client_assertion: {{jwt}}
client_assertion_type: urn:ietf:params:oauth:client-assertion-type:jwt-bearer
grant_type: client_credentials

HTTP 200

[Captures]
token: jsonpath "$['access_token']"

# curl -H 'Content-Type: application/hl7-v2' -H 'Authorization: Bearer $token' --data-binary '@/path/to/oru.hl7' 'https://epicproxy-np.et0502.epichosted.com/CDPH_NBGS_TST/api/epic/2015/EDI/HTTP/HL7v2/910377'
POST https://epicproxy-np.et0502.epichosted.com/CDPH_NBGS_TST/api/epic/2015/EDI/HTTP/HL7v2/910377
Content-Type: application/hl7-v2
Authorization: Bearer {{token}}
file,{{fpath}};
