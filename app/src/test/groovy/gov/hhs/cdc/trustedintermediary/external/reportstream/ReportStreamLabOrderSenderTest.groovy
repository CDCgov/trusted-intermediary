package gov.hhs.cdc.trustedintermediary.external.reportstream

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.external.slf4j.Slf4jLogger
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import spock.lang.Specification

class ReportStreamLabOrderSenderTest extends Specification{

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(LabOrderSender, ReportStreamLabOrderSender.getInstance())
    }

    def "sendRequestBody works"() {
        given:
        def mockClient = Mock(HttpClient)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamLabOrderSender.getInstance().sendRequestBody("message_1", "fake token")
        ReportStreamLabOrderSender.getInstance().sendRequestBody("message_2", "fake token")

        then:
        2 * mockClient.post(_ as String, _ as Map<String,String>, _ as String) >> "200"
    }

    def "requestToken works"() {
        given:
        def expected = "rs fake token"
        def mockAuthEngine = Mock(AuthEngine)
        def mockClient = Mock(HttpClient)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(Logger, Slf4jLogger.getLogger())
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
        when:
        def actual = ReportStreamLabOrderSender.getInstance().requestToken()
        then:
        1 * mockAuthEngine.generateSenderToken(_ as String, _ as String, _ as String, _ as String, 300) >> "sender fake token"
        1 * mockClient.post(_ as String, _ as Map<String,String>, _ as String) >> """{"access_token":"rs fake token", "token_type":"bearer"}"""
        actual == expected
    }

    def "extractToken works"() {
        given:
        TestApplicationContext.register(Logger,Slf4jLogger.getLogger())
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
        def reportStreamLabOrderSender = ReportStreamLabOrderSender.getInstance()
        def expected = "IaMAfaKEt0keNN"
        def responseBody = """{"foo":"foo value", "access_token":"IaMAfaKEt0keNN", "boo":"boo value"}"""
        when:
        def actual = reportStreamLabOrderSender.extractToken(responseBody)
        then:
        actual == expected
    }

    def "composeRequestBody works"() {
        given:
        def reportStreamLabOrderSender = ReportStreamLabOrderSender.getInstance()
        def expected = "scope=flexion.*.report" +
                "&grant_type=client_credentials" +
                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                "&client_assertion=rsFakeToken"
        when:
        def actual = reportStreamLabOrderSender.composeRequestBody("rsFakeToken")
        then:
        actual == expected
    }

    def "send order works"() {

        given:
        def expected = "rs fake token"
        def mockAuthEngine = Mock(AuthEngine)
        def mockClient = Mock(HttpClient)
        def mockFhir = Mock(HapiFhir)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(HapiFhir,mockFhir)
        TestApplicationContext.register(Logger, Slf4jLogger.getLogger())
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
        LabOrder<?> mockOrder = new LabOrder<String>() {

                    @Override
                    String getUnderlyingOrder() {
                        return "Mock order"
                    }
                }

        when:
        mockFhir.encodeResourceToJson(_ as String) >> "Mock order"
        ReportStreamLabOrderSender.getInstance().sendOrder(mockOrder)

        then:
        noExceptionThrown()
    }
}
