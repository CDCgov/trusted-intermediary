package gov.hhs.cdc.trustedintermediary.external.reportstream

import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.orders.Order
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderSender
import gov.hhs.cdc.trustedintermediary.etor.orders.UnableToSendOrderException
import gov.hhs.cdc.trustedintermediary.external.inmemory.KeyCache
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine
import gov.hhs.cdc.trustedintermediary.wrappers.Cache
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClientException
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import spock.lang.Specification

import java.util.concurrent.ConcurrentHashMap

class ReportStreamOrderSenderTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(OrderSender, ReportStreamOrderSender.getInstance())
    }

    def "sendRequestBody works"() {
        given:
        def mockClient = Mock(HttpClient)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamOrderSender.getInstance().sendRequestBody("message_1", "fake token")
        ReportStreamOrderSender.getInstance().sendRequestBody("message_2", "fake token")

        then:
        2 * mockClient.post(_ as String, _ as Map<String, String>, _ as String) >> "200"
    }

    def "sendRequestBody fails from an IOException from the client"() {
        given:
        def mockClient = Mock(HttpClient)
        mockClient.post(_ as String, _ as Map<String, String>, _ as String) >> { throw new IOException("oops") }
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamOrderSender.getInstance().sendRequestBody("message_1", "fake token")
        ReportStreamOrderSender.getInstance().sendRequestBody("message_2", "fake token")

        then:
        def exception = thrown(Exception)
        exception.getCause().getClass() == IOException
    }

    def "requestToken works"() {
        given:
        def expected = "rs fake token"
        def mockAuthEngine = Mock(AuthEngine)
        def mockClient = Mock(HttpClient)
        def mockSecrets = Mock(Secrets)
        def mockCache = Mock(Cache)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.register(Secrets, mockSecrets)
        TestApplicationContext.register(Cache, mockCache)
        TestApplicationContext.injectRegisteredImplementations()
        when:
        mockSecrets.getKey(_ as String) >> "Fake Azure Key"
        def actual = ReportStreamOrderSender.getInstance().requestToken()
        then:
        1 * mockAuthEngine.generateToken(_ as String, _ as String, _ as String, _ as String, 300, _ as String) >> "sender fake token"
        1 * mockClient.post(_ as String, _ as Map<String, String>, _ as String) >> """{"access_token":"${expected}", "token_type":"bearer"}"""
        actual == expected
    }

    def "extractToken works"() {
        given:
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.injectRegisteredImplementations()

        def expected = "IaMAfaKEt0keNN"
        def responseBody = """{"foo":"foo value", "access_token":"${expected}", "boo":"boo value"}"""

        when:
        def actual = ReportStreamOrderSender.getInstance().extractToken(responseBody)

        then:
        actual == expected
    }

    def "extractToken works when access_token is a number"() {
        given:
        def mockClient = Mock(HttpClient)
        def mockCache = Mock(Cache)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(Secrets, Mock(Secrets))
        TestApplicationContext.register(AuthEngine, Mock(AuthEngine))
        TestApplicationContext.register(Cache, mockCache)
        TestApplicationContext.injectRegisteredImplementations()
        def expectedTokenValue = "3"

        def responseBody = """{"foo":"foo value", "access_token": 3, "boo":"boo value"}"""
        mockClient.post(_ as String, _ as Map, _ as String) >> responseBody

        when:
        def actualTokenValue = ReportStreamOrderSender.getInstance().requestToken()

        then:
        actualTokenValue == expectedTokenValue
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
        ReportStreamOrderSender.getInstance().requestToken()

        then:
        def exception = thrown(UnableToSendOrderException)
        exception.getCause().getClass() == FormatterProcessingException
    }

    def "composeRequestBody works"() {
        given:
        def reportStreamOrderSender = ReportStreamOrderSender.getInstance()
        def fakeToken = "rsFakeToken"
        def expected = "scope=flexion.*.report" +
                "&grant_type=client_credentials" +
                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                "&client_assertion=${fakeToken}"
        when:
        def actual = reportStreamOrderSender.composeRequestBody(fakeToken)
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
        def mockCache = Mock(Cache)
        mockClient.post(_ as String, _ as Map, _ as String) >> "something"
        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> Map.of("access_token", "fake-token")
        TestApplicationContext.register(Secrets, mockSecrets)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(HapiFhir, mockFhir)
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.register(Cache, mockCache)
        TestApplicationContext.injectRegisteredImplementations()
        mockFhir.encodeResourceToJson(_ as String) >> "Mock order"
        Order<?> mockOrder = new OrderMock(null, null, "Mock order")

        when:
        ReportStreamOrderSender.getInstance().sendOrder(mockOrder)

        then:
        noExceptionThrown()
    }

    def "retrievePrivateKey works when cache is empty" () {
        given:
        def mockSecret = Mock(Secrets)
        def expected = "New Fake Azure Key"
        def keyCache = KeyCache.getInstance()
        def key = "trusted-intermediary-private-key-local"
        mockSecret.getKey(_ as String) >> expected
        TestApplicationContext.register(Secrets, mockSecret)
        TestApplicationContext.register(Cache, keyCache)
        TestApplicationContext.injectRegisteredImplementations()
        def rsOrderSender = ReportStreamOrderSender.getInstance()
        when:
        def actual = rsOrderSender.retrievePrivateKey()

        then:
        actual == expected
        keyCache.get(key) == expected
    }

    def "retrievePrivateKey works when cache is not empty" () {
        given:
        def keyCache = KeyCache.getInstance()
        def key = "trusted-intermediary-private-key-local"
        def expected = "existing fake azure key"
        TestApplicationContext.register(Cache, keyCache)
        TestApplicationContext.injectRegisteredImplementations()
        def rsOrderSender = ReportStreamOrderSender.getInstance()

        when:
        keyCache.put(key, expected)
        def actual = rsOrderSender.retrievePrivateKey()

        then:
        expected == actual
    }

    def "ensure jwt that expires 15 seconds from now is valid"() {
        given:
        def mockAuthEngine = Mock(AuthEngine)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        mockAuthEngine.getExpirationDate(_ as String) >> LocalDateTime.now().plus(20, ChronoUnit.SECONDS)
        TestApplicationContext.register(OrderSender, ReportStreamOrderSender.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
        ReportStreamOrderSender.getInstance().setRsTokenCache("our token from rs")

        when:
        def isValid = ReportStreamOrderSender.getInstance().isValidToken()

        then:
        isValid
    }

    def "rsTokenCache getter and setter works, no synchronization"() {
        given:
        def rsOrderSender = ReportStreamOrderSender.getInstance()
        def expected = "fake token"

        when:
        rsOrderSender.setRsTokenCache(expected)
        def actual = rsOrderSender.getRsTokenCache()

        then:
        actual == expected
    }

    def "rsTokenCache synchronization works"() {
        given:
        def orderSender = ReportStreamOrderSender.getInstance()
        def threadNums = 5
        def iterations = 25
        def table = new ConcurrentHashMap<String, Integer>()

        when:
        List<Thread> threads = []
        (1..threadNums).each { threadId ->
            threads.add(new Thread({
                for(int i=0; i<iterations; i++) {
                    orderSender.setRsTokenCache("${i}")
                    if (i == 24) {
                        table.put("thread"+"${threadId}", i)
                    }
                }
            }))
        }

        threads*.start()
        threads*.join()

        then:
        orderSender.getRsTokenCache() == "${iterations - 1}"
        table.size() == threadNums
        table.values().toSet().size() == 1
    }

    def "sendRequestBody bombs out due to http exception"() {
        given:
        def orderSender = ReportStreamOrderSender.getInstance()
        def mockClient = Mock(HttpClient)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(OrderSender, orderSender)
        TestApplicationContext.injectRegisteredImplementations()

        mockClient.post(_ as String, _ as Map<String,String>, _ as String) >> {
            throw new HttpClientException("404",new IOException())
        }

        when:
        orderSender.sendRequestBody("json", "bearerToken")

        then:
        def exception = thrown(UnableToSendOrderException)
        exception.getCause().getClass() == HttpClientException
    }

    def "getRsToken when cache is empty"() {
        given:
        def orderSender = ReportStreamOrderSender.getInstance()
        def mockClient = Mock(HttpClient)
        def mockAuthEngine = Mock(AuthEngine)
        def mockSecrets = Mock(Secrets)
        def mockFormatter = Mock(Formatter)
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(Secrets, mockSecrets)
        mockSecrets.getKey(_ as String) >> "fake private key"
        TestApplicationContext.register(OrderSender, orderSender)
        TestApplicationContext.injectRegisteredImplementations()

        mockAuthEngine.getExpirationDate(_ as String) >> LocalDateTime.now().plus(10, ChronoUnit.SECONDS)
        mockAuthEngine.generateSenderToken(_ as String, _ as String, _ as String, _ as String, 300) >> "fake token"
        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> Map.of("access_token", "fake token")
        def responseBody = """{"foo":"foo value", "access_token":fake token, "boo":"boo value"}"""
        mockClient.post(_ as String, _ as Map, _ as String) >> responseBody

        when:
        def token = orderSender.getRsToken()

        then:
        token == orderSender.getRsTokenCache()
    }

    def "getRsToken when cache token is invalid"() {
        given:
        def orderSender = ReportStreamOrderSender.getInstance()
        def mockClient = Mock(HttpClient)
        def mockAuthEngine = Mock(AuthEngine)
        def mockSecrets = Mock(Secrets)
        def mockFormatter = Mock(Formatter)
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(Secrets, mockSecrets)
        mockSecrets.getKey(_ as String) >> "fakePrivateKey"
        TestApplicationContext.register(OrderSender, orderSender)
        TestApplicationContext.injectRegisteredImplementations()

        mockAuthEngine.generateSenderToken(_ as String, _ as String, _ as String, _ as String, 300) >> "fake token"
        mockAuthEngine.getExpirationDate(_ as String) >> LocalDateTime.now().plus(10, ChronoUnit.SECONDS)
        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> Map.of("access_token", "fake token")
        def responseBody = """{"foo":"foo value", "access_token":fake token, "boo":"boo value"}"""
        mockClient.post(_ as String, _ as Map, _ as String) >> responseBody
        orderSender.setRsTokenCache("Invalid Token")

        when:
        def token = orderSender.getRsToken()

        then:
        token == orderSender.getRsTokenCache()
    }

    def "getRsToken when cache token is valid"() {
        given:
        def orderSender = ReportStreamOrderSender.getInstance()
        def mockClient = Mock(HttpClient)
        def mockAuthEngine = Mock(AuthEngine)
        def mockSecrets = Mock(Secrets)
        def mockFormatter = Mock(Formatter)
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(Secrets, mockSecrets)
        mockSecrets.getKey(_ as String) >> "fakePrivateKey"
        TestApplicationContext.register(OrderSender, orderSender)
        TestApplicationContext.injectRegisteredImplementations()

        mockAuthEngine.generateSenderToken(_ as String, _ as String, _ as String, _ as String, 300) >> "fake token"
        mockAuthEngine.getExpirationDate(_ as String) >> LocalDateTime.now().plus(25, ChronoUnit.SECONDS)
        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> Map.of("access_token", "fake token")
        def responseBody = """{"foo":"foo value", "access_token":fake token, "boo":"boo value"}"""
        mockClient.post(_ as String, _ as Map, _ as String) >> responseBody
        orderSender.setRsTokenCache("valid Token")

        when:
        def token = orderSender.getRsToken()

        then:
        token == orderSender.getRsTokenCache()
    }
}
