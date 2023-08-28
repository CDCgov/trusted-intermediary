// defines the ADT-01 input that is expected for newborn screening

Logical: ADT01
Id: adt-01-logical-model
Title: "ADT-01"
Description: "The expected input for the demographic data for the newborn screening ETOR workflow"
* MSH 1..1 SU MSHSegment "MSH segment"
* EVN 1..1 SU EVNSegment "EVN segment"
* PID 1..1 SU PIDSegment "PID segment"
* messageString 0..1 SU string "the full message as delimeted text"





