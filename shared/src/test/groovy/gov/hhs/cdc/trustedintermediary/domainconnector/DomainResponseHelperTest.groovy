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
        def expectedResponseStatus = 200
        def expectedResponseBody = "DogCow goes Moof"

        def formatter = Mock(Jackson)
        formatter.convertToJsonString(_ as Object) >> expectedResponseBody
        TestApplicationContext.register(Formatter, formatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = DomainResponseHelper.getInstance().constructResponse(expectedResponseStatus, expectedResponseBody)

        then:
        actual.getBody() == expectedResponseBody
        actual.getStatusCode() == expectedResponseStatus
        actual.getHeaders().get(DomainResponseHelper.CONTENT_TYPE_LITERAL) == DomainResponseHelper.APPLICATION_JSON_LITERAL
    }

    def "constructOkResponse returns expected response"() {
        given:
        def expectedResponseStatus = 200
        def expectedResponseBody = "DogCow goes Moof"

        def formatter = Mock(Jackson)
        formatter.convertToJsonString(_ as Object) >> expectedResponseBody
        TestApplicationContext.register(Formatter, formatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = DomainResponseHelper.getInstance().constructOkResponse(expectedResponseBody)

        then:
        actual.getBody() == expectedResponseBody
        actual.getStatusCode() == expectedResponseStatus
        actual.getHeaders().get(DomainResponseHelper.CONTENT_TYPE_LITERAL) == DomainResponseHelper.APPLICATION_JSON_LITERAL
    }

    def "constructOkResponseFromString returns expected response"() {
        given:
        def expectedResponseStatus = 200
        def expectedResponseBody = "{json: DogCow goes Moof}"

        when:
        def actual = DomainResponseHelper.getInstance().constructOkResponseFromString(expectedResponseBody)

        then:
        actual.getBody() == expectedResponseBody
        actual.getStatusCode() == expectedResponseStatus
        actual.getHeaders().get(DomainResponseHelper.CONTENT_TYPE_LITERAL) == DomainResponseHelper.APPLICATION_JSON_LITERAL
    }

    def "constructErrorResponse with error message returns expected response"() {
        given:
        def expectedResponseStatus = 500
        def expectedResponseString = "This is an error"
        def expectedResponseBody = Map.of("error", expectedResponseString)
        def formatter = Mock(Jackson)
        formatter.convertToJsonString(_ as Map<String, String>) >> expectedResponseBody
        TestApplicationContext.register(Formatter, formatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = DomainResponseHelper.getInstance().constructErrorResponse(expectedResponseStatus, expectedResponseString)

        then:
        actual.getBody() == expectedResponseBody.toString()
        actual.getStatusCode() == expectedResponseStatus
        actual.getHeaders().get(DomainResponseHelper.CONTENT_TYPE_LITERAL) == DomainResponseHelper.APPLICATION_JSON_LITERAL
    }

    def "constructErrorResponse with exception returns expected response"() {
        given:
        def expectedBody = "DogCow goes Moof"
        def expectedResponseStatus = 404

        def formatter = Mock(Jackson)
        formatter.convertToJsonString(_ as Map) >> expectedBody
        TestApplicationContext.register(Formatter, formatter)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = DomainResponseHelper.getInstance().constructErrorResponse(expectedResponseStatus, new Exception("dogcow"))

        then:
        actual.getBody() == expectedBody
        actual.getStatusCode() == expectedResponseStatus
        actual.getHeaders().get(DomainResponseHelper.CONTENT_TYPE_LITERAL) == DomainResponseHelper.APPLICATION_JSON_LITERAL
    }

    def "constructGenericInternalServerErrorResponse returns expected response"() {
        given:
        def expectedResponseStatus = 500
        def expectedResponseBody = "An internal server error occurred"

        when:
        def actual = DomainResponseHelper.getInstance().constructGenericInternalServerErrorResponse()

        then:
        actual.getBody() == expectedResponseBody
        actual.getStatusCode() == expectedResponseStatus
    }

    def "constructResponse fails to make the JSON"() {

        given:
        def formatter = Mock(Jackson)
        formatter.convertToJsonString(_ as Object) >> { throw new FormatterProcessingException("couldn't make the JSON", new Exception()) }
        TestApplicationContext.register(Formatter, formatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = DomainResponseHelper.getInstance().constructResponse(200, "asdf1234")

        then:
        actual.statusCode == 500
    }

    def "failed constructErrorResponse uses a different error message if there isn't a message in the Exception"() {

        given:
        def expectedBody = "DogCow goes Moof"
        def expectedResponseStatus = 404
        def expectedException = new NullPointerException()

        def formatter = Mock(Jackson)
        1 * formatter.convertToJsonString(_ as Map) >> { Map error ->
            assert error.get("error") == expectedException.getClass().toString()
            return expectedBody
        }
        TestApplicationContext.register(Formatter, formatter)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actual = DomainResponseHelper.getInstance().constructErrorResponse(expectedResponseStatus, expectedException)

        then:
        actual.getBody() == expectedBody
        actual.getStatusCode() == expectedResponseStatus
        actual.getHeaders().get(DomainResponseHelper.CONTENT_TYPE_LITERAL) == DomainResponseHelper.APPLICATION_JSON_LITERAL
    }
}
