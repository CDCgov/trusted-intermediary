// defines the ADT-01 input that is expected for newborn screening

Logical: ADT01
Id: adt-01-logical-model
Title: "ADT-01"
Description: "The expected input for the demographic data for the newborn screening ETOR workflow"
* MSH 1..1 SU MSHSegment "MSH segment"
* EVN 1..1 SU EVNSegment "EVN segment"
* PID 1..1 SU PIDSegment "PID segment"
* messageString 0..1 SU string "the full message as delimeted text"

Logical: MSHSegment
Id: segment-msh-logical-model
Title: "MSH Segment"
Description: "The MSH Segment"
* segmentType 1..1 SU string "MSH"
* delimeters 1..1 SU string "The delimeters for the message"
* sendingApplication 1..1 SU string "The sending application"
* sendingFacility 1..1 SU string "The sending facility"
* receivingApplication 1..1 SU string "The receiving application"
* receivingFacility 1..1 SU string "The receiving facility"
* messageDateTime 1..1 SU string "The date and time the message was created"
* security 0..1 SU string "indicates if security features should be followed"
* messageType 1..1 SU string "The type of message"
* messageControlId 1..1 SU string "a control id for the message"
* processingId 1..1 SU string "the processing id"
* versionId 1..1 SU string "the HL7 version of this message"
// there are other optional fields we might want to define

Logical: EVNSegment
Id: segment-evn-logical-model
Title: "EVN Segment"
Description: "The EVN Segment"
* segmentType 1..1 SU string "EVN"
* eventTypeCode 1..1 SU string "A01" // this is apparently not in the spec anymore but retained for backward compatibility
* eventDateTime 1..1 SU string "Date and time of the event"
// there are other optional fields we might want to define

Logical: PIDSegment
Id: segment-pid-logical-model
Title: "PID Segment"
Description: "The PID Segment"
* segmentType 1..1 SU string "PID"
* setId 1..1 SU string "a sequence number"
* patientId 0..1 SU string "a patient id (retained for backward compatibility, but should be blank"
* patientIdList 1..1 SU string "a list of patient identifiers"
* alternatePatientId 0..1 SU string "alternate patient id, should be blank"
* patientName 1..1 SU string "patient name"
* mothersMaidenName 0..1 SU string "mothers maiden name"
* birthDateTime 1..1 SU string "birth date and time"
* administrativeSex 1..1 SU string "administrative sex"
* patientAlias 0..1 SU string "should be blank"
* race 1..1 SU string "race/ethnicity"
* patientAddress 1..1 SU string "patient address"
* countyCode 0..1 SU string "retained for backward compatibility"
* homePhone 1..1 SU string "home phone number"
* businessPhone 0..1 SU string "business phone number"
* primaryLanguage 0..1 SU string "language"
* maritalStatus 1..1 SU string "marital status"
* religion 0..1 SU string "religion"
* patientAccountNumber 1..1 SU string "patient account number"
* ssn 0..1 SU string "retained for backward compatibility"
// other optional fields
