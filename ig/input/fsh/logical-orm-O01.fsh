// defines the ORM-O01 (loosely) input that is expected for newborn screening

//TODO: lines 11-12; Do we want to cross refernce the MSH and PID Segments fron the ADT message, or should they be custom to the ORM?
//TODO: Check for correct fields for each segment
//TODO: We need proper cardinality for ORM segment fields

Logical: ORMO01
Id: orm-O01-logical-model
Title: "ORM-O01"
Description: "The expected input for ORM formatted data for the newborn screening ETOR workflow"
* MSH 1..1 SU MSHSegment "MSH segment"
* PID 1..1 SU PIDSegment "PID segment"
* ORC 1..1 SU ORCSegment "ORC segment"
* OBR 1..* SU OBRSegment "OBR segment"
* OBX 1..* SU OBXSegment "OBX segment"

Logical: ORCSegment
Id: segment-orc-logical-model
Title: "ORC Segment"
Description: "The ORC Segment"
* orderControl 1..1 SU string "ORC-1 Order Control"
* ORCPlacerOrderNumber 1..1 SU string "ORC-2 Placer Order Number"
* fillerOrderNumber 1..1 SU string "ORC-3 Filler Order Number"
* placerGroupNumber 1..1 SU string "ORC-4 Placer Group Number"
* dateTimeOfTransaction 1..1 SU string "ORC-9 Date/Time of Transaction"
* orderingProvider 1..1 SU string "ORC-12 Ordering Provider"
* orderingFacilityName 1..1 SU string "ORC-11 prdering Facility Name"
// other optional fields

Logical: OBRSegment
Id: segment-obr-logical-model
Title: "OBR Segment"
Description: "The OBR Segment"
* setOBRId 1..1 SU string "OBR-1 Set ID"
* OBRplacerOrderNumber 1..1 SU string "OBR-2 Placer Order Number"
* fillerOrderNumber 1..1 SU string "OBR-3 Filler Order Number"
* universalServiceIdentifier 1..1 SU string "OBR-4 Universal Service Identifier"
* observationDateTime 1..1 SU string "OBR-7 Observation Date/Time"
* observationEndDateTime 1..1 SU string "OBR-8 Observation End Time"
* specimenID 1..1 SU string "OBR-15 or OBR-16  Specimen Id"
* specimenType 1..1 SU string "OBR-15 or OBR-16 Specimen Type"
* specimenCollectionDateTime 1..1 SU string "OBR-7 Specimen Collection Date/Time"
// other optional fields

Logical: OBXSegment
Id: segment-obx-logical-model
Title: "OBX Segment"
Description: "The OBX Segment"
* setOBXId 1..1 SU string "OBX-1 (1) Birthweight an indentifier"
* valueType 1..1 SU string "OBX-2 A Value Type"
* observationIdentifier 1..1 SU string "OBX-3 Observation Identifier"
* observationSubID 1..1 SU string "OBX-4 (LOINC 54089-8) Observation Sub-ID"
* observationValue 1..1 SU string "OBX-5 (LOINC or 99MDH) Observation Value"
* units 1..1 SU string "OBX-6 Unit For Observation Value"
* observationResultStatus 1..1 SU string "OBX-11 Observation Result Status"
* dateTimeOfTheObservation 1..1 SU string "OBX-14 Date/time Of Observation"
* observationType 1..1 SU string "OBX-29 Observation Type"
* observationSubType 1..1 SU string "OBX-30 Observation Sub-type"
// other optional fields
