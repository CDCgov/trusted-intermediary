Logical: PIDSegment
Id: segment-pid-logical-model
Title: "PID Segment"
Description: "The PID Segment"
* segmentType 1..1 SU string "PID"
* setId 1..1 SU string "PID-1 a sequence number"
* patientIdList 1..1 SU string "PID-3 a list of patient identifiers"
* patientName 1..1 SU string "PID-5 patient name"
* mothersMaidenName 0..1 SU string "PID-6 mothers maiden name"
* birthDateTime 1..1 SU string "PID-7 birth date and time"
* administrativeSex 1..1 SU string "PID-8 administrative sex"
* patientAddress 0..1 SU string "PID-11 patient address"
* phoneNumberHome 0..1 SU string "PID-13 Phone Number - Home"
* patientDeathIndicator 0..1 SU string "PID-30 Patient Death Indicator"
