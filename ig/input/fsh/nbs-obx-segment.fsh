Logical: OBXSegment
Id: segment-obx-logical-model
Title: "OBX Segment"
Description: "The OBX Segment"
* setOBXId 1..1 SU string "OBX-1 (1) Birthweight an identifier"
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