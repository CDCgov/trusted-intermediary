// defines the ORU-R01 (loosely) input that is expected for newborn screening

Logical: ORUR01
Id: oru-R01-logical-model
Title: "ORU-R01"
Description: "The expected input and output for ORU formatted data for the newborn screening ETOR workflow"
* MSH 1..1 MSHSegment "MSH segment"
* PID 1..1 PIDSegment "PID segment"
* NK1 1..1 NK1Segment "NK1 segment"
* ORC 1..* ORCSegment "ORC segment"
* OBR 1..* OBRSegment "OBR segment"
* SPM 1..1 SPMSegment "SPM segment"
* OBX 1..* OBXSegment "OBX segment"


// Provenance Section begins
Instance: oru-R01-logical-model-history-create
InstanceOf: Provenance
Title: "Initial creation of ORU-R01 message changelog"
Usage: #definition
* target[+] = Reference(StructureDefinition/oru-R01-logical-model)
* recorded = "2024-02-21T17:36:36.0000Z"
* occurredDateTime = "2024-02-21"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "Created an ORU resource"
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#CREATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "T. R. Johnson"
