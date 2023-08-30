Logical: ORCSegment
Id: segment-orc-logical-model
Title: "ORC Segment"
Description: "The ORC Segment"
* orderControl 1..1 SU string "ORC-1 Order Control"
* ORCPlacerOrderNumber 1..1 SU string "ORC-2 Placer Order Number"
* fillerOrderNumber 1..1 SU string "ORC-3 Filler Order Number"
* placerGroupNumber 1..1 SU string "ORC-4 Placer Group Number"
* dateTimeOfTransaction 1..1 SU string "ORC-9 Date/Time of Transaction"
* orderingProvider 1..1 SU string "ORC-12 Ordering Provider"
* orderingFacilityName 1..1 SU string "ORC-11 prdering Facility Name"
// other optional fields


// Provenance Section begins
Instance: segment-orc-logical-model-history-create
InstanceOf: Provenance
Title: "Initial creation of ORC segment changelog"
Usage: #definition
* target[+] = Reference(StructureDefinition/segment-orc-logical-model)
* recorded = "2023-08-29T17:54:36.0000Z"
* occurredDateTime = "2023-08-29"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "Created an ORC segment resource"
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#CREATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "T. R. Johnson"
