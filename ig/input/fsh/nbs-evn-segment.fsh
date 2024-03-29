Logical: EVNSegment
Id: segment-evn-logical-model
Title: "EVN Segment"
Description: "The EVN Segment"
* segmentType 1..1 string "EVN"
* eventTypeCode 1..1 string "A01" // this is apparently not in the spec anymore but retained for backward compatibility
* eventDateTime 1..1 string "Date and Time of the Event"
// there are other optional fields we might want to define


// Provenance Section begins
Instance: segment-evn-logical-model-history-create
InstanceOf: Provenance
Title: "Initial creation of EVN segment changelog"
Usage: #definition
* target[+] = Reference(StructureDefinition/segment-evn-logical-model)
* recorded = "2023-08-29T17:44:36.0000Z"
* occurredDateTime = "2023-08-29"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "Created an EVN segment resource"
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#CREATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "T. R. Johnson"
