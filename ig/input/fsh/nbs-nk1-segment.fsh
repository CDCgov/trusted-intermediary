Logical: NK1Segment
Id: segment-nk1-logical-model
Title: "NK1 Segment"
Description: "The NK1 Segment (HL7 Description)"
* setNK1Id 1..1 string "NK1-1: Set ID - NK1"
* name 1..1 string "NK1-2 Name"
* familyName 1..1 string "NK1-2.1 Family Name"
* givenName 1..1 string "NK1-2.2 Given Name"
* furtherGivenNames 1..1 string "NK1-2.3 Second and Further Given Names or Initials Thereof"
* relationship 1..1 string "NK1-3 Relationship"
* relationshipIdentifier 1..1 string "NK1-3.1 Identifier"
* relationshipText 1..1 string "NK1-3.2 Text"
* relationshipCodingSystem 1..1 id "NK1-3.3 Name of Coding System"
* originalText 1..1 string "NK1-3.9 Original Text"
* phoneNumberXTN 1..1 string "NK1-5 Phone Number (XTN)"
* equipType 1..1 string "NK1-5.3 Telecommunication Equipment Type"
* countryCode 1..1 string "NK1-5.5 Country Code"
* cityCode 1..1 string "NK1-5.6 Area/City Code"
* localNumber 1..1 string "NK1-5.7 Local Number"
* unformattedTelephoneNumber 1..1 string "NK1-5.12 Unformatted Telephone Number"


// Provenance Section begins
Instance: segment-nk1-logical-model-history-create
InstanceOf: Provenance
Title: "Initial creation of NK1 segment changelog"
Usage: #definition
* target[+] = Reference(StructureDefinition/segment-nk1-logical-model)
* recorded = "2023-08-29T17:49:36.0000Z"
* occurredDateTime = "2023-08-29"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "Created an NK1 segment resource"
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#CREATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "T. R. Johnson"


Instance: segment-nk1-logical-model-history-update
InstanceOf: Provenance
Title: "Addition of converted NK1 segment fields and data type correction"
Usage: #definition
* target[+] = Reference(StructureDefinition/segment-nk1-logical-model)
* recorded = "2023-09-27T17:41:23.0000Z"
* occurredDateTime = "2023-09-27"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "Changed the sub-segments to the ones the system will initially support."
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#UPDATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "T. Johnson"