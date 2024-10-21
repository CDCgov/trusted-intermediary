# CDC Intermediary Hurl Script

## Usage

```
Usage: hrl <HURL_FILE> [OPTIONS]

Options:
    -f <REL_PATH>         The path to the hl7/fhir file to submit, relative the root path (Required for orders and results APIs)
    -r <ROOT_PATH>        The root path to the hl7/fhir files (Default: $CDCTI_HOME/examples/)
    -e [local | staging]  The environment to run the test in (Default: local)
    -c <CLIENT>           The client id to use (Default: report-stream)
    -j <JWT>              The JWT to use for authentication
    -i <SUBMISSION_ID>    The submissionId to call the metadata API with (Required for metadata API)
    -v                    Verbose mode
    -h                    Display this help and exit
```

## Examples

Submit an order to local environment:
```
./hrl orders.hurl -f Test/Orders/003_AL_ORM_O01_NBS_Fully_Populated_1_hl7_translation.fhir -i 100
```

Submit an order to staging:
```
./hrl orders.hurl -f Test/Orders/003_AL_ORM_O01_NBS_Fully_Populated_0_initial_message.hl7 -e staging -j eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ

```

Submit a result to local environment:
```
./hrl results.hurl -f Test/Results/002_AL_ORU_R01_NBS_Fully_Populated_1_hl7_translation.fhir -i 100
```

Get metadata from local environment:
```
./hrl metadata -i 100
```

Authenticate to local environment:
```
./hrl auth.hurl
```

Get OpenAPI docs from local environment:
```
./hrl openapi.hurl
```

Get Health info from local environment:
```
./hrl health.hurl
```
