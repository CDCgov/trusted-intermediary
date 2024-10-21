# Hurl Scripts

## Requirements

- [hurl](https://hurl.dev/)
- [jq](https://jqlang.github.io/jq/)
- [azure-cli](https://learn.microsoft.com/en-us/cli/azure/)
- `CDCTI_HOME` environment variable ([see here](../README.md))

## Available Hurl Scripts

- [ReportStream](./rs/): scripts to send requests to ReportStream's endpoints
- [CDC Intermediary](./ti/): scripts to send requests to the CDC Intermediary's endpoints
- [Epic/UCSD](./epic/): scripts to send requests to Epic endpoints for UCSD

## Local Submission Scripts

- `submit_message.sh`: sends a HL7 message to a locally running RS instance. It also grabs the snapshots of the file in azurite after converting to FHIR, after applying transformations in TI, and after converting back to HL7. It copies these files to the same folder where the submitted file is
    ```
    Usage: ./submit_message.sh /path/to/message.hl7
    ```
- `update_examples.sh`: sends all the HL7 files with `_0_initial_message.hl7` suffix in the `/examples` folder to a locally running RS instance. As the previous script, it copies the snapshots at each stage
    ```
    Usage: ./update_examples.sh
    ```
- `message_submission_utils.sh`: utility functions for the previous scripts. It has functions to submit requests to RS, check the submission status throughout the whole flow, and downloading snapshots from azurite

**Note**: these scripts require both RS and TI to be running locally
