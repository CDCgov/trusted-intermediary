package gov.hhs.cdc.trustedintermediary.external.reportstream

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.ApacheClient
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
        mockClient.setToken(_ as String) >> mockClient
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        TestApplicationContext.getImplementation(ClientConnection).sendRequestBody("message_1")
        TestApplicationContext.getImplementation(ClientConnection).sendRequestBody("message_2")

        then:
        2 * mockClient.post(_ as String, _ as String)
    }

    def "requestToken works"() {
    }
}
