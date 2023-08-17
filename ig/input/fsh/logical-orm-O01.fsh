// defines the OORM-O01 (loosely) input that is expected for newborn screening

Logical: ORMO01
Id: orm-O01-logical-model
Title: "ORM-O01"
Description: "The expected input from a hospital for the newborn screening ETOR workflow"
* MSH 1..1 SU ORM-MSHSegment "ORM-MSH segment"
* PID 1..1 SU ORM-PIDSegment "ORM-PID segment"



Logical: ORM-MSHSegment
Id: segment-orm-msh-logical-model
Title: "ORM-MSH Segment"
Description: "The ORM-MSH Segment"
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

Logical: ORM-PIDSegment
Id: segment-orm-pid-logical-model
Title: "ORM-PID Segment"
Description: "The ORM-PID Segment"
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

//Logical: ORM-ORCSegment
//Id: segment-orm-orc-logical-model
//Title: "ORM-ORC Segment"
//Description: "The ORM-ORC Segment"

// there are other optional fields we might want to define
