client=
audience=https://epicproxy-np.et0502.epichosted.com/FhirProxy/oauth2/token
secret=/path/to/ucsd-epic-private-key.pem
root=$CDCTI_HOME/examples/CA/
fpath="$1"
shift

hurl \
    --variable fpath=$fpath \
    --file-root $root \
    --variable jwt=$(jwt encode --exp='+5min' --jti $(uuidgen) --alg RS256 -k $client -i $client -s $client -a $audience --no-iat -S @$secret) \
    epic/results.hurl \
    $@
