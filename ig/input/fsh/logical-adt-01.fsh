// defines the ADT-01 input that is expected for newborn screening

Logical: ADT01
Id: adt-01-logical-model
Title: "ADT-01"
Description: "The expected input for the demographic data for the newborn screening ETOR workflow"
* MSH 1..1 SU MSHSegment "MSH segment"
* EVN 1..1 SU EVNSegment "EVN segment"
* PID 1..1 SU PIDSegment "PID segment"
* messageString 0..1 SU string "the full message as delimeted text"


// Provenance Section begins
Instance: adt01-initial-history-create
InstanceOf: Provenance
Title: "Initial creation of ADT01 message changelog"
Usage: #definition
* target[+] = Reference(StructureDefinition/adt-01-logical-model)
* recorded = "2023-08-29T17:26:36.0000Z"
* occurredDateTime = "2023-08-29"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "Created an ADT message resource"
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#CREATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "T. R. Johnson"
