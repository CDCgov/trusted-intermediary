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
