package gov.hhs.cdc.trustedintermediary.domainconnector

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException
import spock.lang.Specification


class DomainResponseHelperTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(DomainResponseHelper, DomainResponseHelper.getInstance())
    }

    def "constructResponse returns expected response"() {
        given:
        def mockResponseStatus = 200
        def mockResponseBody = "DogCow goes Moof"

        def formatter = Mock(Jackson)
        formatter.convertToJsonString(_ as Object) >> mockResponseBody
        TestApplicationContext.register(Formatter, formatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = DomainResponseHelper.getInstance().constructResponse(mockResponseStatus, mockResponseBody)

        then:
        response.getBody() == mockResponseBody
        response.getStatusCode() == mockResponseStatus
        response.getHeaders().get(DomainResponseHelper.CONTENT_TYPE_LITERAL) == DomainResponseHelper.APPLICATION_JSON_LITERAL
    }

    def "constructOkResponse returns expected response"() {
        given:
        def okResponseStatus = 200
        def mockResponseBody = "DogCow goes Moof"

        def formatter = Mock(Jackson)
        formatter.convertToJsonString(_ as Object) >> mockResponseBody
        TestApplicationContext.register(Formatter, formatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = DomainResponseHelper.getInstance().constructOkResponse(mockResponseBody)

        then:
        response.getBody() == mockResponseBody
        response.getStatusCode() == okResponseStatus
        response.getHeaders().get(DomainResponseHelper.CONTENT_TYPE_LITERAL) == DomainResponseHelper.APPLICATION_JSON_LITERAL
    }

    def "constructErrorResponse with error message returns expected response"() {
        given:
        def errorResponseStatus = 500
        def errorResponseString = "This is an error"
        def errorResponseBody = Map.of("error", errorResponseString)
        def formatter = Mock(Jackson)
        formatter.convertToJsonString(_ as Map<String, String>) >> errorResponseBody
        TestApplicationContext.register(Formatter, formatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = DomainResponseHelper.getInstance().constructErrorResponse(errorResponseStatus, errorResponseString)

        then:
        response.getBody() == errorResponseBody.toString()
        response.getStatusCode() == errorResponseStatus
        response.getHeaders().get(DomainResponseHelper.CONTENT_TYPE_LITERAL) == DomainResponseHelper.APPLICATION_JSON_LITERAL
    }

    def "constructErrorResponse with exception returns expected response"() {
        given:
        def mockBody = "DogCow goes Moof"
        def mockResponseStatus = 404

        def formatter = Mock(Jackson)
        formatter.convertToJsonString(_ as Map) >> mockBody
        TestApplicationContext.register(Formatter, formatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = DomainResponseHelper.getInstance().constructErrorResponse(mockResponseStatus, new Exception("dogcow"))

        then:
        response.getBody() == mockBody
        response.getStatusCode() == mockResponseStatus
        response.getHeaders().get(DomainResponseHelper.CONTENT_TYPE_LITERAL) == DomainResponseHelper.APPLICATION_JSON_LITERAL
    }

    def "constructGenericInternalServerErrorResponse returns expected response"() {
        given:
        def expectedResponseStatus = 500
        def expectedResponseBody = "An internal server error occurred"

        when:
        def response = DomainResponseHelper.getInstance().constructGenericInternalServerErrorResponse()

        then:
        response.getBody() == expectedResponseBody
        response.getStatusCode() == expectedResponseStatus
    }

    def "constructResponse fails to make the JSON"() {

        given:
        def formatter = Mock(Jackson)
        formatter.convertToJsonString(_ as Object) >> { throw new FormatterProcessingException("couldn't make the JSON", new Exception()) }
        TestApplicationContext.register(Formatter, formatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def response = DomainResponseHelper.getInstance().constructResponse(200, "asdf1234")

        then:
        response.statusCode == 500
    }

    def "failed constructErrorResponse uses a different error message if there isn't a message in the Exception"() {

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
        def response = DomainResponseHelper.getInstance().constructErrorResponse(mockResponseStatus, mockException)

        then:
        response.getBody() == mockBody
        response.getStatusCode() == mockResponseStatus
        response.getHeaders().get(DomainResponseHelper.CONTENT_TYPE_LITERAL) == DomainResponseHelper.APPLICATION_JSON_LITERAL
    }
}
