package gov.hhs.cdc.trustedintermediary.e2e

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class DemographicsTest extends Specification {

    def demographicsClient = new EndpointClient('/v1/etor/demographics')
    def newbornPatientJsonFileString = Files.readString(Path.of('../examples/Other/003_NBS_patient.fhir'))

    def setup() {
        SentPayloadReader.delete()
    }

    def "a demographics response is returned from the ETOR demographics endpoint"() {
        given:
        def expectedFhirResourceId  = 'Bundle/bundle-with-patient'
        def expectedPatientId  = 'MRN7465737865'

        when:
        def response = demographicsClient.submit(newbornPatientJsonFileString, true)
        def parsedJsonBody = JsonParsing.parseContent(response)

        then:
        response.getCode() == 200
        parsedJsonBody.fhirResourceId == expectedFhirResourceId
        parsedJsonBody.patientId == expectedPatientId
    }

    def "payload file check"() {
        when:
        def response = demographicsClient.submit(newbornPatientJsonFileString, true)
        def parsedResponseBody = JsonParsing.parseContent(response)
        def sentPayload = SentPayloadReader.read()
        def parsedSentPayload = JsonParsing.parse(sentPayload)

        then:
        response.getCode() == 200
        parsedSentPayload.entry[0].resource.resourceType == 'MessageHeader'
        parsedSentPayload.entry[2].resource.resourceType == 'ServiceRequest'

        parsedSentPayload.entry[1].resource.resourceType == 'Patient'
        parsedSentPayload.entry[1].resource.id == 'infant-twin-1'

        parsedSentPayload.entry[1].resource.identifier[1].value == parsedResponseBody.patientId  //the second (index 1) identifier so happens to be the MRN
        parsedSentPayload.resourceType + '/' + parsedSentPayload.id == parsedResponseBody.fhirResourceId
    }

    def "return a 400 response when request has unexpected format"() {
        given:
        def invalidJsonRequest = newbornPatientJsonFileString.substring(1)

        when:
        def response = demographicsClient.submit(invalidJsonRequest, true)
        def parsedJsonBody = JsonParsing.parseContent(response)

        then:
        response.getCode() == 400
        !(parsedJsonBody.error as String).isEmpty()
    }

    def "return a 401 response when making an unauthenticated request"() {
        when:
        def response = demographicsClient.submit(newbornPatientJsonFileString, false)
        def parsedJsonBody = JsonParsing.parseContent(response)

        then:
        response.getCode() == 401
        !(parsedJsonBody.error as String).isEmpty()
    }
}
