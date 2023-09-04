Logical: PIDSegment
Id: segment-pid-logical-model
Title: "PID Segment"
Description: "The PID Segment"
* segmentType 1..1 SU string "PID"
* setId 1..1 SU positiveInt "PID-1 A Sequence Number"
* patientIdentifierList 1..1 SU string "PID-3 A List Of Patient Identifiers"
* patientIdentifierListIdNumber 1..1 SU string "PID-3.1 Patient Identifier Id Number"
* patientIdentifierListTypeCode 1..1 SU code "PID-3.5 Patient identifier Type Code"
* patientName 1..1 SU string "PID-5 Patient Name"
* patientNameFamilyName 1..1 SU string "PID-5.1 Patient Family Name"
* patientNameGivenName 1..1 SU string "PID-5.2 Patient Given Name"
* patientNameTypeCode 1..1 SU code "PID-5.7 Patient Name Type Code"
* mothersMaidenName 0..1 SU string "PID-6 Mother's Maiden Name"
* mothersMaidenNameFamilyName 0..1 SU string "PID-6.1 Mother's Maiden Family Name"
* mothersMaidenNameGivenName 0..1 SU string "PID-6.2 Mother's Maiden Given Name"
* mothersMaidenNameSecondName 0..1 SU string "PID-6.3 Mother's Maiden Second and Further Given Name"
* birthDateTime 1..1 SU dateTime "PID-7 Birth Date And Time"
* administrativeSex 1..1 SU string "PID-8 Administrative sex"
* patientAddress 0..1 SU string "PID-11 Patient Address"
* patientAddressStreet 1..1 SU string "PID-11.1 Patient Street Address"
* patientAddressStreetMailing 1..1 SU string "PID-11.1.1 Patient Street or Mailing Address"
* patientAddressCity 1..1 SU string "PID-11.3 Patient Address City"
* patientAddressPostalCode 1..1 SU string "PID-11.5 Patient Address ZIP or Postal Code"
* patientAddressCountry 0..1 SU code "PID-11.6 Patient Address Country"
* patientAddressType 0..1 SU code "PID-11.7 Patient Address Type"
* patientAddressCounty 0..1 SU string "PID-11.9 Patient Address County or Parish Code"
* phoneNumberHome 0..1 SU string "PID-13 Phone Number - Home"
* phoneNumberHomeType 0..1 SU code "PID-13.3 Phone Number Telecommunication Equipment Type"
* phoneNumberHomeAnyText 0..1 SU string "PID-13.9 Phone Number Any Text"
* multipleBirthIndicator 0..1 SU code "PID-24 Multple Birth Indicator"
* birthOrder 0..1 SU positiveInt "PID-25 Birth Order"
* patientDeathIndicator 0..1 SU code "PID-30 Patient Death Indicator"

// Provenance Section begins
Instance: segment-pid-logical-model-history-create
InstanceOf: Provenance
Title: "Initial creation of PID segment changelog"
Usage: #definition
* target[+] = Reference(StructureDefinition/segment-pid-logical-model)
* recorded = "2023-08-29T18:17:36.0000Z"
* occurredDateTime = "2023-08-29"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "Created an PID segment resource"
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#CREATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "T. R. Johnson"


Instance: segment-pid-logical-model-history-update
InstanceOf: Provenance
Title: "addition of PID segment fields and cardinality correction"
Usage: #definition
* target[+] = Reference(StructureDefinition/segment-pid-logical-model)
* recorded = "2023-09-04T18:17:36.0000Z"
* occurredDateTime = "2023-09-04"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "changed the sub-segments to the ones we initially support."
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#UPDATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "T. R. Johnson"
