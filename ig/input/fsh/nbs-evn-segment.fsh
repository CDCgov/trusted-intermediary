Logical: EVNSegment
Id: segment-evn-logical-model
Title: "EVN Segment"
Description: "The EVN Segment"
* segmentType 1..1 SU string "EVN"
* eventTypeCode 1..1 SU string "A01" // this is apparently not in the spec anymore but retained for backward compatibility
* eventDateTime 1..1 SU string "Date and time of the event"
// there are other optional fields we might want to define