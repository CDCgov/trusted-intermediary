package gov.hhs.cdc.trustedintermediary.e2e

import org.apache.hc.core5.http.io.entity.EntityUtils
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class DemographicsTest extends Specification {

    def demographicsClient = new EndpointClient("/v1/etor/demographics")
    def newbornPatientJsonFileString = Files.readString(Paths.get("src/test/resources/newborn_patient.json"))

    def "a demographics response is returned from the ETOR demographics endpoint"() {
        given:
        def expectedFhirResourceId  = "Bundle/bundle-with-patient"
        def expectedPatientId  = "MRN7465737865"

        when:
        def response = demographicsClient.submit(newbornPatientJsonFileString, true)
        def parsedJsonBody = JsonParsing.parseContent(response)

        then:
        response.getCode() == 200
        parsedJsonBody.fhirResourceId == expectedFhirResourceId
        parsedJsonBody.patientId == expectedPatientId
    }

    def "bad response given for poorly formatted JSON"() {
        given:
        def invalidJsonRequest = newbornPatientJsonFileString.substring(1)

        when:
        def response = demographicsClient.submit(invalidJsonRequest, true)
        def responseBody = EntityUtils.toString(response.getEntity())

        then:
        response.getCode() == 500
        responseBody == "Server Error"
    }

    def "payload file check"() {
        when:
        def response = demographicsClient.submit(newbornPatientJsonFileString, true)
        def parsedJsonBody = JsonParsing.parseContent(response)
        def sentPayload = SentPayloadReader.read()
        def parsedSentPayload = JsonParsing.parse(sentPayload)

        then:
        response.getCode() == 200
        parsedSentPayload.entry[0].resource.resourceType == "MessageHeader"
        parsedSentPayload.entry[2].resource.resourceType == "ServiceRequest"

        parsedSentPayload.entry[1].resource.resourceType == "Patient"
        parsedSentPayload.entry[1].resource.id == "infant-twin-1"

        parsedSentPayload.entry[1].resource.identifier[1].value == parsedJsonBody.patientId  //the second (index 1) identifier so happens to be the MRN
        parsedSentPayload.resourceType + "/" + parsedSentPayload.id == parsedJsonBody.fhirResourceId
    }

    def "a 401 comes from the ETOR demographics endpoint when unauthenticated"() {
        when:
        def response = demographicsClient.submit(newbornPatientJsonFileString, false)

        then:
        response.getCode() == 401
    }
}
