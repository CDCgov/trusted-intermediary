# Scripts

## Instructions

1. Copy `.env.template` to `.env`
    ```
    cp .env.template .env
    ```
2. Edit `.env` and make sure to update at least `CDCTI_HOME` (local path to the `trusted-intermediary` codebase) and `RS_HOME` (local path to the `prime-reportstream` codebase) are set. **Note**: if you don't set `CDCTI_HOME`, none of these scripts will work. Also, please use `$HOME` or the full path to your home directory instead of `~`
3. Load the environment variables in `.env` by running
   ```
   source .env
   ```
   **Note**: you may also want to add it to your shell's startup file so you don't need to run it for every terminal session.
4. Run your script

## Available Scripts

### submit.sh

Sends a HL7 message to RS and tracks its status throughout the flow until final delivery. When running locally, it grabs the snapshots of the file in azurite after converting to FHIR, after applying transformations in TI, and after converting back to HL7; and it copies those files to the same folder where the submitted file is. If running in a deployed environment we currently don't have a way to download the files from Azure, but the script will print the relative path for the files in the blob storage container.

#### Requirements

- hurl
- jq
- azure-cli

#### Usage

```
Usage: submit.sh -f <message_file.hl7> [-e <environment>]

Options:
    -f <FILE>                   Message file path (Required)
    -e <ENVIRONMENT>            Environment: local|staging|production (Default: )
    -x <RS_CLIENT_PRIVATE_KEY>  Path to the client private key for authentication with RS API (Required for non-local environments)
    -z <TI_CLIENT_PRIVATE_KEY>  Path to the client private key for authentication with TI API (Optional for all environments)
    -h                          Display this help and exit
```

### rs.sh

Submit requests to RS API endpoints

#### Requirements

- hurl
- jwt-cli

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
    -k <KEY_PATH>       Path to the client private key (Required for non-local environments)
    -i <SUBMISSION_ID>  Submission ID for history API (Required for history API)
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

### ti.sh

Submit requests to TI API endpoints

#### Requirements

- hurl
- jwt-cli

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
    -k <KEY_PATH>       Path to the client private key (Required for non-local environments)
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

### epic.sh

Submit requests to Epic API endpoints

#### Requirements

- hurl

#### Before running the script

- Add the `client` id to `epic.rs`
- Update the `secret` variable path

#### Usage

`./epic.sh results`

### setup/update-examples-snapshots.sh

Sends all the HL7 files with `_0_initial_message.hl7` suffix in the `/examples` folder to a locally running RS instance. As the previous script, it copies the snapshots at each stage

#### Requirements

- hurl
- jq
- azure-cli

#### Usage

```
./update-examples-snapshots.sh
```

### setup/setup-reportstream.sh

Setup script for ReportStream

#### Requirements

- yq

#### Usage

```
./setup-reportstream.sh
```

### lib/common.sh

Utility functions shared by scripts

### lib/submission-utils.sh

Functions to submit requests to RS, check the submission status throughout the whole flow, and downloading snapshots from azurite

## References

- [hurl](https://hurl.dev/)
- [jq](https://jqlang.github.io/jq/)
- [yq](https://github.com/mikefarah/yq)
- [azure-cli](https://learn.microsoft.com/en-us/cli/azure/)
- [jwt-cli](https://github.com/mike-engel/jwt-cli)
