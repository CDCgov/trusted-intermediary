// definition of the Patient resource we expect for CDC TI NBS
Profile: NBSPatient
Parent: $USCorePatient
Description: "The Patient resource containing demographic data for a newborn"
* birthDate 0..1
* extension contains $BirthTimeExt named _birthDate 1..1 MS


Instance: nbs-patient-example-01
InstanceOf: NBSPatient
Description: "An example NBS patient resource"
* name.given[0] = "Jaina"
* name.family = "Solo"
* identifier.use = #usual
* identifier.type = $IdType#MR "Medical Record Number"
* identifier.system = "http://hospital.example.org"
* identifier.value = "1234"
* gender = #female
* extension[_birthDate].valueDateTime = "2017-05-15T17:11:00+01:00"


// Provenance Section begins
Instance: nbs-patient-example-01
InstanceOf: Provenance
Title: "Initial creation of Patient resource changelog"
Usage: #definition
* target[+] = Reference(Profile/nbs-patient-example-01)
* recorded = "2023-08-29T18:14:36.0000Z"
* occurredDateTime = "2023-08-29"
* reason = Code(http://terminology.hl7.org/CodeSystem/v3-ActReason, "METAMGT")
* reason.text = "Created a patient resource"
* activity = Code(http://terminology.hl7.org/CodeSystem/v3-DataOperation, "CREATE")
* agent[0].type = Coding(http://terminology.hl7.org/CodeSystem/provenance-participant-type, "author")
* agent[0].who.display = "T. R. Johnson"
