// defines the OORM-O01 (loosely) input that is expected for newborn screening
//TODO: We need proper cardinality for ORM segment fields
Logical: ORMO01
Id: orm-O01-logical-model
Title: "ORM-O01"
Description: "The expected input for ORM formatted data for the newborn screening ETOR workflow"
* MSH 1..1 SU MSHSegment "MSH segment"
* PID 1..1 SU PIDSegment "PID segment"
* ORC 1..1 SU ORCSegment "ORC segment"
* OBR 0..* SU OBRSegment "OBR segment"
* OBX 0..* SU OBXSegment "OBX segment"

Logical: ORCSegment
Id: segment-orc-logical-model
Title: "ORC Segment"
Description: "The ORC Segment"
*orderControl 0..1 SU string "order control"
*placerOrderNumber 0..1 SU string "placer order number"
*fillerOrderNumber 0..1 SU string "filler order number"
*PlacerGroupNumber 0..1 SU string "placer group number"
*Date/TimeOfTransaction 0..1 SU string "date/time of transaction"
*orderingProvider 0..1 SU string " ordering provider"
*orderingFacilityName 0..1 SU string "ordering facility name"

Logical: OBRSegment
Id: segment-obr-logical-model
Title: "OBR Segment"
Description: "The OBR Segment"
*setID–OBR 0..1 SU string
*placerOrderNumber 0..1 SU string
*fillerOrderNumber 0..1 SU string
*universalServiceIdentifier 0..1 SU string
*observationDateTime 0..1 SU string
*observationEndDateTime 0..1 SU string
*specimenID 0..1 SU string
*specimenType 0..1 SU string
*specimenCollectionDateTime 0..1 SU string
*setID–OBR 0..1 SU string
*placerOrderNumber 0..1 SU string
*fillerOrderNumber 0..1 SU string
*universalServiceIdentifier 0..1 SU string
*observationDateTime 0..1 SU string
*observationEndDateTime 0..1 SU string

Logical: OBXSegment
Id: segment-obx-logical-model
Title: "OBX Segment"
Description: "The OBX Segment"
