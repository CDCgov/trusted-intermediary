# Sample message files

## Naming convention

`<XXX>_<SS>_<HL7_Type>_<SUB_TYPE>_<DESCRIPTION>.[fhir|hl7]`
- `XXX`: incremental id for the message. This id will not change after translation/transformation (e.g. `001`)
- `SS`: state initials or organization name (e.g. `CA`, `Natus`)
- `HL7_Type`: HL7 message type (e.g. `ORU_R01`)
- `SUB_TYPE`: specific HL7 message type (e.g. `NBS`)
- `DESCRIPTION`: any additional description to identify the message type. For message flow description, we'll use this convention:
  - `0_initial_message`
  - `1_hl7_translation`
  - `2_fhir_transformation`
  - `3_hl7_translation_final`
- File extension: either `hl7` or `fhir`

## Current sample files

### States

#### California

- [001_CA_OML_O21.hl7](/examples/CA/001_CA_OML_O21.hl7)
- [002_CA_ORU_R01.hl7](/examples/CA/002_CA_ORU_R01.hl7)

#### Minnesota

- [002_MN_OML_O21_NBS.hl7](/examples/MN/002_MN_OML_O21_NBS.hl7)
- [003_MN_ORM_O01_NBS.hl7](/examples/MN/003_MN_ORM_O01_NBS.hl7)
- [003_MN_ORM_O01_NBS_extended.hl7](/examples/MN/003_MN_ORM_O01_NBS_extended.hl7)
- [003_MN_ORM_O01_NBS_simplified.hl7](/examples/MN/003_MN_ORM_O01_NBS_simplified.hl7)
- [004_MN_ORU_R01_NBS_0_initial_message.hl7](examples/MN/004_MN_ORU_R01_NBS_0_initial_message.hl7)
- [004_MN_ORU_R01_NBS_1_hl7_translation.fhir](/examples/MN/004_MN_ORU_R01_NBS_1_hl7_translation.fhir)
- [004_MN_ORU_R01_NBS_2_fhir_transformation.fhir](/examples/MN/004_MN_ORU_R01_NBS_2_fhir_transformation.fhir)
- [004_MN_ORU_R01_NBS_3_hl7_translation_final.hl7](/examples/MN/004_MN_ORU_R01_NBS_3_hl7_translation_final.hl7)
- [005_MN_ADT_A01.hl7](/examples/MN/005_MN_ADT_A01.hl7)

#### Tennessee

- [001_TN_ORU_R01_LRI.hl7](/examples/TN/001_TN_ORU_R01_LRI.hl7)
- [001_TN_ORU_R01_LRI_annotated_for_transformation.hl7](/examples/TN/001_TN_ORU_R01_LRI_annotated_for_transformation.hl7)
- [001_TN_ORU_R01_LRI_transformed_to_r5.hl7](/examples/TN/001_TN_ORU_R01_LRI_transformed_to_r5.hl7)
- [002_TN_OML_O21_NBS.hl7](/examples/TN/002_TN_OML_O21_NBS.hl7)

#### Texas

- [001_TX_OML_O21.hl7](/examples/TX/001_TX_OML_O21.hl7)
- [002_TX_ORU_R01.hl7](/examples/TX/002_TX_ORU_R01.hl7)

### Organizations

#### Epic

- [001_Epic_ORM_O01.hl7](/examples/Epic/001_Epic_ORM_O01.hl7)
- [002_Epic_ORU_R01.hl7](/examples/Epic/002_Epic_ORU_R01.hl7)

#### Natus

- [001_Natus_ACK.hl7](/examples/Natus/001_Natus_ACK.hl7)

#### NewSTEPs

- [001_NewSTEPs_OML_021.hl7](/examples/NewSTEPs/001_NewSTEPs_OML_021.hl7)
- [002_NewSTEPs_ORU_R01.hl7](/examples/NewSTEPs/002_NewSTEPs_ORU_R01.hl7)

#### Oracle

- [001_Oracle_ORM_O01.hl7](/examples/Oracle/001_Oracle_ORM_O01.hl7)
- [002_Oracle_ORM_O01.hl7](/examples/Oracle/002_Oracle_ORM_O01.hl7)
- [003_Oracle_ORM_O01.hl7](/examples/Oracle/003_Oracle_ORM_O01.hl7)
- [004_Oracle_ORU_R01.hl7](/examples/Oracle/004_Oracle_ORU_R01.hl7)

### Other

- [001_ADT_A01.hl7](/examples/Other/001_ADT_A01.hl7)
- [004_OML_O21.fhir](/examples/Other/004_OML_O21.fhir)

### Test

- [Patient_NBS.fhir](/examples/Test/Patient_NBS.fhir)
- [ORM_O01.fhir](/examples/Test/ORM_O01.fhir)
- [OML_O21_short.fhir](/examples/Test/OML_O21_short.fhir)
- [ORU_R01.fhir](/examples/Test/ORU_R01.fhir)
- [ORU_R01_short.fhir](/examples/Test/ORU_R01_short.fhir)

## Previously renamed files

- `fhir/MN NBS FHIR Order Message.json` => `Test/ORM_O01.fhir`
- `fhir/lab_order.json` => `Test/OML_O21_short.fhir`
- `fhir/newborn_patient.json` => `Test/Patient_NBS.fhir`
- `fhir/oml_message.json` => `Other/004_OML_O21.fhir`
- `hl7/AL/Baptist ORM 1.txt` => `Oracle/001_Oracle_ORM_O01.hl7`
- `hl7/AL/Baptist ORM 2.txt` => `Oracle/002_Oracle_ORM_O01.hl7`
- `hl7/AL/Baptist ORU.hl7` => `Oracle/004_Oracle_ORU_R01.hl7`
- `hl7/CA/CA NBS OML_O21 Lab Order Message.txt` => `CA/001_CA_OML_O21.hl7`
- `hl7/CA/CA NBS ORU_R01 Lab Result Message.txt` => `CA/002_CA_ORU_R01.hl7`
- `hl7/LA/Ochsner ORM.txt` => `Epic/001_Epic_ORM_O01.hl7`
- `hl7/LA/Ochsner ORU.txt` => `Epic/002_Epic_ORU_R01.hl7`
- `hl7/MN/ADT_A01.txt` => `MN/005_MN_ADT_A01.hl7`
- `hl7/MN/MN NBS OML_O21 Lab Order Message.txt` => `MN/002_MN_OML_O21_NBS.hl7`
- `hl7/MN/MN NBS ORM_O01 Lab Order Message.txt` => `MN/003_MN_ORM_O01_NBS.hl7`
- `hl7/MN/MN NBS ORM_O01 Lab Order Message Extended.txt` => `MN/003_MN_ORM_O01_NBS_extended.hl7`
- `hl7/MN/MN NBS ORU_R01 Lab Result Message.txt` => `MN/004_MN_ORU_R01_NBS_0_initial_message.hl7`
- `hl7/MN/ORM simplified.txt` => `MN/003_MN_ORM_O01_NBS_simplified.hl7`
- `hl7/OML_021.hl7` => `NewSTEPs/001_NewSTEPs_OML_021.hl7`
- `hl7/ORU_R01.hl7` => `NewSTEPs/002_NewSTEPs_ORU_R01.hl7`
- `hl7/ADT_A01.txt` => `Other/001_ADT_A01.hl7`
