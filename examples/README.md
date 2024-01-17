# Sample message files

## Naming convention

`<XXX>_<SS>_<HL7_Type>_<SUB_TYPE>_<DESCRIPTION>.[fhir|hl7]`
- `XXX`: incremental id for the message. This id will not change after translation/transformation (e.g. `001`)
- `SS`: state initials or organization name (e.g. `CA`, `Natus`)
- `HL7_Type`: HL7 message type (e.g. `ORU_R01`)
- `SUB_TYPE`: specific HL7 message type (e.g. `NBS`)
- `DESCRIPTION`: any additional description to identify the message type (e.g. `translation_from_initial_hl7_ingestion`)
- File extension: either `hl7` or `fhir`

## Current sample files

### States

#### Alabama

- []()

#### California

- []()

#### Louisiana

- []()

#### Minnesota

- []()

#### Texas

- []()

### Organizations

#### Epic

- []()

#### Natus

- []()

#### NewSTEPs

- []()

#### Oracle

- []()

### Other

- []()

## Previously renamed files

- `fhir/MN NBS FHIR Order Message.json` => `MN/001_MN_NBS_Order.fhir`
- `fhir/lab_order.json` => `Other/002_Order.fhir`
- `fhir/newborn_patient.json` => `Other/003_NBS_patient.fhir`
- `fhir/oml_message.json` => `Other/004_OML_O21.fhir`
- `hl7/AL/Baptist ORM 1.txt` => `Oracle/001_Oracle_ORM_O01.hl7`
- `hl7/AL/Baptist ORM 2.txt` => `Oracle/002_Oracle_ORM_O01.hl7`
- `hl7/AL/Baptist ORU.hl7` => `Oracle/004_Oracle_ORU_R01.hl7`
- `hl7/CA/CA NBS OML_O21 Lab Order Message.txt` => `CA/001_CA_OML_O21.hl7`
- `hl7/CA/CA NBS ORU_R01 Lab Result Message.txt` => `CA/002_CA_ORU_R01.hl7`
- `hl7/LA/Ochsner ORM.txt` => `Epic/001_Epic_ORM_O01.hl7`
- `hl7/LA/Ochsner ORU.txt` => `Epic/002_Epic_ORU_R01.hl7`
- `hl7/MN/ADT_A01.txt` => `MN/001_MN_ADT_A01.hl7`
- `hl7/MN/MN NBS OML_O21 Lab Order Message.txt` => `MN/002_MN_NBS_OML_O21.hl7`
- `hl7/MN/MN NBS ORM_O01 Lab Order Message.txt` => `MN/003_MN_NBS_ORM_O01.hl7`
- `hl7/MN/MN NBS ORM_O01 Lab Order Message Extended.txt` => `MN/003_MN_NBS_ORM_O01_extended.hl7`
- `hl7/MN/MN NBS ORU_R01 Lab Result Message.txt` => `MN/004_MN_NBS_ORU_R01.hl7`
- `hl7/MN/ORM simplified.txt` => `MN/003_MN_NBS_ORM_O01_simplified.hl7`
- `hl7/OML_021.hl7` => `NewSTEPs/001_NewSTEPs_OML_021.hl7`
- `hl7/ORU_R01.hl7` => `NewSTEPs/002_NewSTEPs_ORU_R01.hl7`
- `hl7/ADT_A01.txt` => `Other/005_ADT_A01.hl7`
