package gov.hhs.cdc.trustedintermediary.auth

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.external.inmemory.KeyCache
import gov.hhs.cdc.trustedintermediary.external.jjwt.JjwtEngine
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine
import gov.hhs.cdc.trustedintermediary.wrappers.Cache
import gov.hhs.cdc.trustedintermediary.wrappers.InvalidTokenException
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets
import spock.lang.Specification

class AuthRequestValidatorTest extends Specification{

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(AuthRequestValidator, AuthRequestValidator.getInstance())
    }

    def "tokenHasContent unhappy empty path works"() {
        given:
        def emptyToken = ""
        def expected = false
        def validator = AuthRequestValidator.getInstance()

        when:
        def actual = validator.tokenHasContent(emptyToken)

        then:
        actual == expected
    }

    def "tokenHasContent  unhappy null path works"() {
        given:
        def nullToken = null
        def expected = false
        def validator = AuthRequestValidator.getInstance()

        when:
        def actual = validator.tokenHasContent(nullToken)

        then:
        actual == expected
    }

    def "tokenHasContent happy path works"() {
        given:
        def Token = "I'm not empty"
        def expected = true
        def validator = AuthRequestValidator.getInstance()

        when:
        def actual = validator.tokenHasContent(Token)

        then:
        actual == expected
    }

    def "extractToken happy path works"() {
        given:
        def token = "fake-token-here"
        def header = Map.of("Authorization", "Bearer " + token)
        def expected = token
        def request = new DomainRequest()
        def validator = AuthRequestValidator.getInstance()
        TestApplicationContext.injectRegisteredImplementations()

        when:
        request.setHeaders(header)
        def actual = validator.extractToken(request)

        then:
        actual == expected
    }

    def "extractToken unhappy path works"() {
        given:
        def header = Map.of("key", "value")
        def expected = ""
        def request = new DomainRequest()
        def validator = AuthRequestValidator.getInstance()

        when:
        request.setHeaders(header)
        def actual = validator.extractToken(request)

        then:
        actual == expected
    }

    def "retrievePublicKey works when keyCache not empty"() {
        given:
        def mockCache = Mock(KeyCache)
        def key = "fake key"
        def expected = key
        def validator = AuthRequestValidator.getInstance()
        TestApplicationContext.register(Cache, mockCache)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        mockCache.get(_ as String) >> key
        def actual = validator.retrievePublicKey()

        then:
        actual == expected
    }

    def "retrievePublicKey works when keyCache is empty"() {
        given:
        def mockCache = Mock(KeyCache)
        def mockSecrets = Mock(Secrets)
        def key = "fake key"
        def expected = key
        def validator = AuthRequestValidator.getInstance()
        TestApplicationContext.register(Cache, mockCache)
        TestApplicationContext.register(Secrets, mockSecrets)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        mockCache.get(_ as String) >> null
        mockSecrets.getKey(_ as String) >> key
        def actual = validator.retrievePublicKey()

        then:
        actual == expected
    }

    def "retrievePublicKey adds key to keyCache works"() {
        given:
        def cache = KeyCache.getInstance()
        def mockSecrets = Mock(Secrets)
        def key = "fake key"
        def expected = key
        def validator = AuthRequestValidator.getInstance()
        TestApplicationContext.register(Cache, cache)
        TestApplicationContext.register(Secrets, mockSecrets)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        mockSecrets.getKey(_ as String) >> key
        validator.retrievePublicKey()
        def actual = cache.get("trusted-intermediary-public-key-local")

        then:
        actual == expected
    }

    def "isValidAuthenticatedRequest happy path works"() {
        given:
        def validator = AuthRequestValidator.getInstance()
        def token = "fake-token-here"
        def header = Map.of("Authorization", "Bearer " + token)
        def mockEngine = Mock(JjwtEngine)
        def mockCache = Mock(KeyCache)
        def request = new DomainRequest()
        def expected = true
        TestApplicationContext.register(Cache, mockCache)
        TestApplicationContext.register(AuthEngine, mockEngine)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        request.setHeaders(header)
        mockCache.get(_ as String) >> {"my-fake-private-key"}
        mockEngine.validateToken(_ as String, _ as String)
        def actual = validator.isValidAuthenticatedRequest(request)

        then:
        actual == expected
    }

    def "isValidAuthenticatedRequest unhappy empty path works"() {
        given:
        def validator = AuthRequestValidator.getInstance()
        def token = ""
        def header = Map.of("key", "Bearer " + token)
        def request = new DomainRequest()
        def expected = false

        TestApplicationContext.injectRegisteredImplementations()

        when:
        request.setHeaders(header)
        def actual = validator.isValidAuthenticatedRequest(request)

        then:
        actual == expected
    }

    def "isValidAuthenticatedRequest unhappy empty path works"() {
        given:
        def validator = AuthRequestValidator.getInstance()
        def token = ""
        def header = Map.of("Authorization", "Bearer " + token)
        def request = new DomainRequest()
        def expected = false

        TestApplicationContext.injectRegisteredImplementations()

        when:
        request.setHeaders(header)
        def actual = validator.isValidAuthenticatedRequest(request)

        then:
        actual == expected
    }

    def "isValidAuthenticatedRequest unhappy null path works"() {
        given:
        def validator = AuthRequestValidator.getInstance()
        def header = new HashMap<String, String>()
        def request = new DomainRequest()
        def expected = false

        TestApplicationContext.injectRegisteredImplementations()

        when:
        request.setHeaders(header)
        def actual = validator.isValidAuthenticatedRequest(request)

        then:
        actual == expected
    }

    def "isValidAuthenticatedRequest unhappy invalidTokenException path works"() {
        given:
        def validator = AuthRequestValidator.getInstance()
        def token = "fake-token-here"
        def header = Map.of("Authorization", "Bearer " + token)
        def mockEngine = Mock(JjwtEngine)
        def mockCache = Mock(KeyCache)
        def request = new DomainRequest()
        def expected = false
        TestApplicationContext.register(Cache, mockCache)
        TestApplicationContext.register(AuthEngine, mockEngine)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        request.setHeaders(header)
        mockCache.get(_ as String) >> {"my-fake-private-key"}
        mockEngine.validateToken(_ as String, _ as String) >> { throw new InvalidTokenException(new Throwable("fake exception"))}
        def actual = validator.isValidAuthenticatedRequest(request)

        then:
        actual == expected
    }
}
