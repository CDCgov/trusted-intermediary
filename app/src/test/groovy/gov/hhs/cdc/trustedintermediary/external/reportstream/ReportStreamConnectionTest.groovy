package gov.hhs.cdc.trustedintermediary.external.reportstream

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine
import gov.hhs.cdc.trustedintermediary.wrappers.ClientConnection
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient
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
        TestApplicationContext.injectRegisteredImplementations()
        when:
        def actual = ReportStreamConnection.getInstance().requestToken()
        then:
        1 * mockAuthEngine.generateSenderToken(_ as String, _ as String, _ as String, _ as String, 300) >> "sender fake token"
        1 * mockClient.requestToken(_ as String, _ as String, _ as String) >> "rs fake token"
        actual == expected
    }
}
