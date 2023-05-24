package gov.hhs.cdc.trustedintermediary.etor.demographics

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class PatientDemographicsControllerTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(PatientDemographicsController, PatientDemographicsController.getInstance())
    }

    def "parseDemographics gets the Bundle and puts it as the underlying demographics"() {
        given:
        def mockBundle = new Bundle()

        def fhir = Mock(HapiFhir)
        fhir.parseResource(_ as String, _ as Class) >> mockBundle
        TestApplicationContext.register(HapiFhir, fhir)

        TestApplicationContext.injectRegisteredImplementations()

        def request = new DomainRequest()

        when:
        def patientDemographics = PatientDemographicsController.getInstance().parseDemographics(request)

        then:
        patientDemographics.getUnderlyingDemographics() == mockBundle
    }

    def "demographics constructResponse works"() {

        given:
        def mockBody = "DogCow goes Moof"

        def formatter = Mock(Jackson)
        formatter.convertToJsonString(_ as PatientDemographicsResponse) >> mockBody
        TestApplicationContext.register(Formatter, formatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = PatientDemographicsController.getInstance().constructResponse(new PatientDemographicsResponse("asdf-12341-jkl-7890", "blkjh-7685"))

        then:
        response.getBody() == mockBody
        response.getStatusCode() == 200
        response.getHeaders().get(PatientDemographicsController.CONTENT_TYPE_LITERAL) == PatientDemographicsController.APPLICATION_JSON_LITERAL
    }

    def "demographics constructResponse fails to make the JSON"() {

        given:
        def formatter = Mock(Jackson)
        formatter.convertToJsonString(_ as PatientDemographicsResponse) >> { throw new FormatterProcessingException("couldn't make the JSON", new Exception()) }
        TestApplicationContext.register(Formatter, formatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = PatientDemographicsController.getInstance().constructResponse(new PatientDemographicsResponse("asdf-12341-jkl-7890", "asdf1234"))

        then:
        response.statusCode == 500
    }

    def "failed constructResponse works"() {

        given:
        def mockBody = "DogCow goes Moof"
        def mockResponseStatus = 404

        def formatter = Mock(Jackson)
        formatter.convertToJsonString(_ as Map) >> mockBody
        TestApplicationContext.register(Formatter, formatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = PatientDemographicsController.getInstance().constructResponse(mockResponseStatus, new Exception("dogcow"))

        then:
        response.getBody() == mockBody
        response.getStatusCode() == mockResponseStatus
        response.getHeaders().get(PatientDemographicsController.CONTENT_TYPE_LITERAL) == PatientDemographicsController.APPLICATION_JSON_LITERAL
    }

    def "failed constructResponse fails to make the JSON"() {

        given:
        def formatter = Mock(Jackson)
        formatter.convertToJsonString(_ as Map) >> { throw new FormatterProcessingException("couldn't make the JSON", new Exception()) }
        TestApplicationContext.register(Formatter, formatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = PatientDemographicsController.getInstance().constructResponse(404, new Exception("dogcow"))

        then:
        response.statusCode == 500
    }

    def "failed constructResponse uses a different error message if there isn't a message in the Exception"() {

        given:
        def mockBody = "DogCow goes Moof"
        def mockResponseStatus = 404
        def mockException = new NullPointerException()

        def formatter = Mock(Jackson)
        1 * formatter.convertToJsonString(_ as Map) >> { Map error ->
            assert error.get("error") == mockException.getClass().toString()
            return mockBody
        }
        TestApplicationContext.register(Formatter, formatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = PatientDemographicsController.getInstance().constructResponse(mockResponseStatus, mockException)

        then:
        response.getBody() == mockBody
        response.getStatusCode() == mockResponseStatus
        response.getHeaders().get(PatientDemographicsController.CONTENT_TYPE_LITERAL) == PatientDemographicsController.APPLICATION_JSON_LITERAL
    }
}
