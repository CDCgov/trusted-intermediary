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