# Hurl Scripts

## Requirements

- [hurl](https://hurl.dev/)

## ReportStream

### Usage

```
Usage: hrl <HURL_FILE> [OPTIONS]

Options:
    -f <REL_PATH>         The path to the hl7/fhir file to submit, relative the root path (Required for waters API)
    -r <ROOT_PATH>        The root path to the hl7/fhir files (Default: $CDCTI_HOME/examples/Test/)
    -e [local | staging]  The environment to run the test in (Default: local)
    -c <CLIENT_ID>        The client id to use (Default: flexion)
    -s <CLIENT_SENDER>    The client sender to use (Default: simulated-lab)
    -x <KEY_PATH>         The path to the client private key for the environment
    -i <SUBMISSION_ID>    The submissionId to call the history API with (Required for history API)
    -v                    Verbose mode
    -h                    Display this help and exit
```

### Examples

Sending an order to local environment
```
./hrl waters.hurl -f Orders/003_AL_ORM_O01_NBS_Fully_Populated_0_initial_message.hl7
```

Sending a result to local environment
```
./hrl waters.hurl -f Results/002_AL_ORU_R01_NBS_Fully_Populated_0_initial_message.hl7
```

Sending an order to staging
```
./hrl waters.hurl -f Orders/003_AL_ORM_O01_NBS_Fully_Populated_0_initial_message.hl7 -e staging -x /path/to/staging/private/key
```

Checking the history for a submision id
```
./hrl history.hurl -i 100
```
