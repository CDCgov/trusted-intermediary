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
* orderControl 1..1 SU string "order control"
* ORCPlacerOrderNumber 1..1 SU string "placer order number"
* fillerOrderNumber 1..1 SU string "filler order number"
* placerGroupNumber 1..1 SU string "placer group number"
* dateTimeOfTransaction 1..1 SU string "date/time of transaction"
* orderingProvider 1..1 SU string " ordering provider"
* orderingFacilityName 1..1 SU string "ordering facility name"
// other optional fields

Logical: OBRSegment
Id: segment-obr-logical-model
Title: "OBR Segment"
Description: "The OBR Segment"
* setOBRId 1..1 SU string "OBR id"
* OBRplacerOrderNumber 1..1 SU string "placer order number"
* fillerOrderNumber 1..1 SU string "filler order number"
* universalServiceIdentifier 1..1 SU string " universal service identifier"
* observationDateTime 1..1 SU string "observation date/time"
* observationEndDateTime 1..1 SU string "observation end time"
* specimenID 1..1 SU string "specimen id"
* specimenType 1..1 SU string "specimen type"
* specimenCollectionDateTime 1..1 SU string "specimen collection date/time"
// other optional fields

Logical: OBXSegment
Id: segment-obx-logical-model
Title: "OBX Segment"
Description: "The OBX Segment"
* setOBXId 1..1 SU string "an indentifier"
* valueType 1..1 SU string "a value type"
* observationIdentifier 1..1 SU string "observation identifier"
* observationSubID 1..1 SU string "observation subID"
* observationValue 1..1 SU string "observation value"
* units 1..1 SU string "unit for observation value"
* observationResultStatus 1..1 SU string "observation result status"
* dateTimeOfTheObservation 1..1 SU string "date/time of observation"
* observationType 1..1 SU string "observation type"
* observationSubType 1..1 SU string "observation sub-type"
// other optional fields
