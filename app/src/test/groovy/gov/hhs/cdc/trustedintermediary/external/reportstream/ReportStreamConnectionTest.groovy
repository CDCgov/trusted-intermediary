package gov.hhs.cdc.trustedintermediary.external.reportstream

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.external.slf4j.Slf4jLogger
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine
import gov.hhs.cdc.trustedintermediary.wrappers.ClientConnection
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import spock.lang.Specification

class ReportStreamConnectionTest extends Specification{

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(ClientConnection, ReportStreamConnection.getInstance())
    }

    def "sendRequestBody works"() {
        given:
        def mockClient = Mock(HttpClient)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        TestApplicationContext.getImplementation(ClientConnection).sendRequestBody("message_1", "fake token")
        TestApplicationContext.getImplementation(ClientConnection).sendRequestBody("message_2", "fake token")

        then:
        2 * mockClient.post(_ as String, _ as String, _ as String) >> "200"
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
        def actual = ReportStreamConnection.getInstance().requestToken()
        then:
        1 * mockAuthEngine.generateSenderToken(_ as String, _ as String, _ as String, _ as String, 300) >> "sender fake token"
        1 * mockClient.requestToken(_ as String, _ as String) >> """{"access_token":"rs fake token", "token_type":"bearer"}"""
        actual == expected
    }

    def "extractToken works"() {
        given:
        TestApplicationContext.register(Logger,Slf4jLogger.getLogger())
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
        def reportStreamConnection = ReportStreamConnection.getInstance()
        def expected = "IaMAfaKEt0keNN"
        def responseBody = """{"foo":"foo value", "access_token":"IaMAfaKEt0keNN", "boo":"boo value"}"""
        when:
        def actual = reportStreamConnection.extractToken(responseBody)
        then:
        actual == expected
    }

    def "composeRequestBody works"() {
        given:
        def reportStreamConnection = ReportStreamConnection.getInstance()
        def expected = "scope=flexion.*.report" +
                "&grant_type=client_credentials" +
                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                "&client_assertion=rsFakeToken"
        when:
        def actual = reportStreamConnection.composeRequestBody("rsFakeToken")
        then:
        actual == expected
    }
}
