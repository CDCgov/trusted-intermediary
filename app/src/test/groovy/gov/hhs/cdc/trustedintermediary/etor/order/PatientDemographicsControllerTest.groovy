package gov.hhs.cdc.trustedintermediary.etor.order

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import spock.lang.Specification

class PatientDemographicsControllerTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(PatientDemographicsController, PatientDemographicsController.getInstance())
    }

    def "parseOrder works"() {
        given:
        def mockOrderId = "asdf-12341-jkl-7890"

        def formatter = Mock(Jackson)
        formatter.convertToObject(_ as String, _ as Class) >> new PatientDemographics(mockOrderId, "Massachusetts", "2022-12-21T08:34:27Z", "MassGeneral", "NBS panel for Clarus the DogCow")
        TestApplicationContext.register(Formatter, formatter)

        def request = new DomainRequest()

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def parsedOrder = PatientDemographicsController.getInstance().parseDemographics(request)

        then:
        noExceptionThrown()
        parsedOrder.getRequestId() == mockOrderId
    }

    def "parseOrder fails by the formatter"() {
        given:
        def formatter = Mock(Jackson)
        formatter.convertToObject(_ as String, _ as Class) >> { throw new FormatterProcessingException("unable to format or whatever", new Exception()) }
        TestApplicationContext.register(Formatter, formatter)

        def request = new DomainRequest()

        TestApplicationContext.injectRegisteredImplementations()

        when:
        PatientDemographicsController.getInstance().parseDemographics(request)

        then:
        thrown(RuntimeException)
    }

    def "constructResponse works"() {

        given:
        def mockBody = "DogCow goes Moof"

        def formatter = Mock(Jackson)
        formatter.convertToString(_ as PatientDemographicsResponse) >> mockBody
        TestApplicationContext.register(Formatter, formatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = PatientDemographicsController.getInstance().constructResponse(new PatientDemographicsResponse("asdf-12341-jkl-7890", "Massachusetts", "2022-12-21T08:34:27Z", "MassGeneral", "NBS panel for Clarus the DogCow"))

        then:
        response.getBody() == mockBody
        response.getStatusCode() == 200
        response.getHeaders().get(PatientDemographicsController.CONTENT_TYPE_LITERAL) == PatientDemographicsController.APPLICATION_JSON_LITERAL
    }

    def "parseOrder fails by the formatter"() {
        given:
        def formatter = Mock(Jackson)
        formatter.convertToObject(_ as String, _ as Class) >> { throw new FormatterProcessingException("unable to format or whatever", new Exception()) }
        TestApplicationContext.register(Formatter, formatter)

        def request = new DomainRequest()

        TestApplicationContext.injectRegisteredImplementations()

        when:
        PatientDemographicsController.getInstance().parseDemographics(request)

        then:
        thrown(RuntimeException)
    }

    def "constructResponse fails to make the JSON"() {

        given:
        def formatter = Mock(Jackson)
        formatter.convertToString(_ as PatientDemographicsResponse) >> { throw new FormatterProcessingException("couldn't make the JSON", new Exception()) }
        TestApplicationContext.register(Formatter, formatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        PatientDemographicsController.getInstance().constructResponse(new PatientDemographicsResponse("asdf-12341-jkl-7890", "Massachusetts", "2022-12-21T08:34:27Z", "MassGeneral", "NBS panel for Clarus the DogCow"))

        then:
        thrown(RuntimeException)
    }
}
