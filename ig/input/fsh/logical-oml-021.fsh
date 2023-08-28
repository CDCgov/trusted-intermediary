// defines the OML-O01 (loosely) input that is expected for newborn screening

//TODO: The MSH, PID, ORC, OBR, OBX segments are cross referenced from other logicals
//TODO: Check for correct fields for each segment
//TODO: We need proper cardinality for ORM segment fields

Logical: OML021
Id: oml-021-logical-model
Title: "OML-021"
Description: "The expected output for OML formatted data for the newborn screening ETOR workflow"
* MSH 1..1 SU MSHSegment "MSH segment"
* PID 1..1 SU PIDSegment "PID segment"
* NK1 1..1 SU NK1Segment "NK1 segment"
* ORC 1..* SU ORCSegment "ORC segment"
* OBR 1..* SU OBRSegment "OBR segment"
* SPM 1..1 SU SPMSegment "SPM segment"
* OBX 1..* SU OBXSegment "OBX segment"



