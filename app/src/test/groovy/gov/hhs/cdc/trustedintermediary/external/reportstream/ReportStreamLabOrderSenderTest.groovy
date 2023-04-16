package gov.hhs.cdc.trustedintermediary.external.reportstream

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender
import gov.hhs.cdc.trustedintermediary.external.azure.AzureSecrets
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.external.localfile.LocalSecrets
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class ReportStreamLabOrderSenderTest extends Specification {

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

    def "sendRequestBody fails from an IOException from the client"() {
        given:
        def mockClient = Mock(HttpClient)
        mockClient.post(_ as String, _ as Map<String,String>, _ as String) >> { throw new IOException("oops") }
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamLabOrderSender.getInstance().sendRequestBody("message_1", "fake token")
        ReportStreamLabOrderSender.getInstance().sendRequestBody("message_2", "fake token")

        then:
        thrown(Exception)  //This test to be updated whenever the actual code's TODO is addressed for the exception handling
    }

    def "requestToken works"() {
        given:
        def expected = "rs fake token"
        def mockAuthEngine = Mock(AuthEngine)
        def mockClient = Mock(HttpClient)
        def mockSecrets = Mock(Secrets)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.register(Secrets, mockSecrets)
        TestApplicationContext.injectRegisteredImplementations()
        when:
        mockSecrets.getKey(_ as String) >> "Fake Azure Key"
        def actual = ReportStreamLabOrderSender.getInstance().requestToken()
        then:
        1 * mockAuthEngine.generateSenderToken(_ as String, _ as String, _ as String, _ as String, 300) >> "sender fake token"
        1 * mockClient.post(_ as String, _ as Map<String,String>, _ as String) >> """{"access_token":"${expected}", "token_type":"bearer"}"""
        actual == expected
    }

    def "extractToken works"() {
        given:
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        def expected = "IaMAfaKEt0keNN"
        def responseBody = """{"foo":"foo value", "access_token":"${expected}", "boo":"boo value"}"""

        when:
        def actual = ReportStreamLabOrderSender.getInstance().extractToken(responseBody)

        then:
        actual == expected
    }

    def "extractToken fails from not getting a String in the access_token"() {
        given:
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        def responseBody = """{"foo":"foo value", "access_token":3, "boo":"boo value"}"""

        when:
        ReportStreamLabOrderSender.getInstance().extractToken(responseBody)

        then:
        noExceptionThrown()  //This test to be updated whenever the actual code's TODO is addressed for the exception handling
    }

    def "extractToken fails from not getting valid JSON from the auth token endpoint"() {
        given:
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        def responseBody = """{"foo":"foo value", "access_token":"""

        when:
        ReportStreamLabOrderSender.getInstance().extractToken(responseBody)

        then:
        noExceptionThrown()  //This test to be updated whenever the actual code's TODO is addressed for the exception handling
    }

    def "composeRequestBody works"() {
        given:
        def reportStreamLabOrderSender = ReportStreamLabOrderSender.getInstance()
        def fakeToken = "rsFakeToken"
        def expected = "scope=flexion.*.report" +
                "&grant_type=client_credentials" +
                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                "&client_assertion=${fakeToken}"
        when:
        def actual = reportStreamLabOrderSender.composeRequestBody(fakeToken)
        then:
        actual == expected
    }

    def "send order works"() {

        given:
        def mockAuthEngine = Mock(AuthEngine)
        def mockClient = Mock(HttpClient)
        def mockFhir = Mock(HapiFhir)
        def mockFormatter = Mock(Formatter)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(HapiFhir, mockFhir)
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.injectRegisteredImplementations()
        mockFhir.encodeResourceToJson(_ as String) >> "Mock order"
        LabOrder<?> mockOrder = new LabOrder<String>() {

                    @Override
                    String getUnderlyingOrder() {
                        return "Mock order"
                    }
                }

        when:
        ReportStreamLabOrderSender.getInstance().sendOrder(mockOrder)

        then:
        noExceptionThrown()
    }

    def "retrieveAzureKey works when cache is empty" () {
        given:

        def mockSecret = Mock(Secrets)
        def expected = "New Fake Azure Key"
        mockSecret.getKey(_ as String) >> expected
        TestApplicationContext.register(Secrets, mockSecret)
        TestApplicationContext.injectRegisteredImplementations()
        def labOrderSender = ReportStreamLabOrderSender.getInstance()
        labOrderSender.azureKeyCache = null // TODO - azureKeyCache needs to be emptied from prior test
        when:
        def actual = labOrderSender.retrieveAzureKey("senderPrivateKey")

        then:
        expected == actual
        expected == labOrderSender.getAzureKeyCache()
    }

    def "retrieveAzureKey works when cache is not empty" () {
        given:
        def expected = "existing fake azure key"
        def labOrderSender = ReportStreamLabOrderSender.getInstance()

        when:
        labOrderSender.setAzureKeyCache(expected)
        def actual = labOrderSender.retrieveAzureKey("senderPrivateKey")

        then:
        expected == actual
    }
}
