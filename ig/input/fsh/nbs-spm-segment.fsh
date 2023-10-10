Logical: SPMSegment
Id: segment-spm-logical-model
Title: "SPM Segment"
Description: "The SPM Segment"

* setSPMId 1..1 string "SPM-1 Set ID - SPM"
* specimenType 1..1 string "SPM-4 Specimen Type"
* specimenTypeIdentifier 1..1 string "SPM-4.1 Identifier"
* specimenTypeCodingSystem 1..1 code "SPM-4.3 Name of Coding System"
* specimenRangeStartDateTime 1..1 dateTime "SPM-17.1 Specimen Range Start Date Time"
// other optional fields

// Provenance Section begins
Instance: segment-spm-logical-model-history-create
InstanceOf: Provenance
Title: "Initial creation of SPM segment changelog"
Usage: #definition
* target[+] = Reference(StructureDefinition/segment-spm-logical-model)
* recorded = "2023-08-29T18:19:36.0000Z"
* occurredDateTime = "2023-08-29"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "Created an SPM segment resource"
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#CREATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "T. R. Johnson"


Instance: segment-spm-logical-model-history-update
InstanceOf: Provenance
Title: "Update of converted SPM segment fields and data type correction"
Usage: #definition
* target[+] = Reference(StructureDefinition/segment-spm-logical-model)
* recorded = "2023-10-09T17:41:23.0000Z"
* occurredDateTime = "2023-10-09"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "Changed the sub-segments to the ones the system will initially support."
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#UPDATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "T. Johnson"