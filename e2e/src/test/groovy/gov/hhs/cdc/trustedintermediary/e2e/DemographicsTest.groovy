package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

class DemographicsTest extends Specification {

    def newbornPatientInBundle = """{
        "resourceType": "Bundle",
        "id": "bundle-with-patient",
        "type": "collection",
        "entry": [
            {
                "resource": {
                    "resourceType": "Patient",
                    "id": "infant-twin-1",
                    "text": {
                        "status": "generated",
                        "div": "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p><b>Jaina Solo (OFFICIAL)</b> female, DoB: 2017-05-15 ( Medical record number: MRN7465737865)</p></div>"
                    },
                    "extension": [
                        {
                            "url": "http://hl7.org/fhir/StructureDefinition/patient-mothersMaidenName",
                            "valueString": "Organa"
                        }
                    ],
                    "identifier": [
                        {
                            "type": {
                                "coding": [
                                    {
                                        "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
                                        "code": "MR"
                                    }
                                ]
                            },
                            "system": "http://coruscanthealth.org/main-hospital/patient-identifier",
                            "value": "MRN7465737865"
                        },
                        {
                            "system": "http://new-republic.gov/galactic-citizen-identifier",
                            "value": "7465737865"
                        }
                    ],
                    "name": [
                        {
                            "use": "official",
                            "family": "Solo",
                            "given": [
                                "Jaina"
                            ]
                        }
                    ],
                    "gender": "female",
                    "birthDate": "2017-05-15",
                    "_birthDate": {
                        "extension": [
                            {
                                "url": "http://hl7.org/fhir/StructureDefinition/patient-birthTime",
                                "valueDateTime": "2017-05-15T17:11:00+01:00"
                            }
                        ]
                    },
                    "multipleBirthInteger": 1,
                    "contact": [
                        {
                            "relationship": [
                                {
                                    "coding": [
                                        {
                                            "system": "http://snomed.info/sct",
                                            "code": "72705000",
                                            "display": "Mother"
                                        },
                                        {
                                            "system": "http://terminology.hl7.org/CodeSystem/v2-0131",
                                            "code": "N"
                                        },
                                        {
                                            "system": "http://terminology.hl7.org/CodeSystem/v3-RoleCode",
                                            "code": "MTH"
                                        }
                                    ]
                                }
                            ],
                            "name": {
                                "use": "maiden",
                                "family": "Organa",
                                "given": [
                                    "Leia"
                                ]
                            },
                            "telecom": [
                                {
                                    "system": "phone",
                                    "value": "+31201234567",
                                    "use": "mobile"
                                }
                            ]
                        }
                    ]
                }
            }
        ]
    }"""

    def "a demographics response is returned from the ETOR demographics endpoint"() {
        given:
        def expected = """{"fhirResourceId":"Patient/infant-twin-1","patientId":"MRN7465737865"}"""

        when:
        def responseBody = Client.post("/v1/etor/demographics", newbornPatientInBundle)

        then:
        responseBody == expected
    }

    def "bad response given for poorly formatted JSON"() {

        when:
        def responseBody = Client.post("/v1/etor/demographics", newbornPatientInBundle.substring(1))
        //removed beginning '{' to make this JSON invalid

        then:
        responseBody == "Server Error"
    }
}
