Logical: MSHSegment
Id: segment-msh-logical-model
Title: "MSH Segment"
Description: "The MSH Segment"
* segmentType 1..1 SU string "MSH"
* delimeters 1..1 SU string "MSH-1 The delimeters for the message"
* sendingApplication 1..1 SU string "MSH-3 The sending application"
* sendingFacility 1..1 SU string "MSH-4 The sending facility"
* receivingApplication 1..1 SU string "MSH-5 The receiving application"
* receivingFacility 1..1 SU string "MSH-6 The receiving facility"
* messageDateTime 1..1 SU string "MSH-7 The date and time the message was created"
* security 0..1 SU string "indicates if security features should be followed"
* messageType 1..1 SU string "MSH-9 The type of message"
* messageControlId 1..1 SU string "MSH-10 a control id for the message"
* processingId 1..1 SU string "MSH-11 the processing id"
* versionId 1..1 SU string "MSH-12 the HL7 version of this message"
// there are other optional fields we might want to define


// Provenance Section begins
Instance: segment-msh-logical-model-history-create
InstanceOf: Provenance
Title: "Initial creation of MSH segment changelog"
Usage: #definition
* target[+] = Reference(StructureDefinition/segment-msh-logical-model)
* recorded = "2023-08-29T17:46:36.0000Z"
* occurredDateTime = "2023-08-29"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "Created an MSH segment resource"
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#CREATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "T. R. Johnson"
