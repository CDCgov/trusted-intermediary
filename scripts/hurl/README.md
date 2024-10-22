# Hurl Scripts

## Requirements

- [hurl](https://hurl.dev/)
- [jq](https://jqlang.github.io/jq/)
- [azure-cli](https://learn.microsoft.com/en-us/cli/azure/)
- `CDCTI_HOME` environment variable ([see here](../README.md))

## Available Hurl Scripts

### ReportStream

#### Usage

```
Usage: ./rs.sh <ENDPOINT_NAME> [OPTIONS]

Options:
    -f <REL_PATH>                       The path to the hl7/fhir file to submit, relative the root path (Required for waters API)
    -r <ROOT_PATH>                      The root path to the hl7/fhir files (Default: $CDCTI_HOME/examples/)
    -t <CONTENT_TYPE>                   The content type for the message (e.g. 'application/hl7-v2' or 'application/fhir+ndjson') (Default: application/hl7-v2)
    -e [local | staging | production ]  The environment to run the test in (Default: local)
    -c <CLIENT_ID>                      The client id to use (Default: flexion)
    -s <CLIENT_SENDER>                  The client sender to use (Default: simulated-sender)
    -x <KEY_PATH>                       The path to the client private key for the environment
    -i <SUBMISSION_ID>                  The submissionId to call the history API with (Required for history API)
    -v                                  Verbose mode
    -h                                  Display this help and exit
```

#### Examples

Sending an order to local environment

```
./rs.sh waters -f Test/Orders/003_AL_ORM_O01_NBS_Fully_Populated_0_initial_message.hl7
```

Sending a result to local environment

```
./rs.sh waters -f Test/Results/002_AL_ORU_R01_NBS_Fully_Populated_0_initial_message.hl7
```

Sending an order to staging

```
./rs.sh waters -f Test/Orders/003_AL_ORM_O01_NBS_Fully_Populated_0_initial_message.hl7 -e staging -x /path/to/staging/private/key
```

Checking the history in local environment for a submission id

```
./rs.sh history -i 100
```

Checking the history in staging for a submission id

```
./rs.sh history -i 100 -e staging -x /path/to/staging/private/key
```

### CDC Intermediary

#### Usage

```
Usage: ti.rs <ENDPOINT_NAME> [OPTIONS]

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

#### Examples

Submit an order to local environment:
```
./ti.rs orders -f Test/Orders/003_AL_ORM_O01_NBS_Fully_Populated_1_hl7_translation.fhir -i 100
```

Submit an order to staging:
```
./ti.rs orders -f Test/Orders/003_AL_ORM_O01_NBS_Fully_Populated_0_initial_message.hl7 -e staging -j eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ

```

Submit a result to local environment:
```
./ti.rs results -f Test/Results/002_AL_ORU_R01_NBS_Fully_Populated_1_hl7_translation.fhir -i 100
```

Get metadata from local environment:
```
./ti.rs metadata -i 100
```

Authenticate to local environment:
```
./ti.rs auth
```

Get OpenAPI docs from local environment:
```
./ti.rs openapi
```

Get Health info from local environment:
```
./ti.rs health
```

### Epic/UCSD

#### Before running the script

- Add the `client` id to `epic.rs`
- Update the `secret` variable path

#### Usage

`./epic.rs results`

## Local Submission Scripts

- `submit_message.sh`: sends a HL7 message to a locally running RS instance. It also grabs the snapshots of the file in azurite after converting to FHIR, after applying transformations in TI, and after converting back to HL7. It copies these files to the same folder where the submitted file is
    ```
    ./submit_message.sh /path/to/message.hl7
    ```
- `update_examples.sh`: sends all the HL7 files with `_0_initial_message.hl7` suffix in the `/examples` folder to a locally running RS instance. As the previous script, it copies the snapshots at each stage
    ```
    ./update_examples.sh
    ```
- `message_submission_utils.sh`: utility functions for the previous scripts. It has functions to submit requests to RS, check the submission status throughout the whole flow, and downloading snapshots from azurite

**Note**: these scripts require both RS and TI to be running locally
