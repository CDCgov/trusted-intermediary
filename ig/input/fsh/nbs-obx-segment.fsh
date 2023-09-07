Logical: OBXSegment
Id: segment-obx-logical-model
Title: "OBX Segment"
Description: "The OBX Segment"
* setOBXId 1..1 SU string "OBX-1 (1) Birthweight an identifier"
* valueType 0..1 SU id "OBX-2 A Value Type"
* observationIdentifier 1..1 SU code "OBX-3 Observation Identifier"
* identifier 1..1 SU string "OBX-3.1 Identifier"
* observationtText 1..1 SU string "OBX-3.2 Text"
* observationNameOfCodingSystem 1..1 SU id "OBX-3.3 Name of Coding System"
* observationOriginalText 1..1 SU string "OBX-3.9 Original Text"
* observationSubID 1..1 SU string "OBX-4 (LOINC 54089-8) Observation Sub-ID"
* observationGroup 1..1 SU string "OBX-4.2 Observation Group"
* observationSequence 1..1 SU string "OBX-4.3 Observation Sequence"
* observationSubIdentifier 1..1 SU string "OBX-4.4 Observation Identifier"
* observationValue 1..1 SU string "OBX-5 (LOINC or 99MDH) Observation Value"
* observationValueIdentifier 1..1 SU string "OBX-5.1 Observation Identifier"
* observationValueText 0..1 SU string "OBX-5.2 Observation Text"
* observationValueNameOfCodingSystem 1..1 SU string "OBX-5.3 Observation Name of Coding System"
* observationValueUnits 1..1 SU code "OBX-6 Unit For Observation Value"
* observationResultStatus 1..1 SU id "OBX-11 Observation Result Status"
* dateTimeOfTheObservation 1..1 SU dateTime "OBX-14 Date/time Of Observation"
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


Instance: segment-obx-logical-model-history-update
InstanceOf: Provenance
Title: "addition of required OBX segment fields and cardinality correction"
Usage: #definition
* target[+] = Reference(StructureDefinition/segment-obx-logical-model)
* recorded = "2023-09-07T17:41:23.0000Z"
* occurredDateTime = "2023-09-07"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "changed the sub-segments to the ones the system will initially support."
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#UPDATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "T. Johnson"
