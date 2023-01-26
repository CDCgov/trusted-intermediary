package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

class DemographicsTest extends Specification {

    def "a demographics response is returned from the ETOR demographics endpoint"() {
        given:
        def expected = """{"id":"Patient/infant-twin-1","firstName":"Jaina","lastName":"Solo","sex":"female","birthDateTime":null,"birthOrder":1}"""

        when:
        def responseBody = Client.post("/v1/etor/demographics","""{
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
              "system": "http://coruscanthealth.org/main-hospital/patient-identifier"
            },
            {
              "system": "http://new-republic.gov/galactic-citizen-identifier"
            },
            {
              "system": "http://new-republic.gov/galactic-citizen-identifier",
              "value": "test2"
            },
            {
              "system": "http://new-republic.gov/galactic-citizen-identifier",
              "value": "test3"
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
        }""")

        then:
        responseBody == expected
    }

    def "bad response given for poorly formatted JSON"() {

        when:
        def responseBody = Client.post("/v1/etor/demographics","""{
                "id": "an ID",
                "destination": "Massachusetts",
                "createdAt": "2022-12-21T08:34:27Z",
                "client": "MassGeneral"
            """)
        //notice the missing end } above

        then:
        responseBody == "Server Error"
    }
}
