package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

class DemographicsTest extends Specification {


    def newbornPatientFilePath = "src/test/resources/newborn_patient.json"
    def newbornPatientJsonFile = new File(newbornPatientFilePath)
    def newbornPatientJsonFileString = newbornPatientJsonFile.getText("UTF-8")



    def "a demographics response is returned from the ETOR demographics endpoint"() {
        given:
        def expected = """{"fhirResourceId":"Patient/infant-twin-1","patientId":"MRN7465737865"}"""

        when:
        def responseBody = Client.post("/v1/etor/demographics", newbornPatientJsonFileString)

        then:
        responseBody == expected
    }

    def "bad response given for poorly formatted JSON"() {

        when:
        def responseBody = Client.post("/v1/etor/demographics", newbornPatientJsonFileString.substring(1))
        //removed beginning '{' to make this JSON invalid

        then:
        responseBody == "Server Error"
    }
}
