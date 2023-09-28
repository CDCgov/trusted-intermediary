Logical: SPMSegment
Id: segment-spm-logical-model
Title: "SPM Segment"
Description: "The SPM Segment"
* setSPMId 1..1 string "SPM-1 Set ID - SPM"
* assignedIdentifier 1..1 string "SPM-2.1 Placer Assigned Identifier"
* entityIdentifier 1..1 string "SPM-2.1.1 Entity Identifier"
* specimenType 1..1 string "SPM-4 Specimen Type"
* identifier 1..1 string "SPM-4.1 Identifier"
* codingSystem 1..1 string "SPM-4.3 Name of Coding System"
* specimenCollectionDateTime 1..1 string "SPM-17	Specimen Collection Date Time"
* rangeStartDateTime 1..1 string "SPM-17.1: Range Start Date Time"
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
