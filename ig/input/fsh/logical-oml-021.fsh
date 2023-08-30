// defines the OML-O01 (loosely) input that is expected for newborn screening

Logical: OML021
Id: oml-021-logical-model
Title: "OML-021"
Description: "The expected output for OML formatted data for the newborn screening ETOR workflow"
* MSH 1..1 SU MSHSegment "MSH segment"
* PID 1..1 SU PIDSegment "PID segment"
* NK1 1..1 SU NK1Segment "NK1 segment"
* ORC 1..* SU ORCSegment "ORC segment"
* OBR 1..* SU OBRSegment "OBR segment"
* SPM 1..1 SU SPMSegment "SPM segment"
* OBX 1..* SU OBXSegment "OBX segment"


// Provenance Section begins
Instance: oml-021-logical-model-history-create
InstanceOf: Provenance
Title: "Initial creation of OML-021 message changelog"
Usage: #definition
* target[+] = Reference(StructureDefinition/oml-021-logical-model)
* recorded = "2023-08-29T17:32:36.0000Z"
* occurredDateTime = "2023-08-29"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "Created an OML message resource"
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#CREATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "T. R. Johnson"
