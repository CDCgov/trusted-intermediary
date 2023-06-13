package gov.hhs.cdc.trustedintermediary.auth

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.external.inmemory.KeyCache
import gov.hhs.cdc.trustedintermediary.wrappers.Cache
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets
import spock.lang.Specification

class AuthRequestValidatorTest extends Specification{

    def "tokenHascontent unhappy path works"() {
        given:
        def emptyToken = ""
        def expected = false
        def validator = AuthRequestValidator.getInstance()

        when:
        def actual = validator.tokenHasContent(emptyToken)

        then:
        actual == expected
    }

    def "tokenHascontent happy path works"() {
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

    def "retrievePrivateKey works when keyCache not empty"() {
        given:
        def cache = Mock(KeyCache)
        def key = "fake key"
        def expected = key
        def validator = AuthRequestValidator.getInstance()
        TestApplicationContext.register(Cache, cache)
        TestApplicationContext.register(AuthRequestValidator, validator)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        cache.get(_ as String) >> key
        def actual = validator.retrievePrivateKey()
        then:
        actual == expected
    }

    def "retrievePrivateKey works when keyCache is empty"() {
        given:
        def cache = Mock(KeyCache)
        def secrets = Mock(Secrets)
        def key = "fake key"
        def expected = key
        def validator = AuthRequestValidator.getInstance()
        TestApplicationContext.register(Cache, cache)
        TestApplicationContext.register(Secrets, secrets)
        TestApplicationContext.register(AuthRequestValidator, validator)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        cache.get(_ as String) >> null
        secrets.getKey(_ as String) >> key
        def actual = validator.retrievePrivateKey()
        then:
        actual == expected
    }
}
