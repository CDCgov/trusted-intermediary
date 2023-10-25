Logical: MSHSegment
Id: segment-msh-logical-model
Title: "MSH Segment"
Description: "The MSH Segment"
* segmentType 1..1 string "MSH"
* fieldSeparator 1..1 string "MSH-1 The Field Separator for the Message"
* encodingCharacters 1..1 string "MSH-2 Encoding Characters"
* sendingApplication 1..1 code "MSH-3 The Sending Application"
* sendingApplicationNamespaceId 1..1 code "MSH-3.1 Sending Application Namespace ID"
* sendingApplicationUniversalId 1..1 string "MSH-3.2 Sending Application Universal ID"
* sendingApplicationUniversalIdType 1..1 id "MSH-3.3 Sending Application Universal ID Type"
* sendingFacility 1..1 code "MSH-4 The Sending Facility"
* sendingFacilityNamespaceId 1..1 code "MSH-4.1 The Sending Facility Namespace ID"
* sendingFacilityUniversalId 1..1 string "MSH-4.2 The Sending Facility Universal ID"
* sendingFacilityUniversalIdType 1..1 id "MSH-4.3 The Sending Facility Universal ID Type"
* receivingApplication 1..1 code "MSH-5 The Receiving Application"
* receivingApplicationNamespaceId 1..1 code "MSH-5.1 The Receiving Application Namespace ID"
* receivingApplicationUniversalId 1..1 string "MSH-5.2 The Receiving Application Universal ID Type"
* receivingApplicationUniversalIdType 1..1 id "MSH-5.3 The Receiving Application Universal ID Type"
* receivingFacility 1..1 code "MSH-6 The Receiving facility"
* receivingFacilityNamespaceId 1..1 code "MSH-6.1 Receiving Facility Namespace ID"
* receivingFacilityUniversalId 1..1 string "MSH-6.2 Receiving Facility Universal ID"
* receivingFacilityUniversalIdType 1..1 id "MSH-6.3 Receiving Facility Universal ID Type"
* messageDateTime 1..1 dateTime "MSH-7 The Date and Time the Message was Created"
* messageType 1..1 Coding "MSH-9 The Type Of Message"
* messageTypeMessageCode 1..1 id "MSH-9.1 Message Code"
* messageTypeTriggerEvent 1..1 id "MSH-9.2 Trigger Event"
* messageTypeMessageStructure 1..1 id "MSH-9.3 Message Structure"
* messageControlId 1..1 string "MSH-10 A Control ID for the Message"
* processingId 1..1 string "MSH-11 The Processing ID"
* versionId 1..1 string "MSH-12 The HL7 Version of this Message"
* acceptAcknowledgmentType 1..1 id "MSH-15 Accept Acknowledgment Type"
* applicationAcknowledgementType 1..1 id "MSH-16 Application Acknowledgement Type"
* messageProfileIdentifier 1..2 Identifier "MSH-21 Message Profile Identifier"
* messageProfileIdentifierEntityIdentifier 1..1 string "MSH-21.1 Message Profile Identifier Entity Identifier"
* messageProfileIdentifierUniversalId 1..1 string "MSH-21.3 Message Profile Identifier Universal ID"
* messageProfileIdentifierUniversalIdType 1..1 id "MSH-21.4 Message Profile Identifier Universal ID Type"



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
