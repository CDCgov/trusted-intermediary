package gov.hhs.cdc.trustedintermediary.external.reportstream

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrder
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender
import gov.hhs.cdc.trustedintermediary.etor.demographics.UnableToSendLabOrderException
import gov.hhs.cdc.trustedintermediary.external.in_memory.KeyCache
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine
import gov.hhs.cdc.trustedintermediary.wrappers.Cache
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClientException
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets
import spock.lang.Specification
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

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
        2 * mockClient.post(_ as String, _ as Map<String, String>, _ as String) >> "200"
    }

    def "sendRequestBody fails from an IOException from the client"() {
        given:
        def mockClient = Mock(HttpClient)
        mockClient.post(_ as String, _ as Map<String, String>, _ as String) >> { throw new IOException("oops") }
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamLabOrderSender.getInstance().sendRequestBody("message_1", "fake token")
        ReportStreamLabOrderSender.getInstance().sendRequestBody("message_2", "fake token")

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
        def actual = ReportStreamLabOrderSender.getInstance().requestToken()
        then:
        1 * mockAuthEngine.generateSenderToken(_ as String, _ as String, _ as String, _ as String, 300) >> "sender fake token"
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
        def actual = ReportStreamLabOrderSender.getInstance().extractToken(responseBody)

        then:
        actual == expected
    }

    def "extractToken fails from not getting a String in the access_token"() {
        given:
        def mockClient = Mock(HttpClient)
        def mockCache = Mock(Cache)
        TestApplicationContext.register(Formatter, Jackson.getInstance())
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(Secrets, Mock(Secrets))
        TestApplicationContext.register(AuthEngine, Mock(AuthEngine))
        TestApplicationContext.register(Cache, mockCache)
        TestApplicationContext.injectRegisteredImplementations()

        def responseBody = """{"foo":"foo value", "access_token":3, "boo":"boo value"}"""
        mockClient.post(_ as String, _ as Map, _ as String) >> responseBody

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
        def mockCache = Mock(Cache)
        mockClient.post(_ as String, _ as Map, _ as String) >> "something"
        mockFormatter.convertJsonToObject(_ as String, _ as Class) >> Map.of("access_token", "fake-token")
        TestApplicationContext.register(Secrets, mockSecrets)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(HapiFhir, mockFhir)
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.register(Cache, mockCache)
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

    def "cachedPrivateKey getter and setter works" () {
        given:
        def rsLabOrderSender = ReportStreamLabOrderSender.getInstance()
        def expected = "a fake azure key"

        when:
        rsLabOrderSender.setCachedPrivateKey(expected)
        def actual = rsLabOrderSender.getCachedPrivateKey()

        then:
        expected == actual
    }

    def "cachedPrivateKey thread synchronization" () {
        // TODO - Pending race-condition clarification
        given:
        def rsLabOrderSender = ReportStreamLabOrderSender.getInstance()
        def threadCount = 15
        def lock = new Object()
        def threads = (1..threadCount).collect { index ->
            new Thread( {
                synchronized (lock) {
                    rsLabOrderSender.setCachedPrivateKey("${index}")
                    def result = rsLabOrderSender.getCachedPrivateKey()
                    assert result =="${index}"
                }
            })
        }

        when:
        threads*.start()
        threads*.join()

        then:
        noExceptionThrown()
    }

    def "retrievePrivateKey works when cache is empty" () {
        given:
        def mockSecret = Mock(Secrets)
        def expected = "New Fake Azure Key"
        def keyCache = KeyCache.getInstance()
        def key = "report-stream-sender-private-key-local"
        mockSecret.getKey(_ as String) >> expected
        TestApplicationContext.register(Secrets, mockSecret)
        TestApplicationContext.register(Cache, keyCache)
        TestApplicationContext.injectRegisteredImplementations()
        def rsLabOrderSender = ReportStreamLabOrderSender.getInstance()
        when:
        def actual = rsLabOrderSender.retrievePrivateKey()

        then:
        actual == expected
        keyCache.get(key) == expected
    }

    def "retrievePrivateKey works when cache is not empty" () {
        given:
        def keyCache = KeyCache.getInstance()
        def key = "report-stream-sender-private-key-local"
        def expected = "existing fake azure key"
        TestApplicationContext.register(Cache, keyCache)
        TestApplicationContext.injectRegisteredImplementations()
        def rsLabOrderSender = ReportStreamLabOrderSender.getInstance()

        when:
        keyCache.put(key, expected)
        // rsLabOrderSender.setCachedPrivateKey(expected)
        def actual = rsLabOrderSender.retrievePrivateKey()

        then:
        expected == actual
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

    def "rsTokencache getter and setter works, no synchronization"() {
        given:
        def rsLabOrderSender = ReportStreamLabOrderSender.getInstance()
        def expected = "fake token"

        when:
        rsLabOrderSender.setRsTokenCache(expected)
        def actual = rsLabOrderSender.getRsTokenCache()

        then:
        actual == expected
    }

    // TODO cache getter and "setter" needs test for synchronization

    def "sendRequestBody bombs out due to http exception"() {
        given:
        def labOrderSender = ReportStreamLabOrderSender.getInstance()
        def mockClient = Mock(HttpClient)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(LabOrderSender, labOrderSender)
        TestApplicationContext.injectRegisteredImplementations()

        mockClient.post(_ as String, _ as Map<String,String>, _ as String) >> {
            throw new HttpClientException("404",new IOException())
        }

        when:
        labOrderSender.sendRequestBody("json", "bearerToken")

        then:
        def exception = thrown(UnableToSendLabOrderException)
        exception.getCause().getClass() == HttpClientException
    }

    def "getRsToken when cache is empty"() {
        given:
        def labOrderSender = ReportStreamLabOrderSender.getInstance()
        def mockClient = Mock(HttpClient)
        def mockAuthEngine = Mock(AuthEngine)
        def mockSecrets = Mock(Secrets)
        def mockFormatter = Mock(Formatter)
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(Secrets, mockSecrets)
        mockSecrets.getKey(_ as String) >> "fake private key"
        TestApplicationContext.register(LabOrderSender, labOrderSender)
        TestApplicationContext.injectRegisteredImplementations()

        mockAuthEngine.getExpirationDate(_ as String) >> LocalDateTime.now().plus(10, ChronoUnit.SECONDS)
        mockAuthEngine.generateSenderToken(_ as String, _ as String, _ as String, _ as String, 300) >> "fake token"
        mockFormatter.convertJsonToObject(_ as String, _ as Class) >> Map.of("access_token", "fake token")
        def responseBody = """{"foo":"foo value", "access_token":fake token, "boo":"boo value"}"""
        mockClient.post(_ as String, _ as Map, _ as String) >> responseBody

        when:
        def token = labOrderSender.getRsToken()

        then:
        token == labOrderSender.getRsTokenCache()
    }

    def "getRsToken when cache token is invalid"() {
        given:
        def labOrderSender = ReportStreamLabOrderSender.getInstance()
        def mockClient = Mock(HttpClient)
        def mockAuthEngine = Mock(AuthEngine)
        def mockSecrets = Mock(Secrets)
        def mockFormatter = Mock(Formatter)
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(Secrets, mockSecrets)
        mockSecrets.getKey(_ as String) >> "fakePrivateKey"
        TestApplicationContext.register(LabOrderSender, labOrderSender)
        TestApplicationContext.injectRegisteredImplementations()

        mockAuthEngine.generateSenderToken(_ as String, _ as String, _ as String, _ as String, 300) >> "fake token"
        mockAuthEngine.getExpirationDate(_ as String) >> LocalDateTime.now().plus(10, ChronoUnit.SECONDS)
        mockFormatter.convertJsonToObject(_ as String, _ as Class) >> Map.of("access_token", "fake token")
        def responseBody = """{"foo":"foo value", "access_token":fake token, "boo":"boo value"}"""
        mockClient.post(_ as String, _ as Map, _ as String) >> responseBody
        labOrderSender.setRsTokenCache("Invalid Token")

        when:
        def token = labOrderSender.getRsToken()

        then:
        token == labOrderSender.getRsTokenCache()
    }

    def "getRsToken when cache token is valid"() {
        given:
        def labOrderSender = ReportStreamLabOrderSender.getInstance()
        def mockClient = Mock(HttpClient)
        def mockAuthEngine = Mock(AuthEngine)
        def mockSecrets = Mock(Secrets)
        def mockFormatter = Mock(Formatter)
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(Secrets, mockSecrets)
        mockSecrets.getKey(_ as String) >> "fakePrivateKey"
        TestApplicationContext.register(LabOrderSender, labOrderSender)
        TestApplicationContext.injectRegisteredImplementations()

        mockAuthEngine.generateSenderToken(_ as String, _ as String, _ as String, _ as String, 300) >> "fake token"
        mockAuthEngine.getExpirationDate(_ as String) >> LocalDateTime.now().plus(25, ChronoUnit.SECONDS)
        mockFormatter.convertJsonToObject(_ as String, _ as Class) >> Map.of("access_token", "fake token")
        def responseBody = """{"foo":"foo value", "access_token":fake token, "boo":"boo value"}"""
        mockClient.post(_ as String, _ as Map, _ as String) >> responseBody
        labOrderSender.setRsTokenCache("valid Token")

        when:
        def token = labOrderSender.getRsToken()

        then:
        token == labOrderSender.getRsTokenCache()
    }
}
