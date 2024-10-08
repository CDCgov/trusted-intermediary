# ReportStream Integration Test

The ReportStream Integration Test is a framework meant to add test coverage for the integration between the
Intermediary and ReportStream. It's scheduled to run daily using the
[automated-staging-test-run.yml](/.github/workflows/automated-staging-test-run.yml) workflow

Information on how to set up the sample files evaluated by the tests can be found [here](/examples/Test/Automated/README.md)

## Running the tests

- Automatically - these are scheduled to run every weekday
- Manually via Github
  - Run the [automated-staging-test-submit](/.github/workflows/automated-staging-test-submit.yml) action
  - Wait for RS and TI to finish processing files
  - Run the [automated-staging-test-run](/.github/workflows/automated-staging-test-run.yml) action
- Locally
  - Set the `AZURE_STORAGE_CONNECTION_STRING` environment variable to the [value in Keybase](keybase://team/cdc_ti/service_keys/TI/staging/azure-storage-connection-string-for-automated-rs-e2e-tests.txt)
  - Run the tests with `./gradlew rs-e2e:clean rs-e2e:automatedTest`

## Assertions Definition

The assertions for the integration tests are defined in the
[assertion_definitions.json](/rs-e2e/src/main/resources/assertion_definitions.json) file, which uses
the same rules engine framework as the transformations and validations in the [etor](/etor) project

### File Structure

The file contains a list of definitions which each contain:

- `name`: a descriptive name for the assertions group
- `conditions`: a list of conditions to be met. These determine whether this set of
  assertions apply to the file being evaluated. When no conditions are included, the definition
  applies to all files. If conditions are included, all of them must be satisfied for the
  definition to apply. Conditions are structured the same way as rules
- `rules`: a list of assertions to evaluate

#### Rules

The rules are the assertions for the integration test. The assertions are HL7 expressions inspired
by `FHIRPath`. The current assertions we allow are: equality, non-equality, membership, and
segment count. We can evaluate strings and/or values in HL7 fields. An HL7 field in a rule
can be in either the input file or the output file. If no file is specified, we assume it's the output.
Each rule is contained in double quotes and any string values are contained in single quotes

Examples:

- Equality between an HL7 field in the output and input
  - `"MSH-10 = input.MSH-10"` - `MSH-10` has the same value in the output and input files
  - `"output.MSH-10 = input.MSH-10"` - same as above
- Equality between an HL7 field and a string
  - `"MSH-4 = 'CDPH'"` - the value of `MSH-4` in the output file equals `CDPH`
  - `"MSH-4 != ''"` - the value of `MSH-4` in the output file doesn't equal an empty string
- Membership
  - `"MSH-6 in ('R797', 'R508')"` - the value of `MSH-6` in the output file is either `R797` or `R508`
- Segment count
  - `"OBR.count() = 1"` - there is only one `OBR` segment in the file
