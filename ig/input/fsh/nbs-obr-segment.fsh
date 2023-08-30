Logical: OBRSegment
Id: segment-obr-logical-model
Title: "OBR Segment"
Description: "The OBR Segment"
* setOBRId 1..1 SU string "OBR-1 Set ID"
* OBRplacerOrderNumber 1..1 SU string "OBR-2 Placer Order Number"
* fillerOrderNumber 1..1 SU string "OBR-3 Filler Order Number"
* universalServiceIdentifier 1..1 SU string "OBR-4 Universal Service Identifier"
* observationDateTime 1..1 SU string "OBR-7 Observation Date/Time"
* observationEndDateTime 1..1 SU string "OBR-8 Observation End Time"
* specimenID 1..1 SU string "OBR-15 or OBR-16  Specimen Id"
* specimenType 1..1 SU string "OBR-15 or OBR-16 Specimen Type"
* specimenCollectionDateTime 1..1 SU string "OBR-7 Specimen Collection Date/Time"
// other optional fields


// Provenance Section begins
Instance: segment-obr-logical-model-history-create
InstanceOf: Provenance
Title: "Initial creation of OBR segment changelog"
Usage: #definition
* target[+] = Reference(StructureDefinition/segment-obr-logical-model)
* recorded = "2023-08-29T17:50:36.0000Z"
* occurredDateTime = "2023-08-29"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "Created an OBR segment resource"
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#CREATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "T. R. Johnson"
