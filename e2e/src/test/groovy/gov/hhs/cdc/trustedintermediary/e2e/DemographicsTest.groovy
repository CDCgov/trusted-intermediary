package gov.hhs.cdc.trustedintermediary.e2e


import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

class DemographicsTest extends Specification {

    def newbornPatientJsonFileString = new String(Files.readAllBytes(Paths.get("src/test/resources/newborn_patient.json")), StandardCharsets.UTF_8)

    def "a demographics response is returned from the ETOR demographics endpoint"() {
        given:
        def expectedFhirResourceId  = "Bundle/bundle-with-patient"
        def expectedPatientId  = "MRN7465737865"

        when:
        def responseBody = Client.post("/v1/etor/demographics", newbornPatientJsonFileString)
        def parsedJsonBody = JsonParsing.parse(responseBody, Map.class)

        then:
        parsedJsonBody.fhirResourceId == expectedFhirResourceId
        parsedJsonBody.patientId == expectedPatientId
    }

    def "bad response given for poorly formatted JSON"() {

        when:
        def responseBody = Client.post("/v1/etor/demographics", newbornPatientJsonFileString.substring(1))
        //removed beginning '{' to make this JSON invalid

        then:
        responseBody == "Server Error"
    }

    def "payload file check"() {

        when:
        def responseBody = Client.post("/v1/etor/demographics", newbornPatientJsonFileString)
        def sentPayload = SentPayloadReader.read()
        def parsedResponseBody = JsonParsing.parse(responseBody, Map.class)

        def parsedSentPayload = JsonParsing.parse(sentPayload, Map.class)

        then:

        parsedSentPayload.entry[0].resource.resourceType == "MessageHeader"
        parsedSentPayload.entry[2].resource.resourceType == "ServiceRequest"

        parsedSentPayload.entry[1].resource.resourceType == "Patient"
        parsedSentPayload.entry[1].resource.id == "infant-twin-1"

        parsedSentPayload.entry[1].resource.identifier[1].value == parsedResponseBody.patientId  //the second (index 1) identifier so happens to be the MRN
        parsedSentPayload.resourceType + "/" + parsedSentPayload.id == parsedResponseBody.fhirResourceId
    }
}
