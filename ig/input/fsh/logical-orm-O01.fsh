// defines the ORM-O01 (loosely) input that is expected for newborn screening

Logical: ORMO01
Id: orm-O01-logical-model
Title: "ORM-O01"
Description: "The expected input for ORM formatted data for the newborn screening ETOR workflow"
* MSH 1..1 MSHSegment "MSH segment"
* PID 1..1 PIDSegment "PID segment"
* ORC 1..* ORCSegment "ORC segment"
* OBR 1..* OBRSegment "OBR segment"
* OBX 1..* OBXSegment "OBX segment"


// Provenance Section begins
Instance: orm-O01-logical-model-history-create
InstanceOf: Provenance
Title: "Initial creation of ORM-O01 message changelog"
Usage: #definition
* target[+] = Reference(StructureDefinition/orm-O01-logical-model)
* recorded = "2023-08-29T17:36:36.0000Z"
* occurredDateTime = "2023-08-29"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "Created an ORM resource"
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#CREATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "T. R. Johnson"
