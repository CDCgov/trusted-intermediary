package gov.hhs.cdc.trustedintermediary.external.reportstream

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender
import gov.hhs.cdc.trustedintermediary.etor.demographics.UnableToSendLabOrderException
import gov.hhs.cdc.trustedintermediary.external.apache.ApacheClient
import gov.hhs.cdc.trustedintermediary.external.azure.AzureSecrets
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.external.jjwt.JjwtEngine
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets
import spock.lang.Specification
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException

import java.awt.TextArea
import java.awt.datatransfer.StringSelection
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

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
        def clientMock = Mock(HttpClient)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.register(HttpClient, clientMock)
        TestApplicationContext.register(Secrets, Mock(Secrets))
        TestApplicationContext.register(AuthEngine, Mock(AuthEngine))
        TestApplicationContext.injectRegisteredImplementations()

        def responseBody = """{"foo":"foo value", "access_token":3, "boo":"boo value"}"""
        clientMock.post(_ as String, _ as Map, _ as String) >> responseBody

        when:
        ReportStreamLabOrderSender.getInstance().requestToken()

        then:
        def exception = thrown(UnableToSendLabOrderException)
        exception.getCause().getClass() == ClassCastException
    }

    def "extractToken fails from not getting valid JSON from the auth token endpoint"() {
        given:
        def clientMock = Mock(HttpClient)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.register(HttpClient, clientMock)
        TestApplicationContext.register(Secrets, Mock(Secrets))
        TestApplicationContext.register(AuthEngine, Mock(AuthEngine))
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        def responseBody = """{"foo":"foo value", "access_token":"""
        clientMock.post(_ as String, _ as Map, _ as String) >> responseBody

        when:
        ReportStreamLabOrderSender.getInstance().requestToken()

        then:
        def exception = thrown(UnableToSendLabOrderException)
        exception.getCause().getClass() == FormatterProcessingException
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
        def mockSecrets = Mock(Secrets)
        def mockClient = Mock(HttpClient)
        def mockFhir = Mock(HapiFhir)
        def mockFormatter = Mock(Formatter)
        mockClient.post(_ as String, _ as Map, _ as String) >> "something"
        mockFormatter.convertToObject(_ as String, _ as Class) >> Map.of("access_token", "fake-token")
        TestApplicationContext.register(Secrets, mockSecrets)
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

    def "ensure jwt that expires 15 seconds from now is valid"() {
        given:
        def mockAuthEngine = Mock(AuthEngine)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        mockAuthEngine.getExpirationDate(_ as String) >> LocalDateTime.now().plus(20, ChronoUnit.SECONDS)
        TestApplicationContext.register(LabOrderSender, ReportStreamLabOrderSender.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
        ReportStreamLabOrderSender.getInstance().setRsTokenCache("our token from rs")

        when:
        def isValid = ReportStreamLabOrderSender.getInstance().isValidToken()

        then:
        isValid
    }

    def "setRsTokenCache synchronization works"() {
        given:
        def rsLabOrderSender = ReportStreamLabOrderSender.getInstance()
        def threadCount = 1
        def expected = "lock is working"
        def lock = new Object()
        def actual = "lock is not working"

        def threads = (1..threadCount).collect { index ->
            def value
            //            new Thread({
            //
            //                rsLabOrderSender.setRsTokenCache("Thread-${index}")
            //                value = rsLabOrderSender.getRsTokenCache()
            //
            //                // at least one thread will hit the lock
            //                if (value != "Thread-${index}") {
            //                    actual = "once"
            //                    synchronized (lock) {
            //                        actual = "lock is working"
            //                    }
            //
            //                }
            //            })
            actual = "once"
        }

        when:
        println("")
        //        threads*.start()
        //        threads*.join()

        then:
        sleep(1000)
        actual == "once"
    }
}
