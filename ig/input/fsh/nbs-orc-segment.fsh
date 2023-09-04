Logical: ORCSegment
Id: segment-orc-logical-model
Title: "ORC Segment"
Description: "The ORC Segment"
* orderControl 1..1 SU id "ORC-1 Order Control"
* placerOrderNumber 1..1 SU Identifier "ORC-2 Placer Order Number"
* placerOrderNumberEntityIdentifier 1..1 SU string "ORC-2.1 Placer Order Number Entity Identifier"
* placerOrderNumberNamespaceID 1..1 SU code "ORC-2.2 Placer Order Number Namespace ID"
* placerOrderNumberUniversalID 1..1 SU string "ORC-2.3 Placer Order Number Universal ID"
* placerOrderNumberUniversalIDType 1..1 SU id "ORC-2.4 Placer Order Number Universal ID Type"
* orderStatus 1..1 SU string "ORC-5 Order Status"
* dateTimeOfTransaction 1..1 SU dateTime "ORC-9 Date/Time of Transaction"
* orderingFacilityName 1..2 SU string "ORC-21 Ordering Facility Name"
* orderingFacilityNameOrganizationName 1..1 SU string "ORC-21.1 Ordering Facility Organization Name"
* orderingFacilityNameTypeCode 1..1 SU code "ORC-21.2 Ordering Facility Type Code"
* orderingFacilityNameAssigningAuthority 1..1 SU code "ORC-21.6 Ordering Facility Assigning Authority"
* orderingFacilityNameIdentifierTypeCode 1..1 SU id "ORC-21.7 Ordering Facility Identifier Type Code"
* orderingFacilityNameOrganizationIdentifier 1..1 SU string "ORC-21.10 Ordering Facility Organization Identifier"

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

Instance: segment-orc-logical-model-history-update
InstanceOf: Provenance
Title: "Addition of ORC-2 and ORC-21 segments"
Usage: #definition
* target[+] = Reference(StructureDefinition/segment-orc-logical-model)
* recorded = "2023-09-04T00:00:00.0000Z"
* occurredDateTime = "2023-09-04"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "Added sub-segments we initially support"
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#UPDATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "basiliskus"
