# Hurl Scripts

## Requirements

- [hurl](https://hurl.dev/)
- [jq](https://jqlang.github.io/jq/)
- [azure-cli](https://learn.microsoft.com/en-us/cli/azure/)
- [jwt-cli](https://github.com/mike-engel/jwt-cli)
- `CDCTI_HOME` environment variable ([see here](../README.md))

## Available Hurl Scripts

### ReportStream

#### Usage

```
Usage: ./rs.sh <ENDPOINT_NAME> [OPTIONS]

ENDPOINT_NAME:
    The name of the endpoint to call (required)

Options:
    -f <REL_PATH>       Path to the hl7/fhir file to submit (Required for waters API)
    -r <ROOT_PATH>      Root path to the hl7/fhir files (Default: /Users/bbogado/Code/Flexion/CDC-TI/trusted-intermediary/examples/)
    -t <CONTENT_TYPE>   Content type for the message (Default: application/hl7-v2)
    -e <ENVIRONMENT>    Environment: local|staging|production (Default: local)
    -c <CLIENT_ID>      Client ID (Default: flexion)
    -s <CLIENT_SENDER>  Client sender (Default: simulated-sender)
    -k <KEY_PATH>       Path to the client private key
    -i <SUBMISSION_ID>  Submission ID for history API
    -v                  Verbose mode
    -h                  Display this help and exit

Environment Variables:
    CDCTI_HOME          Base directory for CDC TI repository (Required)
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
./rs.sh waters -f Test/Orders/003_AL_ORM_O01_NBS_Fully_Populated_0_initial_message.hl7 -e staging -k /path/to/client/staging/private/key
```

Checking the history in local environment for a submission id

```
./rs.sh history -i 100
```

Checking the history in staging for a submission id

```
./rs.sh history -i 100 -e staging -k /path/to/client/staging/private/key
```

### CDC Intermediary

#### Usage

```
Usage: ./ti.sh <ENDPOINT_NAME> [OPTIONS]

ENDPOINT_NAME:
    The name of the endpoint to call (required)

Options:
    -f <REL_PATH>       Path to the hl7/fhir file to submit (Required for orders and results APIs)
    -r <ROOT_PATH>      Root path to the hl7/fhir files (Default: /Users/bbogado/Code/Flexion/CDC-TI/trusted-intermediary/examples/)
    -e <ENVIRONMENT>    Environment: local|staging (Default: local)
    -c <CLIENT>         Client ID to create JWT with (Default: report-stream)
    -k <KEY_PATH>       Path to the client private key
    -i <SUBMISSION_ID>  Submission ID for metadata API (Required for orders, results and metadata API)
    -v                  Verbose mode
    -h                  Display this help and exit

Environment Variables:
    CDCTI_HOME          Base directory for CDC TI repository (Required)
```

#### Examples

Submit an order to local environment:
```
./ti.sh orders -f Test/Orders/003_AL_ORM_O01_NBS_Fully_Populated_1_hl7_translation.fhir -i 100
```

Submit an order to staging:
```
./ti.sh orders -f Test/Orders/003_AL_ORM_O01_NBS_Fully_Populated_0_initial_message.hl7 -e staging -k /path/to/client/staging/private/key

```

Submit a result to local environment:
```
./ti.sh results -f Test/Results/002_AL_ORU_R01_NBS_Fully_Populated_1_hl7_translation.fhir -i 100
```

Get metadata from local environment:
```
./ti.sh metadata -i 100
```

Authenticate to local environment:
```
./ti.sh auth
```

Get OpenAPI docs from local environment:
```
./ti.rs openapi
```

Get Health info from local environment:
```
./ti.sh health
```

### Epic/UCSD

#### Before running the script

- Add the `client` id to `epic.rs`
- Update the `secret` variable path

#### Usage

`./epic.sh results`

## Submission Scripts

- `submit_message.sh`: sends a HL7 message to a locally running RS instance. It also grabs the snapshots of the file in azurite after converting to FHIR, after applying transformations in TI, and after converting back to HL7. It copies these files to the same folder where the submitted file is
    ```
    Usage: ./submit_message.sh -f <message_file.hl7> [-e <environment>]

    Options:
        -f <FILE>                   Message file path (required)
        -e <ENVIRONMENT>            Environment: local|staging|production (Default: )
        -x <RS_CLIENT_PRIVATE_KEY>  Path to the client private key for authentication with RS API
        -z <TI_CLIENT_PRIVATE_KEY>  Path to the client private key authentication with TI API
        -h                          Display this help and exit
    ```
- `update_examples.sh`: sends all the HL7 files with `_0_initial_message.hl7` suffix in the `/examples` folder to a locally running RS instance. As the previous script, it copies the snapshots at each stage
    ```
    ./update_examples.sh
    ```
- `utils.sh`: utility functions for the previous scripts. It has functions to submit requests to RS, check the submission status throughout the whole flow, and downloading snapshots from azurite

**Note**: these scripts require both RS and TI to be running locally
