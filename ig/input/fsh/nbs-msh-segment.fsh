Logical: MSHSegment
Id: segment-msh-logical-model
Title: "MSH Segment"
Description: "The MSH Segment"
* segmentType 1..1 SU string "MSH"
* delimeters 1..1 SU string "MSH-1 The delimeters for the message"
* delimeters 1..1 SU string "MSH-2 The delimeters for the message"  //TODO
* sendingApplication 1..1 SU string "MSH-3 The sending application"
* sendingFacility 1..1 SU string "MSH-4 The sending facility"
* receivingApplication 1..1 SU string "MSH-5 The receiving application"
* receivingFacility 1..1 SU string "MSH-6 The receiving facility"
* messageDateTime 1..1 SU string "MSH-7 The date and time the message was created"
* messageType 1..1 SU string "MSH-9 The type of message"
* messageControlId 1..1 SU string "MSH-10 a control id for the message"
* processingId 1..1 SU string "MSH-11 the processing id"
* versionId 1..1 SU string "MSH-12 the HL7 version of this message"
* versionId 1..1 SU string "MSH-15 the HL7 version of this message" //TODO
* versionId 1..1 SU string "MSH-16 the HL7 version of this message" //TODO
* versionId 1..1 SU string "MSH-21 the HL7 version of this message" //TODO
