This folder contains the HL7 sample files that are used in the Golden Copy ReportStream
integration tests. The [golden-copy-staging-test-submit.yml](/.github/workflows/automated-staging-test-submit.yml)
Github workflow that runs daily will grab these files and send them to ReportStream in staging.
The files are expected to go through the whole flow and to be delivered to an Azure blob
container that will later be used by the
[golden-copy-staging-test-run.yml](/.github/workflows/golden-copy-staging-test-run.yml) workflow to run tests on them by comparing the golden copy to the actual output.

## Requirements for the HL7 files

The files are required to:

- Be a valid HL7 file
- Be a supported HL7 message type: `ORM`, `OML` or `ORU`
- Have `automated-staging-test-receiver-id` in `MSH-6.2` in order to be routed correctly
    - If it's a sample file for UCSD, which has transformation that will overwrite `MSH-6.2`, the HL7 is required to have the prefix `AUTOMATEDTEST-` in `MSH-10` as a workaround - there is special routing in ReportStream to handle this
- Each file must have a unique value in `MSH-10`. We use this value to match the input and output files, so if it's not unique, we won't be able to match the files correctly
    - We format `MSH-10` based on the file index, like `001` (or `AUTOMATEDTEST-001` for UCSD)
- `MSH-11` needs to have a value of `N`
- `MSH-3` needs to be `GOLDEN-COPY`

## Corresponding File Names

The files map to each test scenario with the following naming:

- 001_ORU_R01_golden_copy_input_acylc_1.hl7 -> Acylcarnitine - Indeterminate - SCADD - IBCDD patterns.hl7
- 002_ORU_R01_golden_copy_input_acylc_2.hl7 -> Acylcarnitine - Indeterminate_2nd_example.hl7
- 003_ORU_R01_golden_copy_input_aa_unusual.hl7 -> Amino Acid - Unusual Pattern.hl7.hl7
- 004_ORU_R01_golden_copy_input_in_range.hl7 -> Out of Range - Hemoglobinopathies - Positive Marker.hl7
- 005_ORU_R01_golden_copy_input_out_of_range.hl7 -> In range example.hl7
- 006_ORU_R01_golden_copy_input_sma_incomplete.hl7 -> SMA Incomplete - Equivocal - Inadequate Specimen for SMA.hl7
