Logical: OBXSegment
Id: segment-obx-logical-model
Title: "OBX Segment"
Description: "The OBX Segment"
* setOBXId 1..1 SU string "OBX-1 (1) Birthweight an identifier"
* valueType 1..1 SU string "OBX-2 A Value Type"
* observationIdentifier 1..1 SU string "OBX-3 Observation Identifier"
* observationSubID 1..1 SU string "OBX-4 (LOINC 54089-8) Observation Sub-ID"
* observationValue 1..1 SU string "OBX-5 (LOINC or 99MDH) Observation Value"
* units 1..1 SU string "OBX-6 Unit For Observation Value"
* observationResultStatus 1..1 SU string "OBX-11 Observation Result Status"
* dateTimeOfTheObservation 1..1 SU string "OBX-14 Date/time Of Observation"
* observationType 1..1 SU string "OBX-29 Observation Type"
* observationSubType 1..1 SU string "OBX-30 Observation Sub-type"
// other optional


// Provenance Section begins
Instance: segment-obx-logical-model-history-create
InstanceOf: Provenance
Title: "Initial creation of OBX segment changelog"
Usage: #definition
* target[+] = Reference(StructureDefinition/segment-obx-logical-model)
* recorded = "2023-08-29T17:52:36.0000Z"
* occurredDateTime = "2023-08-29"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "Created an OBX segment resource"
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#CREATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "T. R. Johnson"
