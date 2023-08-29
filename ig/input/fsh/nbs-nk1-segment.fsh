Logical: NK1Segment
Id: segment-nk1-logical-model
Title: "NK1 Segment"
Description: "The NK1 Segment (HL7 Description)"
* setNK1Id 1..1 SU string "NK1-1: Set ID - NK1"
* name 1..1 SU string "NK1-2 Name"
* familyName 1..1 SU string "NK1-2.1 Family Name"
* surname 1..1 SU string "NK1-2.1.1 Surname"
* relationship 1..1 SU string "NK1-3 Relationship"
* relationshipIdentifier 1..1 SU string "NK1-3.1 idenitifier"
* relationshipCodingSystem 1..1 SU string "Name of Coding System"
* originalText 1..1 SU string "NK1-3.9 Original Text"
* streetAddress 1..1 SU string "NK1-4.1 Street Address"
* streetOrMailingAddress 1..1 SU string "NK1-4.1.1 Street or Mailing Address"
* city 1..1 SU string "NK1-4.3 City"
* stateOrProvince 1..1 SU string "NK1-4.4 State or Province"
* zipOrPostalCode 1..1 SU string "NK1-4.5 Zip or Postal Code"
* equipType 1..1 SU string "NK1-5.3 Telecommunication Equipment Type"
* cityCode 1..1 SU string "NK1-5.6 Area/City Code"
* localNumber 1..1 SU string "NK1-5.7 Local Number"
* contactIdentifier 1..1 SU string "NK1-7.1 Identifier"
* contactCodingSystem 1..1 SU string "NK1-7.3 Name of Coding System"


// Provenance Section begins
Instance: segment-nk1-logical-model
InstanceOf: Provenance
Title: "Initial creation of NK1 segment changelog"
Usage: #definition
* target[+] = Reference(Logical/segment-nk1-logical-model)
* recorded = "2023-08-29T17:49:36.0000Z"
* occurredDateTime = "2023-08-29"
* reason = Code(http://terminology.hl7.org/CodeSystem/v3-ActReason, "METAMGT")
* reason.text = "Created an NK1 segment resource"
* activity = Code(http://terminology.hl7.org/CodeSystem/v3-DataOperation, "CREATE")
* agent[0].type = Coding(http://terminology.hl7.org/CodeSystem/provenance-participant-type, "author")
* agent[0].who.display = "T. R. Johnson"

