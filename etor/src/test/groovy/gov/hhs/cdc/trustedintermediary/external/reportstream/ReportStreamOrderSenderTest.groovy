package gov.hhs.cdc.trustedintermediary.external.reportstream

import gov.hhs.cdc.trustedintermediary.OrderMock
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderSender
import gov.hhs.cdc.trustedintermediary.etor.orders.UnableToSendOrderException
import gov.hhs.cdc.trustedintermediary.external.inmemory.KeyCache
import gov.hhs.cdc.trustedintermediary.external.jackson.Jackson
import gov.hhs.cdc.trustedintermediary.metadata.MetaDataStep
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine
import gov.hhs.cdc.trustedintermediary.wrappers.Cache
import gov.hhs.cdc.trustedintermediary.wrappers.MetricMetaData
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClientException
import gov.hhs.cdc.trustedintermediary.wrappers.Logger
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import spock.lang.Specification

class ReportStreamOrderSenderTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(OrderSender, ReportStreamOrderSender.getInstance())
        TestApplicationContext.register(MetricMetaData, Mock(MetricMetaData))
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

    def "requestToken saves our private key only after successful call to RS"() {
        given:
        def mockSecrets = Mock(Secrets)
        def mockCache = Mock(Cache)
        def mockFormatter = Mock(Formatter)

        def fakeOurPrivateKey = "DogCow" // pragma: allowlist secret
        mockSecrets.getKey(_ as String) >> fakeOurPrivateKey
        mockFormatter.convertJsonToObject(_ , _) >> [access_token: "Moof!"]

        TestApplicationContext.register(AuthEngine, Mock(AuthEngine))
        TestApplicationContext.register(HttpClient, Mock(HttpClient))
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.register(Secrets, mockSecrets)
        TestApplicationContext.register(Cache, mockCache)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamOrderSender.getInstance().requestToken()

        then:
        1 * mockCache.put(_ as String, fakeOurPrivateKey)
    }

    def "requestToken doesn't cache our private key if RS auth call fails"() {
        given:
        def mockClient = Mock(HttpClient)
        def mockCache = Mock(Cache)
        def mockFormatter = Mock(Formatter)

        mockClient.post(_, _, _) >> { throw new HttpClientException("Fake failure", new NullPointerException()) }

        mockFormatter.convertJsonToObject(_ , _) >> [access_token: "Moof!"]

        TestApplicationContext.register(AuthEngine, Mock(AuthEngine))
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.register(Secrets, Mock(Secrets))
        TestApplicationContext.register(Cache, mockCache)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamOrderSender.getInstance().requestToken()

        then:
        thrown(UnableToSendOrderException)
        0 * mockCache.put(_ , _)
    }

    def "cacheOurPrivateKeyIfNotCachedAlready doesn't cache when the key is already is cached"() {
        given:
        def mockCache = Mock(Cache)
        mockCache.get(_ as String) >> "DogCow private key"

        TestApplicationContext.register(Cache, mockCache)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamOrderSender.getInstance().cacheOurPrivateKeyIfNotCachedAlready("Moof!")

        then:
        0 * mockCache.put(_, _)
    }

    def "cacheOurPrivateKeyIfNotCachedAlready caches when the key isn't cached"() {
        given:
        def mockCache = Mock(Cache)
        mockCache.get(_ as String) >> null

        TestApplicationContext.register(Cache, mockCache)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamOrderSender.getInstance().cacheOurPrivateKeyIfNotCachedAlready("Moof!")

        then:
        1 * mockCache.put(_, _)
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
        def orderSender = ReportStreamOrderSender.getInstance()
        TestApplicationContext.register(OrderSender, orderSender)

        def mockAuthEngine = Mock(AuthEngine)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)

        def mockSecrets = Mock(Secrets)
        TestApplicationContext.register(Secrets, mockSecrets)

        def mockClient = Mock(HttpClient)
        mockClient.post(_ as String, _ as Map, _ as String) >> """{"submissionId": "fake-id", "key": "value"}"""
        TestApplicationContext.register(HttpClient, mockClient)

        def mockFhir = Mock(HapiFhir)
        mockFhir.encodeResourceToJson(_ as String) >> "Mock order"
        TestApplicationContext.register(HapiFhir, mockFhir)

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> Map.of("access_token", "fake-token")  >> Map.of("submissionId", "fake-id")
        TestApplicationContext.register(Formatter, mockFormatter)

        def mockCache = Mock(Cache)
        TestApplicationContext.register(Cache, mockCache)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamOrderSender.getInstance().sendOrder(new OrderMock(null, null, "Mock order"))

        then:
        noExceptionThrown()
    }
    def "log the step to metadata when send order is called"() {
        given:

        def mockAuthEngine = Mock(AuthEngine)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)

        def mockSecrets = Mock(Secrets)
        TestApplicationContext.register(Secrets, mockSecrets)

        def mockClient = Mock(HttpClient)
        mockClient.post(_ as String, _ as Map, _ as String) >> """{"submissionId": "fake-id", "key": "value"}"""
        TestApplicationContext.register(HttpClient, mockClient)

        def mockFhir = Mock(HapiFhir)
        mockFhir.encodeResourceToJson(_ as String) >> "Mock order"
        TestApplicationContext.register(HapiFhir, mockFhir)

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> Map.of("access_token", "fake-token")  >> Map.of("submissionId", "fake-id")
        TestApplicationContext.register(Formatter, mockFormatter)

        def mockCache = Mock(Cache)
        TestApplicationContext.register(Cache, mockCache)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamOrderSender.getInstance().sendOrder(new OrderMock(null, null, "Mock order"))

        then:
        1 * ReportStreamOrderSender.getInstance().metaData.put(_, _ as MetaDataStep)
    }

    def "retrievePrivateKey works when cache is empty" () {
        given:
        def mockSecret = Mock(Secrets)
        def expected = "New Fake Azure Key"
        mockSecret.getKey(_ as String) >> expected
        TestApplicationContext.register(Secrets, mockSecret)
        TestApplicationContext.register(Cache, KeyCache.getInstance())
        TestApplicationContext.injectRegisteredImplementations()
        def rsOrderSender = ReportStreamOrderSender.getInstance()
        when:
        def actual = rsOrderSender.retrievePrivateKey()

        then:
        actual == expected
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

        mockAuthEngine.getExpirationDate(_ as String) >> LocalDateTime.now().plus(20, ChronoUnit.SECONDS)

        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def isValid = ReportStreamOrderSender.getInstance().isValidToken("our token from rs")

        then:
        isValid
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

    def "getRsToken when cache is empty we call RS to get a new one"() {
        given:
        def mockClient = Mock(HttpClient)
        def mockFormatter = Mock(Formatter)
        def mockCache = Mock(Cache)

        //make the cache empty
        mockCache.get(_ as String) >> null

        def freshTokenFromRs = "new token"
        mockFormatter.convertJsonToObject(_, _ as TypeReference) >> [access_token: freshTokenFromRs]

        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.register(AuthEngine, Mock(AuthEngine))
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(Cache, mockCache)
        TestApplicationContext.register(Secrets, Mock(Secrets))

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def token = ReportStreamOrderSender.getInstance().getRsToken()

        then:
        1 * mockClient.post(_, _, _)
        token == freshTokenFromRs
    }

    def "getRsToken when cache token is invalid we call RS to get a new one"() {
        given:
        def mockClient = Mock(HttpClient)
        def mockAuthEngine = Mock(AuthEngine)
        def mockFormatter = Mock(Formatter)
        def mockCache = Mock(Cache)

        mockCache.get(_ as String) >> "shouldn't be returned"

        //mock the auth engine so that the JWT looks like it is invalid
        mockAuthEngine.getExpirationDate(_) >> LocalDateTime.now().plus(10, ChronoUnit.SECONDS)

        def freshTokenFromRs = "new token"
        mockFormatter.convertJsonToObject(_, _ as TypeReference) >> [access_token: freshTokenFromRs]

        TestApplicationContext.register(Formatter, mockFormatter)
        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        TestApplicationContext.register(HttpClient, mockClient)
        TestApplicationContext.register(Cache, mockCache)
        TestApplicationContext.register(Secrets, Mock(Secrets))

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def token = ReportStreamOrderSender.getInstance().getRsToken()

        then:
        1 * mockClient.post(_, _, _)
        token == freshTokenFromRs
    }

    def "getRsToken when cache token is valid, return that cached token"() {
        given:
        def mockAuthEngine = Mock(AuthEngine)
        def mockCache = Mock(Cache)

        def cachedRsToken = "DogCow goes Moof!"
        mockCache.get(_ as String) >> cachedRsToken

        //mock the auth engine so that the JWT looks valid
        mockAuthEngine.getExpirationDate(_) >> LocalDateTime.now().plus(60, ChronoUnit.SECONDS)

        TestApplicationContext.register(AuthEngine, mockAuthEngine)
        TestApplicationContext.register(Cache, mockCache)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        def token = ReportStreamOrderSender.getInstance().getRsToken()

        then:
        token == cachedRsToken
    }

    def "logRsSubmissionId logs submissionId if convertJsonToObject is successful"() {
        given:
        def mockSubmissionId = "fake-id"
        def mockResponseBody = """{"submissionId": "${mockSubmissionId}", "key": "value"}"""

        TestApplicationContext.register(Formatter, Jackson.getInstance())

        def mockLogger = Mock(Logger)
        TestApplicationContext.register(Logger, mockLogger)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamOrderSender.getInstance().logRsSubmissionId(mockResponseBody)

        then:
        1 * mockLogger.logInfo(_ as String, mockSubmissionId)
    }

    def "logRsSubmissionId logs error if convertJsonToObject fails"() {
        given:
        def mockResponseBody = '{"submissionId": "fake-id", "key": "value"}'
        def exception = new FormatterProcessingException("couldn't convert json", new Exception())

        def mockFormatter = Mock(Formatter)
        mockFormatter.convertJsonToObject(_ as String, _ as TypeReference) >> { throw exception }
        TestApplicationContext.register(Formatter, mockFormatter)

        def mockLogger = Mock(Logger)
        TestApplicationContext.register(Logger, mockLogger)

        TestApplicationContext.injectRegisteredImplementations()

        when:
        ReportStreamOrderSender.getInstance().logRsSubmissionId(mockResponseBody)

        then:
        1 * mockLogger.logError(_ as String, exception)
    }
}
